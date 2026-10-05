package com.example.pocketdex.core.data.catalog

import androidx.room.withTransaction
import com.example.pocketdex.core.data.local.PocketDexDatabase
import com.example.pocketdex.core.data.remote.PokeApi
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.CatalogRepository
import com.example.pocketdex.core.domain.repository.PokemonRepository
import com.example.pocketdex.core.domain.util.DispatcherProvider
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import com.example.pocketdex.core.data.remote.resourceId
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

fun interface EpochClock { fun now(): Long }

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val index: CatalogIndexProvider, private val api: PokeApi, private val database: PocketDexDatabase,
    private val dispatchers: DispatcherProvider, private val clock: EpochClock,
) : CatalogRepository, PokemonRepository {
    private val dao get() = database.dao()
    private val downloads = Semaphore(4)

    override suspend fun cached(query: CatalogQuery): DataResult<CachedCatalog> = dataResult {
        val data = dao.pokemon().map { it.detail().summary }
        val metadata = index.stored()
        val coverage = if (metadata != null && metadata.all { entry -> data.any { it.identity.id.value == entry.speciesId } })
            CatalogCoverage.Complete else CatalogCoverage.Partial
        CachedCatalog(data.filter(query::matches), data.size, coverage)
    }

    override suspend fun page(query: CatalogQuery, request: PageRequest): DataResult<CatalogPage> = dataResult {
        val entries = index.get()
        val candidates = withContext(dispatchers.default) { entries.filter { query.matches(it.summary()) } }
        val fingerprint = withContext(dispatchers.default) {
            hash(query.toString() + candidates.joinToString { "${it.speciesId}:${it.defaultPokemonId}:${it.nationalNumber}" })
        }
        val offset = request.cursor?.let { cursor ->
            val parts = cursor.split(':')
            require(parts.size == 2 && parts[0] == fingerprint)
            parts[1].toInt().also { require(it in 0..candidates.size) }
        } ?: request.offset?.coerceAtMost(candidates.size) ?: request.anchorId?.let { anchor ->
            candidates.indexOfFirst { it.speciesId == anchor.value }.coerceAtLeast(0) / 30 * 30
        } ?: 0
        val selected = candidates.drop(offset).take(request.size)
        val items = boundedMap(selected) { entry ->
            val saved = dao.pokemon(entry.speciesId)
            // Cursor/offset loads may reuse cache; an initial anchored refresh always downloads its visible page.
            if ((request.cursor != null || request.offset != null) && saved != null && saved.detail().statistics.isComplete) saved.detail().summary
            else fetch(entry).summary
        }
        val next = (offset + selected.size).takeIf { it < candidates.size }?.let { "$fingerprint:$it" }
        require(items.all(query::matches))
        dao.pruneUnprotected(512)
        val previous = if (offset > 0) "$fingerprint:${(offset - request.size).coerceAtLeast(0)}" else null
        CatalogPage(items, next, DataOrigin.Remote, CatalogCoverage.Complete, previous, offset, candidates.size - offset - selected.size)
    }

    override suspend fun detail(id: PokemonId): DataResult<PokemonDetail?> = dataResult {
        val saved = dao.pokemon(id.value)
        if (saved != null) saved.detail()
        else index.get().firstOrNull { it.speciesId == id.value }?.let { fetch(it) }
    }

    override suspend fun cachedDetail(id: PokemonId): DataResult<PokemonDetail?> = dataResult {
        dao.pokemon(id.value)?.detail()
    }

    override fun observeDetail(id: PokemonId): Flow<DataResult<PokemonDetail?>> = dao.observePokemon(id.value)
        .map<com.example.pocketdex.core.data.local.PokemonEntity?, DataResult<PokemonDetail?>> { DataResult.Success(it?.detail()) }
        .flowOn(dispatchers.io)
        .catch { error -> if (error is CancellationException) throw error else emit(DataResult.Failure(DataFailure.Storage)) }

    override suspend fun refresh(id: PokemonId): DataResult<PokemonDetail?> = dataResult {
        val stored = index.stored()?.firstOrNull { it.speciesId == id.value }
        val entry = stored ?: run {
            val national = api.nationalDex().entries.firstOrNull { resourceId(it.species.url) == id.value }
                ?: return@dataResult null
            val species = api.species(id.value)
            require(species.id == id.value)
            CatalogIndexEntry(species.id, national.number, species.name,
                species.names.firstOrNull { it.language.name == "en" }?.name,
                species.names.firstOrNull { it.language.name == "es" }?.name,
                resourceId(species.varieties.single { it.isDefault }.pokemon.url), emptyList())
        }
        fetch(entry)
    }

    private suspend fun fetch(entry: CatalogIndexEntry): PokemonDetail = downloads.withPermit {
        val detail = api.pokemon(requireNotNull(entry.defaultPokemonId)).detail(entry)
        database.withTransaction {
            val existing = dao.pokemon(entry.speciesId)
            val merged = detail.copy(summary = detail.summary.copy(localImageFile = existing?.localImageFile))
            dao.upsert(merged.entity(clock.now()))
            merged
        }
    }
}

internal fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
