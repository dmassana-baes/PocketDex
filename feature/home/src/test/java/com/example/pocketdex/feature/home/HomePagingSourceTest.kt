package com.example.pocketdex.feature.home

import androidx.paging.PagingSource
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.home.paging.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class HomePagingSourceTest {
    @Test fun seededPagePreservesGlobalCountsAndOpaqueContinuation() = runTest {
        val fake = CatalogFake()
        val source = HomePagingSource(fake, CatalogQuery(), DataResult.Success(
            CatalogPage(listOf(pokemon(61)), "opaque-next", DataOrigin.Remote, CatalogCoverage.Complete, "opaque-prev", 60, 39)))
        val page = source.load(PagingSource.LoadParams.Refresh<HomePageKey>(null, 30, true)) as PagingSource.LoadResult.Page<HomePageKey, PokemonSummary>
        assertEquals(60, page.itemsBefore)
        assertEquals(39, page.itemsAfter)
        assertEquals(HomePageKey.Cursor("opaque-next"), page.nextKey)
        assertTrue(fake.requests.isEmpty())
    }

    @Test fun retryAfterIncrementalFailureUsesSameContinuation() = runTest {
        val fake = CatalogFake().apply { response = DataResult.Failure(DataFailure.Offline) }
        val source = HomePagingSource(fake, CatalogQuery())
        val params = PagingSource.LoadParams.Append<HomePageKey>(HomePageKey.Cursor("opaque"), 30, true)
        assertTrue(source.load(params) is PagingSource.LoadResult.Error)
        fake.response = DataResult.Success(CatalogPage(listOf(pokemon(31)), null, DataOrigin.Remote, CatalogCoverage.Complete))
        assertTrue(source.load(params) is PagingSource.LoadResult.Page)
        assertEquals(listOf("opaque", "opaque"), fake.requests.map { it.cursor })
    }

    @Test fun jumpRequestsOffsetWithoutParsingCursor() = runTest {
        val fake = CatalogFake()
        HomePagingSource(fake, CatalogQuery()).load(PagingSource.LoadParams.Refresh<HomePageKey>(HomePageKey.Position(90), 30, true))
        assertEquals(90, fake.requests.single().offset)
        assertNull(fake.requests.single().cursor)
    }

    @Test fun successfulRetryPublishesCoverageIncludingVerifiedEmptyPage() = runTest {
        val fake = CatalogFake().apply { response = DataResult.Success(CatalogPage(emptyList(), null, DataOrigin.Remote, CatalogCoverage.Complete)) }
        var observed: CatalogCoverage? = null
        val source = HomePagingSource(fake, CatalogQuery(), DataResult.Failure(DataFailure.Offline)) { page, initial ->
            assertTrue(initial)
            observed = page.coverage
        }
        val params = PagingSource.LoadParams.Refresh<HomePageKey>(null, 30, true)
        assertTrue(source.load(params) is PagingSource.LoadResult.Error)
        assertTrue(source.load(params) is PagingSource.LoadResult.Page)
        assertEquals(CatalogCoverage.Complete, observed)
    }

    @Test fun cancellationIsNotConvertedToLoadError() = runTest {
        val fake = CatalogFake().apply { load = { _, _ -> throw CancellationException("obsolete") } }
        try {
            HomePagingSource(fake, CatalogQuery()).load(PagingSource.LoadParams.Refresh<HomePageKey>(null, 30, true))
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }
}
