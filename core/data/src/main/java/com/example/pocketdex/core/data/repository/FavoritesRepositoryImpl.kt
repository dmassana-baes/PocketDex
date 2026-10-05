package com.example.pocketdex.core.data.repository

import androidx.room.withTransaction
import com.example.pocketdex.core.data.catalog.*
import com.example.pocketdex.core.data.image.PrivateImageStore
import com.example.pocketdex.core.data.local.*
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.FavoritesRepository
import com.example.pocketdex.core.domain.util.DispatcherProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.withLock

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val database: PocketDexDatabase, private val catalog: CatalogRepositoryImpl,
    private val images: PrivateImageStore, private val clock: EpochClock, private val dispatchers: DispatcherProvider,
) : FavoritesRepository {
    private val dao get() = database.dao()
    // A single write/refresh coordinator also makes pruning and publication deterministic.
    private val mutations get() = images.references

    override fun observeIds(): Flow<DataResult<Set<PokemonId>>> = dao.observeFavoriteIds()
        .map<List<Int>, DataResult<Set<PokemonId>>> { ids -> DataResult.Success(ids.map(::PokemonId).toSet()) }
        .catch { error -> if (error is CancellationException) throw error else emit(DataResult.Failure(DataFailure.Storage)) }

    override fun observeFavorites(): Flow<DataResult<List<FavoritePokemon>>> = dao.observeFavorites()
        .map<List<FavoriteRecord>, DataResult<List<FavoritePokemon>>> { records -> DataResult.Success(records.map { record ->
            val detail = record.data.singleOrNull()?.detail()
            FavoritePokemon(KnownPokemon(PokemonId(record.mark.pokemonId), detail?.summary, detail), record.mark.addedAt,
                detail?.summary?.localImageFile?.let { File(it).isFile } == true)
        }) }.flowOn(dispatchers.io)
        .catch { error -> if (error is CancellationException) throw error else emit(DataResult.Failure(DataFailure.Storage)) }

    override suspend fun setFavorite(pokemon: KnownPokemon, favorite: Boolean): DataResult<Unit> = dataResult {
        mutations.withLock {
            database.withTransaction {
                if (favorite) {
                    val previous = dao.pokemon(pokemon.id.value)?.detail()
                    val supplied = pokemon.detail ?: pokemon.summary?.let { PokemonDetail(it, PokemonStatistics()) }
                    val content = previous?.let { stored -> supplied?.let { stored.enrichedBy(it) } ?: stored } ?: supplied
                    if (content != null) dao.upsert(content.copy(summary = content.summary.copy(
                        localImageFile = previous?.summary?.localImageFile ?: content.summary.localImageFile,
                    )).entity(clock.now()))
                    dao.insert(FavoriteEntity(pokemon.id.value, clock.now()))
                } else dao.unmark(pokemon.id.value)
            }
        }
    }

    override suspend fun complete(id: PokemonId): DataResult<Unit> = download(id, force = false)

    override suspend fun refresh(id: PokemonId): DataResult<Unit> = download(id, force = true)

    private suspend fun download(id: PokemonId, force: Boolean): DataResult<Unit> = withContext(dispatchers.io) {
        val cached = catalog.cachedDetail(id)
        val saved = (cached as? DataResult.Success)?.value
        val refreshed = if (!force && saved != null && saved.statistics.isComplete && saved.summary.types.isNotEmpty())
            DataResult.Success(saved) else catalog.refresh(id)
        if (refreshed is DataResult.Failure) return@withContext refreshed
        val detail = (refreshed as DataResult.Success).value ?: return@withContext DataResult.Failure(DataFailure.InvalidData)
        if (detail.summary.imageUrl == null && detail.summary.localImageFile?.let { File(it).isFile } != true)
            return@withContext DataResult.Failure(DataFailure.InsufficientLocalData)
        dataResult {
            val url = detail.summary.imageUrl
            if (url != null && (force || detail.summary.localImageFile?.let { File(it).isFile } != true)) {
                val path = images.download(id.value, url)
                try {
                    mutations.withLock {
                        database.withTransaction {
                            val current = dao.pokemon(id.value) ?: throw IllegalArgumentException("Missing downloaded Pokémon")
                            dao.upsert(current.copy(localImageFile = path))
                        }
                        images.pruneUnprotected()
                    }
                } finally { images.finishPublication(path) }
            }
        }
    }
}

/** The row read in the transaction is newer than the caller's UI snapshot. Fill holes only. */
internal fun PokemonDetail.enrichedBy(known: PokemonDetail): PokemonDetail {
    require(summary.identity.id == known.summary.identity.id)
    val names = summary.identity.localizedNames
    val incomingNames = known.summary.identity.localizedNames
    return copy(summary = summary.copy(
        identity = summary.identity.copy(localizedNames = LocalizedPokemonNames(
            names.english ?: incomingNames.english, names.spanish ?: incomingNames.spanish)),
        types = summary.types.ifEmpty { known.summary.types },
        imageUrl = summary.imageUrl ?: known.summary.imageUrl,
        localImageFile = summary.localImageFile ?: known.summary.localImageFile,
    ), statistics = PokemonStatistics(
        statistics.hp ?: known.statistics.hp, statistics.attack ?: known.statistics.attack,
        statistics.defense ?: known.statistics.defense, statistics.specialAttack ?: known.statistics.specialAttack,
        statistics.specialDefense ?: known.statistics.specialDefense, statistics.speed ?: known.statistics.speed,
    ))
}
