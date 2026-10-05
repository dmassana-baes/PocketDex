package com.example.pocketdex.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.home.presentation.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.After
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    val dispatcher = StandardTestDispatcher()
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val stores = mutableListOf<ViewModelStore>()
    @After fun clearViewModels() { stores.forEach { it.clear() } }
    private fun viewModel(catalog: CatalogFake, favorites: FavoritesFake, saved: SavedStateHandle): HomeViewModel =
        HomeViewModel(catalog, favorites, saved).also { vm ->
            stores.add(ViewModelStore().apply { put("home", vm) })
        }

    @Test fun debounceImeAndClearUseNormalizedQueries() = runTest {
        val catalog = CatalogFake()
        val vm = viewModel(catalog, FavoritesFake(), SavedStateHandle())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.paging.collect { } }
        runCurrent()
        vm.editText(" PIKA ")
        advanceTimeBy(299); runCurrent()
        assertEquals(1, catalog.queries.size)
        advanceTimeBy(1); runCurrent()
        assertEquals(PokemonSearch.parse("pika"), catalog.queries.last().search)
        vm.searchNow(); runCurrent()
        assertEquals(2, catalog.queries.size)
        vm.editText(""); runCurrent()
        assertEquals(PokemonSearch.All, catalog.queries.last().search)
    }

    @Test fun filterDraftIsDiscardedUntilAppliedAndSurvivesRecreation() = runTest {
        val saved = SavedStateHandle()
        val vm = viewModel(CatalogFake(), FavoritesFake(), saved)
        vm.openFilter(); vm.draftType(PokemonType.Water)
        assertNull(vm.state.value.appliedType)
        val restored = viewModel(CatalogFake(), FavoritesFake(), saved)
        assertTrue(restored.state.value.filterOpen)
        assertEquals(PokemonType.Water, restored.state.value.draftType)
        restored.dismissFilter()
        assertNull(restored.state.value.appliedType)
        vm.applyFilter()
        assertEquals(PokemonType.Water, vm.state.value.appliedType)
        vm.openFilter(); vm.draftType(null); vm.dismissFilter()
        assertEquals(PokemonType.Water, vm.state.value.appliedType)
        vm.clearType(); assertNull(vm.state.value.appliedType)
    }

    @Test fun offlineKeepsValidEmptyLocalQueryAndReportsPartialCoverage() = runTest {
        val catalog = CatalogFake().apply {
            local = CachedCatalog(listOf(pokemon()), 1, CatalogCoverage.Partial)
            response = DataResult.Failure(DataFailure.Offline)
        }
        val vm = viewModel(catalog, FavoritesFake(), SavedStateHandle(mapOf("text" to "#999")))
        assertTrue(vm.paging.asSnapshot().isEmpty())
        runCurrent()
        vm.state.test {
            val state = awaitItem()
            assertTrue(state.hasLocalData)
            assertEquals(DataFailure.Offline, state.notice)
            assertEquals(CatalogCoverage.Partial, state.coverage)
            assertFalse(state.refreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun changingQueryCancelsObsoleteRequest() = runTest {
        var cancelled = false
        val catalog = CatalogFake().apply {
            load = { query, _ ->
                if (query.search == PokemonSearch.All) {
                    try { awaitCancellation() } finally { cancelled = true }
                } else response
            }
        }
        val vm = viewModel(catalog, FavoritesFake(), SavedStateHandle())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.paging.collect { } }
        runCurrent(); vm.editText("#001"); vm.searchNow(); runCurrent()
        assertTrue(cancelled)
        assertEquals(PokemonSearch.parse("#001"), catalog.queries.last().search)
        assertFalse(vm.state.value.refreshing)
    }

    @Test fun favoriteStaysBusyUntilStorageObservationAndFailurePreservesTruth() = runTest {
        val favorites = FavoritesFake().apply { confirm = false }
        val vm = viewModel(CatalogFake(), favorites, SavedStateHandle())
        runCurrent()
        vm.favorite(pokemon()); vm.favorite(pokemon()); runCurrent()
        assertEquals(1, favorites.writes.size)
        assertTrue(PokemonId(1) in vm.state.value.favoriteBusy)
        assertTrue(vm.state.value.favoriteIds.isEmpty())
        favorites.ids.value = DataResult.Success(setOf(PokemonId(1))); runCurrent()
        assertTrue(vm.state.value.favoriteBusy.isEmpty())
        favorites.writeFailure = DataFailure.Storage
        vm.favorite(pokemon()); runCurrent()
        assertEquals(setOf(PokemonId(1)), vm.state.value.favoriteIds)
        assertEquals(DataFailure.Storage, vm.state.value.notice)
        assertTrue(vm.state.value.favoriteBusy.isEmpty())
    }

    @Test fun savedAnchorIsUsedWithoutDownloadingAllPreviousPages() = runTest {
        val catalog = CatalogFake()
        val saved = SavedStateHandle(mapOf("scrollIndex" to 94, "scrollOffset" to 12, "scrollAnchor" to 91))
        val vm = viewModel(catalog, FavoritesFake(), saved)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.paging.collect { } }
        runCurrent()
        assertEquals(PokemonId(91), catalog.requests.single().anchorId)
        assertEquals(30, catalog.requests.single().size)
        assertTrue(vm.state.value.restoringScroll)
        val fresh = viewModel(CatalogFake(), FavoritesFake(), SavedStateHandle())
        assertEquals(0, fresh.scrollIndex)
        assertEquals("", fresh.state.value.text)
    }

    @Test fun recoveredTaskEmitsCacheBeforeSlowRemoteRefresh() = runTest {
        val catalog = CatalogFake().apply {
            local = CachedCatalog(listOf(pokemon(61)), 1, CatalogCoverage.Partial)
            load = { _, _ -> awaitCancellation() }
        }
        val vm = viewModel(catalog, FavoritesFake(), SavedStateHandle(mapOf("scrollIndex" to 64, "scrollAnchor" to 61)))
        vm.paging.test {
            awaitItem()
            runCurrent()
            assertTrue(vm.state.value.hasLocalData)
            assertTrue(vm.state.value.refreshing)
            assertEquals(PokemonId(61), vm.recoveryItem?.identity?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun recoveredTaskWithoutCachePreparesAnchorAfterInitialRetry() = runTest {
        var calls = 0
        val catalog = CatalogFake().apply {
            load = { _, _ ->
                if (++calls == 1) DataResult.Failure(DataFailure.Offline)
                else DataResult.Success(CatalogPage(listOf(pokemon(91)), null, DataOrigin.Remote, CatalogCoverage.Complete))
            }
        }
        val vm = viewModel(catalog, FavoritesFake(), SavedStateHandle(mapOf("scrollIndex" to 94, "scrollAnchor" to 91)))
        var retries = 0
        val snapshot = vm.paging.asSnapshot(onError = {
            assertEquals(0, retries++)
            ErrorRecovery.RETRY
        })
        assertEquals(listOf(PokemonId(91)), snapshot.map { it.identity.id })
        assertEquals(PokemonId(91), vm.recoveryItem?.identity?.id)
        assertEquals(CatalogCoverage.Complete, vm.state.value.coverage)
        vm.restoredScroll()
        assertFalse(vm.state.value.restoringScroll)
    }

    @Test fun repeatedCompletionRetryKeepsOneDownloadForTheSameFavorite() = runTest {
        val favorites = FavoritesFake().apply { completion = DataResult.Failure(DataFailure.Network) }
        val vm = viewModel(CatalogFake(), favorites, SavedStateHandle())
        runCurrent(); vm.favorite(pokemon()); runCurrent()
        assertEquals(PokemonId(1), vm.state.value.completionFailureId)
        favorites.completionLoad = { awaitCancellation() }
        vm.retryCompletion(); vm.retryCompletion(); runCurrent()
        assertEquals(2, favorites.refreshCalls)
        assertEquals(setOf(PokemonId(1)), vm.state.value.favoriteIds)
    }
}
