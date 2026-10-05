package com.example.pocketdex.feature.compare

import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.*
import kotlinx.coroutines.flow.*

internal fun detail(id: Int = 1, complete: Boolean = true) = PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(id), "pokemon-$id", id),
    listOf(PokemonType.Fire), null), if (complete) PokemonStatistics(1, 2, 3, 4, 5, 6) else PokemonStatistics(hp = 1))

internal class PokemonFake : PokemonRepository {
    val cache = mutableMapOf<PokemonId, PokemonDetail>()
    val calls = mutableListOf<PokemonId>()
    var remote: suspend (PokemonId) -> DataResult<PokemonDetail?> = { DataResult.Success(detail(it.value)) }
    override suspend fun cachedDetail(id: PokemonId) = DataResult.Success(cache[id])
    override fun observeDetail(id: PokemonId) = flowOf(DataResult.Success(cache[id]))
    override suspend fun detail(id: PokemonId) = cachedDetail(id)
    override suspend fun refresh(id: PokemonId): DataResult<PokemonDetail?> { calls += id; return remote(id) }
}

internal class CatalogFake : CatalogRepository {
    val queries = mutableListOf<CatalogQuery>()
    val requests = mutableListOf<PageRequest>()
    var local = CachedCatalog(emptyList(), 0, CatalogCoverage.Partial)
    var response: DataResult<CatalogPage> = DataResult.Success(CatalogPage(listOf(detail().summary), null, DataOrigin.Remote, CatalogCoverage.Complete))
    var load: (suspend (CatalogQuery, PageRequest) -> DataResult<CatalogPage>)? = null
    override suspend fun cached(query: CatalogQuery) = DataResult.Success(local.copy(items = local.items.filter(query::matches)))
    override suspend fun page(query: CatalogQuery, request: PageRequest): DataResult<CatalogPage> {
        queries += query; requests += request
        return load?.invoke(query, request) ?: response
    }
}
