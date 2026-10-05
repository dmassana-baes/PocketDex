package com.example.pocketdex.feature.home

import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.*
import kotlinx.coroutines.flow.*

internal fun pokemon(id: Int = 1) = PokemonSummary(PokemonIdentity(PokemonId(id), "pokemon-$id", id), listOf(PokemonType.Fire), null)

internal class CatalogFake : CatalogRepository {
    val queries = mutableListOf<CatalogQuery>()
    val requests = mutableListOf<PageRequest>()
    var local = CachedCatalog(emptyList(), 0, CatalogCoverage.Partial)
    var response: DataResult<CatalogPage> = DataResult.Success(CatalogPage(listOf(pokemon()), null, DataOrigin.Remote, CatalogCoverage.Complete))
    var load: (suspend (CatalogQuery, PageRequest) -> DataResult<CatalogPage>)? = null
    override suspend fun cached(query: CatalogQuery) = DataResult.Success(local.copy(items = local.items.filter(query::matches)))
    override suspend fun page(query: CatalogQuery, request: PageRequest): DataResult<CatalogPage> {
        queries.add(query); requests.add(request)
        return load?.invoke(query, request) ?: response
    }
}

internal class FavoritesFake : FavoritesRepository {
    val ids = MutableStateFlow<DataResult<Set<PokemonId>>>(DataResult.Success(emptySet()))
    val writes = mutableListOf<Pair<PokemonId, Boolean>>()
    var writeFailure: DataFailure? = null
    var completion: DataResult<Unit> = DataResult.Success(Unit)
    var completionLoad: (suspend () -> DataResult<Unit>)? = null
    var refreshCalls = 0
    var confirm = true
    override fun observeIds(): Flow<DataResult<Set<PokemonId>>> = ids
    override fun observeFavorites(): Flow<DataResult<List<FavoritePokemon>>> = flowOf(DataResult.Success(emptyList()))
    override suspend fun setFavorite(pokemon: KnownPokemon, favorite: Boolean): DataResult<Unit> {
        writes.add(pokemon.id to favorite)
        writeFailure?.let { return DataResult.Failure(it) }
        if (confirm) {
            val current = (ids.value as DataResult.Success).value
            ids.value = DataResult.Success(if (favorite) current + pokemon.id else current - pokemon.id)
        }
        return DataResult.Success(Unit)
    }
    override suspend fun refresh(id: PokemonId): DataResult<Unit> {
        refreshCalls++
        return completionLoad?.invoke() ?: completion
    }
    override suspend fun complete(id: PokemonId): DataResult<Unit> = refresh(id)
}
