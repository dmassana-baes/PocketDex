package com.example.pocketdex.feature.compare

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.compare.presentation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    val dispatcher = StandardTestDispatcher()
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
@OptIn(ExperimentalCoroutinesApi::class)
class CompareViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val stores = mutableListOf<ViewModelStore>()
    @After fun clear() { stores.forEach { it.clear() } }
    private fun <T : ViewModel> keep(vm: T): T = vm.also { stores += ViewModelStore().apply { put("compare", vm) } }

    @Test fun duplicatesCancelAndReplacementInvalidateResults() = runTest {
        val vm = keep(CompareViewModel(PokemonFake(), SavedStateHandle()))
        assertNull(vm.compare())
        vm.select(0, PokemonId(1)); runCurrent()
        assertFalse(vm.select(1, PokemonId(1)))
        assertNull(vm.state.value.slots[1].id)
        vm.select(1, PokemonId(2)); runCurrent()
        assertEquals(PokemonId(1) to PokemonId(2), vm.compare())
        vm.editSlot(0); vm.cancelEditing()
        assertEquals(PokemonId(1), vm.state.value.slots[0].id)
        vm.remove(0)
        assertNull(vm.state.value.result)
        assertFalse(vm.state.value.canCompare)
        vm.select(0, PokemonId(3)); runCurrent()
        assertEquals(PokemonId(3) to PokemonId(2), vm.compare())
    }
    @Test fun lateResponseCannotReplaceNewSlotAndOtherFailureKeepsUsefulSlot() = runTest {
        val repository = PokemonFake()
        val late = CompletableDeferred<DataResult<PokemonDetail?>>()
        repository.remote = { id -> when (id.value) {
            1 -> withContext(NonCancellable) { late.await() }
            2 -> DataResult.Failure(DataFailure.Offline)
            else -> DataResult.Success(detail(id.value))
        } }
        val vm = keep(CompareViewModel(repository, SavedStateHandle()))
        vm.state.test {
            awaitItem()
            vm.select(0, PokemonId(1)); vm.select(1, PokemonId(2)); runCurrent()
            vm.select(0, PokemonId(3)); runCurrent()
            late.complete(DataResult.Success(detail(1))); runCurrent()
            val state = expectMostRecentItem()
            assertEquals(PokemonId(3), state.slots[0].detail!!.summary.identity.id)
            assertEquals(DataFailure.Offline, state.slots[1].failure)
            assertFalse(state.canCompare)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun incompleteLocalDataStaysOnFailureAndRetryCompletesOnlyItsSlot() = runTest {
        val repository = PokemonFake().apply {
            cache[PokemonId(1)] = detail(1, false)
            cache[PokemonId(2)] = detail(2)
            remote = { DataResult.Failure(DataFailure.Offline) }
        }
        val vm = keep(CompareViewModel(repository, SavedStateHandle()))
        vm.select(0, PokemonId(1)); vm.select(1, PokemonId(2)); runCurrent()
        assertNotNull(vm.state.value.slots[0].detail)
        assertFalse(vm.state.value.canCompare)
        assertEquals(listOf(PokemonId(1)), repository.calls)
        repository.remote = { DataResult.Success(detail(it.value)) }
        vm.retry(0); runCurrent()
        assertTrue(vm.state.value.canCompare)
    }
    @Test fun savedPairAndEditingRestoreButFreshTaskIsEmpty() = runTest {
        val saved = SavedStateHandle()
        val vm = keep(CompareViewModel(PokemonFake(), saved))
        vm.select(0, PokemonId(1)); vm.select(1, PokemonId(2)); vm.editSlot(1); runCurrent()
        val restored = keep(CompareViewModel(PokemonFake(), saved))
        restored.restoreResults(PokemonId(1), PokemonId(2)); runCurrent()
        assertEquals(1, restored.state.value.editingSlot)
        assertNotNull(restored.state.value.result)
        assertFalse(keep(CompareViewModel(PokemonFake(), SavedStateHandle())).state.value.canCompare)
    }
    @Test fun selectorDebounceImeAndPaginationUseCommittedQuery() = runTest {
        val catalog = CatalogFake().apply { response = DataResult.Success(CatalogPage(listOf(detail().summary), "cursor", DataOrigin.Remote, CatalogCoverage.Complete)) }
        val vm = keep(SelectorViewModel(catalog, SavedStateHandle()))
        vm.start(PokemonLanguage.English); runCurrent()
        vm.editText("#025"); advanceTimeBy(299); runCurrent()
        assertEquals(1, catalog.queries.size)
        vm.loadMore(); runCurrent()
        assertEquals(PokemonSearch.All, catalog.queries.last().search)
        advanceTimeBy(1); runCurrent()
        assertEquals(PokemonSearch.parse("#025"), catalog.queries.last().search)
        vm.editText(" PIKA "); vm.searchNow(); runCurrent()
        assertEquals(PokemonSearch.parse("pika"), catalog.queries.last().search)
        assertFalse(vm.pick(PokemonId(1), PokemonId(1)))
        assertTrue(vm.state.value.duplicate)
        assertTrue(vm.pick(PokemonId(2), PokemonId(1)))
        assertFalse(vm.pick(PokemonId(3), PokemonId(1)))
    }
    @Test fun selectorOfflineRetainsCacheAndRetryPreservesAvailableResults() = runTest {
        val catalog = CatalogFake().apply { local = CachedCatalog(listOf(detail(5).summary), 1, CatalogCoverage.Partial)
            response = DataResult.Failure(DataFailure.Offline) }
        val vm = keep(SelectorViewModel(catalog, SavedStateHandle()))
        vm.start(PokemonLanguage.Spanish); runCurrent()
        assertEquals(PokemonId(5), vm.state.value.items.single().identity.id)
        assertEquals(CatalogCoverage.Partial, vm.state.value.coverage)
        val gate = CompletableDeferred<DataResult<CatalogPage>>()
        catalog.load = { _, _ -> gate.await() }
        vm.retry(); runCurrent()
        assertEquals(1, vm.state.value.items.size)
        gate.complete(DataResult.Failure(DataFailure.Offline)); runCurrent()
        assertEquals(DataFailure.Offline, vm.state.value.failure)
    }
    @Test fun equivalentQueriesKeepScrollAndClearAppliesImmediately() = runTest {
        val catalog = CatalogFake()
        val vm = keep(SelectorViewModel(catalog, SavedStateHandle()))
        vm.start(PokemonLanguage.English); runCurrent()
        vm.editText("pika"); advanceTimeBy(300); runCurrent()
        vm.saveScroll(8, 45, 25)
        vm.searchNow(); runCurrent()
        vm.editText(" PIKA "); advanceTimeBy(300); runCurrent()
        assertEquals(2, catalog.queries.size)
        assertEquals(8, vm.scrollIndex)
        assertEquals(45, vm.scrollOffset)
        vm.editText(""); runCurrent()
        assertEquals(3, catalog.queries.size)
        assertEquals(PokemonSearch.All, catalog.queries.last().search)
        assertEquals(0, vm.scrollIndex)
    }
    @Test fun selectorRestoresAnchorAndDoesNotReloadOnRotation() = runTest {
        val saved = SavedStateHandle(mapOf("selector.text" to "", "selector.index" to 40,
            "selector.offset" to 12, "selector.anchor" to 61))
        val catalog = CatalogFake()
        val vm = keep(SelectorViewModel(catalog, saved))
        vm.start(PokemonLanguage.English); runCurrent()
        assertEquals(PokemonId(61), catalog.requests.single().anchorId)
        assertEquals(40, vm.scrollIndex)
        vm.start(PokemonLanguage.English); runCurrent()
        assertEquals(1, catalog.requests.size)
    }
}
