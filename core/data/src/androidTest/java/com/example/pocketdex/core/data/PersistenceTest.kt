package com.example.pocketdex.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pocketdex.core.data.catalog.*
import com.example.pocketdex.core.data.image.PrivateImageStore
import com.example.pocketdex.core.data.local.PocketDexDatabase
import com.example.pocketdex.core.data.remote.*
import com.example.pocketdex.core.data.repository.FavoritesRepositoryImpl
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.util.DispatcherProvider
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceTest {
    private val dispatchers = object : DispatcherProvider {
        override val io = Dispatchers.IO
        override val default = Dispatchers.Default
        override val main = Dispatchers.Main
    }
    private val offline = object : PokeApi {
        override suspend fun nationalDex(): DexDto = throw java.net.UnknownHostException()
        override suspend fun species(id: Int): SpeciesDto = throw java.net.UnknownHostException()
        override suspend fun pokemon(id: Int): PokemonDto = throw java.net.UnknownHostException()
    }

    @Test fun favoritesAndDownloadedDataSurviveDiskReopenAndUnmark() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "home-test-${UUID.randomUUID()}.db"
        val media = File(context.filesDir, "test-${UUID.randomUUID()}.image").apply { writeBytes(byteArrayOf(1)) }
        var database = open(context, name)
        var now = 100L
        fun catalog() = CatalogRepositoryImpl(CatalogIndexProvider(offline, object : IndexApi {
            override suspend fun csv(file: String): okhttp3.ResponseBody = throw java.net.UnknownHostException()
        }, database.dao(), Json, dispatchers), offline, database, dispatchers, EpochClock { now })
        fun favorites() = FavoritesRepositoryImpl(database, catalog(), PrivateImageStore(context, OkHttpClient(), database, dispatchers), EpochClock { now }, dispatchers)
        try {
            val first = PokemonSummary(PokemonIdentity(PokemonId(1), "bulbasaur", 1), listOf(PokemonType.Grass), null, media.absolutePath)
            assertTrue(favorites().setFavorite(KnownPokemon(first.identity.id, first), true) is DataResult.Success)
            now = 200
            assertTrue(favorites().setFavorite(KnownPokemon(PokemonId(2)), true) is DataResult.Success)
            now = 300
            favorites().setFavorite(KnownPokemon(first.identity.id, first), true)
            assertEquals(100L, database.dao().favorite(1)?.addedAt)
            database.close()
            database = open(context, name)
            val restored = (favorites().observeFavorites().first() as DataResult.Success).value
            assertEquals(listOf(PokemonId(2), PokemonId(1)), restored.map { it.known.id })
            assertTrue(restored.last().imageAvailableLocally)
            assertNull(restored.first().known.summary)
            val cached = (catalog().cached(CatalogQuery()) as DataResult.Success).value
            assertEquals(listOf(first), cached.items)
            assertEquals(CatalogCoverage.Partial, cached.coverage)
            assertTrue(catalog().page(CatalogQuery(), PageRequest()) is DataResult.Failure)
            favorites().setFavorite(KnownPokemon(PokemonId(1)), false)
            assertNull(database.dao().favorite(1))
            assertNotNull(database.dao().pokemon(1))
            assertTrue(media.exists())
            now = 400
            favorites().setFavorite(KnownPokemon(PokemonId(1)), true)
            assertEquals(listOf(PokemonId(1), PokemonId(2)),
                (favorites().observeFavorites().first() as DataResult.Success).value.map { it.known.id })
            favorites().setFavorite(KnownPokemon(PokemonId(3)), true)
            assertEquals(listOf(PokemonId(1), PokemonId(3), PokemonId(2)),
                (favorites().observeFavorites().first() as DataResult.Success).value.map { it.known.id })
            favorites().setFavorite(KnownPokemon(PokemonId(1)), false)
            database.dao().pruneUnprotected(0)
            assertNotNull(database.dao().favorite(2))
        } finally {
            database.close()
            context.deleteDatabase(name)
            media.delete()
        }
    }

    private fun open(context: Context, name: String) = Room.databaseBuilder(context, PocketDexDatabase::class.java, name).build()

    @Test fun detailRefreshUsesSpeciesDefaultWithoutCsvAndReopensOffline() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "detail-test-${UUID.randomUUID()}.db"
        var database = open(context, name)
        val calls = mutableListOf<Int>()
        val api = object : PokeApi {
            override suspend fun nationalDex() = DexDto(listOf(DexEntryDto(25,
                ResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon-species/25/"))))
            override suspend fun species(id: Int) = SpeciesDto(25, "pikachu", emptyList(), listOf(
                VarietyDto(true, ResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon/1025/"))))
            override suspend fun pokemon(id: Int): PokemonDto {
                calls += id
                return PokemonDto(1025, true, ResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon-species/25/"),
                    listOf(TypeDto(1, ResourceDto("electric", "unused"))),
                    listOf(StatDto(90, ResourceDto("speed", "unused"))), SpritesDto())
            }
        }
        val noCsv = object : IndexApi {
            override suspend fun csv(file: String): okhttp3.ResponseBody = error("Detail must not fetch CSV")
        }
        fun catalog(api: PokeApi) = CatalogRepositoryImpl(CatalogIndexProvider(api, noCsv, database.dao(), Json, dispatchers),
            api, database, dispatchers, EpochClock { 1 })
        try {
            assertNull((catalog(api).cachedDetail(PokemonId(25)) as DataResult.Success).value)
            val remote = (catalog(api).refresh(PokemonId(25)) as DataResult.Success).value!!
            assertEquals(listOf(1025), calls)
            assertEquals(90, remote.statistics.speed)
            assertNull(remote.statistics.hp)
            val repository = FavoritesRepositoryImpl(database, catalog(api), PrivateImageStore(context, OkHttpClient(), database, dispatchers), EpochClock { 1 }, dispatchers)
            repository.setFavorite(KnownPokemon(PokemonId(25), remote.summary, remote), true)
            database.close()
            database = open(context, name)
            val restored = (catalog(offline).observeDetail(PokemonId(25)).first() as DataResult.Success).value
            assertEquals(remote, restored)
            assertEquals(DataResult.Failure(DataFailure.Offline), catalog(offline).refresh(PokemonId(25)))
            assertEquals(remote, (catalog(offline).cachedDetail(PokemonId(25)) as DataResult.Success).value)
            assertNotNull(database.dao().favorite(25))
            database.dao().upsert(remote.copy(statistics = PokemonStatistics(1, 2, 3, 4, 5, 6)).entity(2))
            val offlineFavorites = FavoritesRepositoryImpl(database, catalog(offline),
                PrivateImageStore(context, OkHttpClient(), database, dispatchers), EpochClock { 3 }, dispatchers)
            // A delayed UI write supplies the old partial snapshot after the complete row was saved.
            offlineFavorites.setFavorite(KnownPokemon(PokemonId(25), remote.summary, remote), true)
            assertTrue((catalog(offline).observeDetail(PokemonId(25)).first() as DataResult.Success).value!!.statistics.isComplete)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun globalFilterUsesDefaultVarietyAndAnchoredRefreshDownloadsVisiblePage() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "catalog-test-${UUID.randomUUID()}.db"
        val database = open(context, name)
        val downloads = mutableListOf<Int>()
        val api = object : PokeApi {
            override suspend fun nationalDex() = DexDto((1..100).map { DexEntryDto(it,
                ResourceDto("pokemon-$it", "https://pokeapi.co/api/v2/pokemon-species/$it/")) })
            override suspend fun species(id: Int): SpeciesDto = error("Complete metadata must not fetch every species")
            override suspend fun pokemon(id: Int): PokemonDto {
                synchronized(downloads) { downloads.add(id) }
                val species = id - 1000
                return PokemonDto(id, true, ResourceDto("pokemon-$species", "https://pokeapi.co/api/v2/pokemon-species/$species/"),
                    listOf(TypeDto(1, ResourceDto(if (species == 99) "water" else "fire", "unused"))),
                    listOf("hp", "attack", "defense", "special-attack", "special-defense", "speed").map { StatDto(50, ResourceDto(it, "unused")) },
                    SpritesDto())
            }
        }
        val csv = object : IndexApi {
            override suspend fun csv(file: String) = when (file) {
                "pokemon" -> "id,species_id,is_default\n" + (1..100).joinToString("\n") { "${1000 + it},$it,1" }
                "pokemon_types" -> "pokemon_id,type_id,slot\n" + (1..100).joinToString("\n") { "${1000 + it},${if (it == 99) 11 else 10},1" }
                "pokemon_species_names" -> "pokemon_species_id,local_language_id,name\n" + (1..100).joinToString("\n") { "$it,9,Pokemon-$it\n$it,7,Pokemon-$it" }
                else -> error("Unexpected metadata")
            }.toResponseBody()
        }
        try {
            val catalog = CatalogRepositoryImpl(CatalogIndexProvider(api, csv, database.dao(), Json, dispatchers), api, database, dispatchers, EpochClock { 1 })
            val filtered = catalog.page(CatalogQuery(type = PokemonType.Water), PageRequest()) as DataResult.Success
            assertEquals(listOf(PokemonId(99)), filtered.value.items.map { it.identity.id })
            assertEquals(listOf(1099), downloads)
            val first = catalog.page(CatalogQuery(), PageRequest(anchorId = PokemonId(61))) as DataResult.Success
            assertEquals(60, first.value.itemsBefore)
            assertEquals((61..90).toList(), first.value.items.map { it.identity.nationalNumber })
            catalog.page(CatalogQuery(), PageRequest(anchorId = PokemonId(61)))
            assertEquals(2, downloads.count { it == 1061 })
            assertEquals(DataResult.Failure(DataFailure.InvalidData), catalog.page(CatalogQuery(type = PokemonType.Fire), PageRequest(cursor = first.value.nextCursor)))
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
