package com.example.pocketdex.feature.home.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.CatalogRepository

class CatalogLoadException(val failure: DataFailure) : Exception(failure.name)

sealed interface HomePageKey {
    data class Cursor(val value: String) : HomePageKey
    data class Position(val offset: Int) : HomePageKey
}

class HomePagingSource(
    private val repository: CatalogRepository,
    private val query: CatalogQuery,
    private var firstPage: DataResult<CatalogPage>? = null,
    private val onPage: (CatalogPage, Boolean) -> Unit = { _, _ -> },
) : PagingSource<HomePageKey, PokemonSummary>() {
    override val jumpingSupported: Boolean = true
    override suspend fun load(params: LoadParams<HomePageKey>): LoadResult<HomePageKey, PokemonSummary> {
        val result = if (params.key == null && firstPage != null) firstPage.also { firstPage = null }!!
            else repository.page(query, when (val key = params.key) {
                is HomePageKey.Cursor -> PageRequest(cursor = key.value, size = params.loadSize)
                is HomePageKey.Position -> PageRequest(size = params.loadSize, offset = key.offset)
                null -> PageRequest(size = params.loadSize)
            })
        return when (result) {
            is DataResult.Failure -> LoadResult.Error(CatalogLoadException(result.reason))
            is DataResult.Success -> {
                onPage(result.value, params is LoadParams.Refresh<*>)
                LoadResult.Page(result.value.items, prevKey = result.value.previousCursor?.let(HomePageKey::Cursor),
                    nextKey = result.value.nextCursor?.let(HomePageKey::Cursor), itemsBefore = result.value.itemsBefore, itemsAfter = result.value.itemsAfter)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<HomePageKey, PokemonSummary>): HomePageKey? =
        state.anchorPosition?.let { HomePageKey.Position(it / 30 * 30) }
}
