package com.example.pocketdex.core.data.catalog

import com.example.pocketdex.core.data.local.IndexEntity
import com.example.pocketdex.core.data.local.PocketDexDao
import com.example.pocketdex.core.data.remote.*
import com.example.pocketdex.core.domain.model.PokemonType
import com.example.pocketdex.core.domain.util.DispatcherProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Singleton
class CatalogIndexProvider @Inject constructor(
    private val api: PokeApi, private val csvApi: IndexApi, private val dao: PocketDexDao,
    private val json: Json, private val dispatchers: DispatcherProvider,
) {
    private val mutex = Mutex()
    private var current: List<CatalogIndexEntry>? = null
    private val serializer = ListSerializer(CatalogIndexEntry.serializer())

    suspend fun get(): List<CatalogIndexEntry> = mutex.withLock {
        current ?: withContext(dispatchers.io) {
            val dex = api.nationalDex()
            suspend fun csv(file: String): String = csvApi.csv(file).use { body ->
                val bytes = body.byteStream().readNBytesCompat(4 * 1024 * 1024)
                bytes.toString(Charsets.UTF_8)
            }
            val index = buildIndex(dex, csv("pokemon"), csv("pokemon_types"), csv("pokemon_species_names"))
            // CSV can lag newly released API species. Repair only missing metadata with bounded batches.
            val repaired = boundedMap(index) { entry ->
                if (entry.defaultPokemonId != null && entry.types.isNotEmpty() && entry.englishName != null && entry.spanishName != null) entry
                else {
                    val species = api.species(entry.speciesId)
                    require(species.id == entry.speciesId)
                    val defaultId = resourceId(species.varieties.single { it.isDefault }.pokemon.url)
                    val pokemon = api.pokemon(defaultId)
                    require(pokemon.isDefault && resourceId(pokemon.species.url) == species.id)
                    entry.copy(defaultPokemonId = defaultId,
                        englishName = species.names.firstOrNull { it.language.name == "en" }?.name,
                        spanishName = species.names.firstOrNull { it.language.name == "es" }?.name,
                        types = pokemon.types.sortedBy { it.slot }.map { type -> PokemonType.entries.first { it.name.equals(type.type.name, true) }.name })
                }
            }
            dao.upsert(IndexEntity(payload = json.encodeToString(serializer, repaired)))
            current = repaired
            repaired
        }
    }

    suspend fun stored(): List<CatalogIndexEntry>? = dao.index()?.let { json.decodeFromString(serializer, it.payload) }
}

internal suspend fun <T, R> boundedMap(values: List<T>, transform: suspend (T) -> R): List<R> {
    val result = mutableListOf<R>()
    for (batch in values.chunked(4)) {
        result += coroutineScope { batch.map { async { transform(it) } }.awaitAll() }
    }
    return result
}

internal fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        require(output.size() + read <= limit) { "Resource exceeds limit" }
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}
