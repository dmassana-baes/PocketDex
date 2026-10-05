package com.example.pocketdex.core.data

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pocketdex.core.data.catalog.*
import com.example.pocketdex.core.data.image.PrivateImageStore
import com.example.pocketdex.core.data.local.PocketDexDatabase
import com.example.pocketdex.core.data.remote.*
import com.example.pocketdex.core.data.repository.*
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.QuizMediaRepository
import com.example.pocketdex.core.domain.util.DispatcherProvider
import com.example.pocketdex.core.domain.util.RandomSource
import com.example.pocketdex.core.domain.quiz.generateQuiz
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

/** Uses Room in disk and an intercepting HTTP client; no public image server is required. */
class FavoriteMediaTest {
    private val dispatchers = object : DispatcherProvider {
        override val io = Dispatchers.IO
        override val default = Dispatchers.Default
        override val main = Dispatchers.Main
    }
    private fun png(): ByteArray {
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        return try { ByteArrayOutputStream().use { output ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)); output.toByteArray()
        } } finally { bitmap.recycle() }
    }
    private class Fixture(val parent: Context, val interceptor: Interceptor) {
        val name = "favorite-media-${UUID.randomUUID()}.db"
        val root = File(parent.filesDir, "media-test-${UUID.randomUUID()}").apply { mkdirs() }
        val context = object : ContextWrapper(parent) { override fun getFilesDir() = root }
        var database = Room.databaseBuilder(parent, PocketDexDatabase::class.java, name).build()
        fun reopen() { database.close(); database = Room.databaseBuilder(parent, PocketDexDatabase::class.java, name).build() }
        fun close() { database.close(); parent.deleteDatabase(name); root.deleteRecursively() }
    }
    private val api = object : PokeApi {
        override suspend fun nationalDex() = DexDto(listOf(DexEntryDto(25, ResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon-species/25/"))))
        override suspend fun species(id: Int) = SpeciesDto(25, "pikachu", emptyList(), listOf(VarietyDto(true,
            ResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon/25/"))))
        override suspend fun pokemon(id: Int) = PokemonDto(25, true, ResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon-species/25/"),
            listOf(TypeDto(1, ResourceDto("electric", "unused"))),
            listOf("hp", "attack", "defense", "special-attack", "special-defense", "speed").map { StatDto(50, ResourceDto(it, "unused")) },
            SpritesDto(front = "https://images.example.test/pikachu.png"))
    }
    private fun catalog(database: PocketDexDatabase): CatalogRepositoryImpl = CatalogRepositoryImpl(CatalogIndexProvider(api,
        object : IndexApi { override suspend fun csv(file: String): ResponseBody = error("No CSV in favorite refresh") },
        database.dao(), Json, dispatchers), api, database, dispatchers, EpochClock { 1 })

    @Test fun protectedCopySurvivesPruningAndSessionPinIsIndependentOfMark() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var bytes = png()
        val fixture = Fixture(context, Interceptor { chain -> Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
            .code(200).message("OK").body(bytes.toResponseBody("image/png".toMediaType())).build() })
        val store = PrivateImageStore(fixture.context, OkHttpClient.Builder().addInterceptor(fixture.interceptor).build(), fixture.database, dispatchers)
        val favorites = FavoritesRepositoryImpl(fixture.database, catalog(fixture.database), store, EpochClock { 1 }, dispatchers)
        val quiz: QuizMediaRepository = QuizMediaRepositoryImpl(fixture.database, store, fixture.context)
        try {
            favorites.setFavorite(KnownPokemon(PokemonId(25)), true)
            assertTrue(favorites.refresh(PokemonId(25)) is DataResult.Success)
            val detail = fixture.database.dao().pokemon(25)!!.detail()
            val path = detail.summary.localImageFile!!
            assertTrue(store.verified(path))
            val oversized = File(File(fixture.root, "pokemon-media"), "normal.image")
            RandomAccessFile(oversized, "rw").use { it.setLength(65L * 1024 * 1024) }
            store.prune()
            assertFalse(oversized.exists())
            assertTrue(File(path).isFile)
            // Broken refresh cannot replace the last verified private copy.
            bytes = byteArrayOf(1, 2, 3)
            assertEquals(DataResult.Failure(DataFailure.InvalidData), favorites.refresh(PokemonId(25)))
            assertEquals(path, fixture.database.dao().pokemon(25)!!.localImageFile)
            assertTrue(quiz.prepare("session", listOf(detail.summary)) is DataResult.Success)
            val missing = PokemonSummary(PokemonIdentity(PokemonId(26), "raichu", 26), emptyList(), null)
            assertEquals(DataResult.Failure(DataFailure.InvalidData), quiz.prepare("session", listOf(detail.summary, missing)))
            assertTrue(quiz.restore("session", listOf(PokemonId(25))) is DataResult.Success)
            favorites.setFavorite(KnownPokemon(PokemonId(25)), false)
            assertTrue(File(path).isFile)
            assertTrue(path in fixture.database.dao().protectedPaths())
            assertTrue(quiz.restore("session", listOf(PokemonId(25))) is DataResult.Success)
            assertEquals(DataResult.Failure(DataFailure.InsufficientLocalData), quiz.restore("session", listOf(PokemonId(26))))
            fixture.database.dao().pruneUnprotected(0)
            assertNotNull(fixture.database.dao().pokemon(25))
            quiz.release("session")
            assertTrue(File(path).exists())
            assertFalse(path in fixture.database.dao().protectedPaths())
            assertTrue(favorites.observeIds().first() == DataResult.Success(emptySet<PokemonId>()))
        } finally { fixture.close() }
    }

    @Test fun sessionPinsSurviveDiskReopenAndMissingMediaDoesNotInventReplacement() = runBlocking {
        val fixture = Fixture(InstrumentationRegistry.getInstrumentation().targetContext, Interceptor { error("Restore must stay local") })
        try {
            val image = File(fixture.root, "verified.png").apply { writeBytes(png()) }
            val summary = PokemonSummary(PokemonIdentity(PokemonId(25), "pikachu", 25), listOf(PokemonType.Electric), null, image.path)
            fun quiz() = QuizMediaRepositoryImpl(fixture.database,
                PrivateImageStore(fixture.context, OkHttpClient.Builder().addInterceptor(fixture.interceptor).build(), fixture.database, dispatchers), fixture.context)
            assertTrue(quiz().prepare("session", listOf(summary)) is DataResult.Success)
            fixture.reopen()
            val restored = quiz().restore("session", listOf(PokemonId(25))) as DataResult.Success
            assertEquals(image.path, restored.value.single().localFile)
            image.delete()
            assertEquals(DataResult.Failure(DataFailure.InsufficientLocalData), quiz().restore("session", listOf(PokemonId(25))))
        } finally { fixture.close() }
    }

    @Test fun finishingDownloadAfterUnmarkDoesNotRecreateMarkOrProtection() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val finish = CompletableDeferred<Unit>()
        val bytes = png()
        val fixture = Fixture(InstrumentationRegistry.getInstrumentation().targetContext, Interceptor { chain ->
            started.complete(Unit)
            runBlocking { finish.await() }
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body(bytes.toResponseBody("image/png".toMediaType())).build()
        })
        val store = PrivateImageStore(fixture.context, OkHttpClient.Builder().addInterceptor(fixture.interceptor).build(), fixture.database, dispatchers)
        val favorites = FavoritesRepositoryImpl(fixture.database, catalog(fixture.database), store, EpochClock { 1 }, dispatchers)
        try {
            favorites.setFavorite(KnownPokemon(PokemonId(25)), true)
            val download = async { favorites.refresh(PokemonId(25)) }
            withTimeout(10_000) { started.await() }
            favorites.setFavorite(KnownPokemon(PokemonId(25)), false)
            finish.complete(Unit)
            assertTrue(withTimeout(10_000) { download.await() } is DataResult.Success)
            assertNull(fixture.database.dao().favorite(25))
            val path = fixture.database.dao().pokemon(25)!!.localImageFile!!
            assertTrue(File(path).isFile)
            assertFalse(path in fixture.database.dao().protectedPaths())
        } finally { finish.complete(Unit); fixture.close() }
    }

    @Test fun fiveVerifiedLocalCandidatesPrepareOfflineAndCorruptionPreventsNewSession() = runBlocking {
        val fixture = Fixture(InstrumentationRegistry.getInstrumentation().targetContext, Interceptor { error("No remote images") })
        var networkCalls = 0
        val offline = object : PokeApi {
            override suspend fun nationalDex(): DexDto { networkCalls++; throw java.net.UnknownHostException() }
            override suspend fun species(id: Int): SpeciesDto = throw java.net.UnknownHostException()
            override suspend fun pokemon(id: Int): PokemonDto = throw java.net.UnknownHostException()
        }
        try {
            val store = PrivateImageStore(fixture.context, OkHttpClient.Builder().addInterceptor(fixture.interceptor).build(), fixture.database, dispatchers)
            val index = CatalogIndexProvider(offline, object : IndexApi {
                override suspend fun csv(file: String): ResponseBody = throw java.net.UnknownHostException()
            }, fixture.database.dao(), Json, dispatchers)
            val catalog = CatalogRepositoryImpl(index, offline, fixture.database, dispatchers, EpochClock { 1 })
            val source = QuizCandidateRepositoryImpl(fixture.database, catalog, index, store, RandomSource { 0 }, dispatchers)
            val files = (1..5).map { id ->
                val file = File(fixture.root, "$id.png").apply { writeBytes(png()) }
                val detail = PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(id), "pokemon-$id", id),
                    listOf(PokemonType.Fire), null, file.path), PokemonStatistics(id, 2, 3, 4, 5, 6))
                fixture.database.dao().upsert(detail.entity(1))
                file
            }
            val pool = (source.candidates() as DataResult.Success).value
            assertEquals(DataOrigin.Local, pool.origin); assertEquals(0, networkCalls)
            val session = requireNotNull(generateQuiz("quiz:42:offline", pool.candidates, RandomSource { 0 }))
            val media = QuizMediaRepositoryImpl(fixture.database, store, fixture.context)
            assertEquals(5, (media.prepare(session.id, session.questions.map { it.correct.summary }) as DataResult.Success).value.size)
            fixture.database.dao().pruneUnprotected(0)
            assertEquals(5, fixture.database.dao().pokemon().size)
            RandomAccessFile(files.last(), "rw").use { it.setLength(0) }
            assertEquals(DataResult.Failure(DataFailure.InsufficientLocalData), media.restore(session.id,
                session.questions.map { it.correct.summary.identity.id }))
            assertEquals(DataResult.Failure(DataFailure.InsufficientLocalData), source.candidates())
            assertEquals(5, fixture.database.dao().retained(session.id).size)
        } finally { fixture.close() }
    }

    @Test fun reconcileRemovesOrphanAndReplacedOwnersWithoutReleasingFavorites() = runBlocking {
        val fixture = Fixture(InstrumentationRegistry.getInstrumentation().targetContext, Interceptor { error("No downloads") })
        try {
            val file = File(fixture.root, "verified.png").apply { writeBytes(png()) }
            val summary = PokemonSummary(PokemonIdentity(PokemonId(25), "pikachu", 25), listOf(PokemonType.Electric), null, file.path)
            fixture.database.dao().upsert(PokemonDetail(summary, PokemonStatistics(1, 2, 3, 4, 5, 6)).entity(1))
            fixture.database.dao().insert(com.example.pocketdex.core.data.local.FavoriteEntity(25, 1))
            val media = QuizMediaRepositoryImpl(fixture.database,
                PrivateImageStore(fixture.context, OkHttpClient(), fixture.database, dispatchers), fixture.context)
            media.prepare("quiz:42:old", listOf(summary)); media.prepare("quiz:42:current", listOf(summary))
            media.prepare("quiz:2147483647:orphan", listOf(summary)); media.prepare("other-owner", listOf(summary))
            assertTrue(media.reconcile(42, "quiz:42:current") is DataResult.Success)
            assertTrue(fixture.database.dao().retained("quiz:42:old").isEmpty())
            assertTrue(fixture.database.dao().retained("quiz:2147483647:orphan").isEmpty())
            assertEquals(1, fixture.database.dao().retained("quiz:42:current").size)
            assertEquals(1, fixture.database.dao().retained("other-owner").size)
            media.release("quiz:42:current")
            assertNotNull(fixture.database.dao().favorite(25)); assertTrue(file.path in fixture.database.dao().protectedPaths())
        } finally { fixture.close() }
    }
}
