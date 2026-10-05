package com.example.pocketdex.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.*
import com.example.pocketdex.feature.detail.presentation.DetailViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

private fun detail(id: Int = 25) = PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(id), "pokemon-$id", id),
    listOf(PokemonType.Electric), null), PokemonStatistics(hp = 35, speed = 90))

private class PokemonFake : PokemonRepository {
    val local = MutableStateFlow<DataResult<PokemonDetail?>>(DataResult.Success(null))
    var remote: suspend (PokemonId) -> DataResult<PokemonDetail?> = { DataResult.Failure(DataFailure.Offline) }
    val calls = mutableListOf<PokemonId>()
    override suspend fun cachedDetail(id: PokemonId) = local.value
    override fun observeDetail(id: PokemonId) = local
    override suspend fun refresh(id: PokemonId): DataResult<PokemonDetail?> { calls += id; return remote(id) }
    override suspend fun detail(id: PokemonId) = cachedDetail(id)
}

private class FavoritesFake : FavoritesRepository {
    val ids = MutableStateFlow<DataResult<Set<PokemonId>>>(DataResult.Success(emptySet()))
    val writes = mutableListOf<Pair<KnownPokemon, Boolean>>()
    var writeResult: DataResult<Unit> = DataResult.Success(Unit)
    var completion: DataResult<Unit> = DataResult.Failure(DataFailure.Offline)
    var completionCalls = 0
    override fun observeIds() = ids
    override fun observeFavorites() = flowOf(DataResult.Success(emptyList<FavoritePokemon>()))
    override suspend fun setFavorite(pokemon: KnownPokemon, favorite: Boolean): DataResult<Unit> {
        writes += pokemon to favorite
        return writeResult
    }
    override suspend fun complete(id: PokemonId): DataResult<Unit> { completionCalls++; return completion }
    override suspend fun refresh(id: PokemonId) = completion
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val stores = mutableListOf<ViewModelStore>()
    @After fun clear() { stores.forEach { it.clear() } }
    private fun vm(pokemon: PokemonFake, favorites: FavoritesFake = FavoritesFake(), saved: SavedStateHandle = SavedStateHandle()) =
        DetailViewModel(pokemon, favorites, saved).also { stores += ViewModelStore().apply { put("detail", it) } }

    @Test fun localContentArrivesBeforeRemoteAndSurvivesOfflineFailure() = runTest {
        val repository = PokemonFake().apply { local.value = DataResult.Success(detail()) }
        val waiting = CompletableDeferred<DataResult<PokemonDetail?>>()
        repository.remote = { waiting.await() }
        val vm = vm(repository)
        vm.state.test {
            awaitItem()
            vm.open(PokemonId(25)); runCurrent()
            assertEquals(detail(), expectMostRecentItem().detail)
            waiting.complete(DataResult.Failure(DataFailure.Offline)); runCurrent()
            val failed = expectMostRecentItem()
            assertEquals(detail(), failed.detail)
            assertEquals(DataFailure.Offline, failed.contentFailure)
            assertFalse(failed.refreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun favoriteWaitsForObservedPersistenceAndCompletionFailureKeepsMark() = runTest {
        val favorites = FavoritesFake()
        val vm = vm(PokemonFake(), favorites)
        vm.open(PokemonId(25)); runCurrent()
        vm.favorite(); vm.favorite(); runCurrent()
        assertEquals(1, favorites.writes.size)
        assertNull(favorites.writes.single().first.detail)
        assertFalse(vm.state.value.favorite)
        assertTrue(vm.state.value.favoriteBusy)
        favorites.ids.value = DataResult.Success(setOf(PokemonId(25))); runCurrent()
        assertTrue(vm.state.value.favorite)
        assertFalse(vm.state.value.favoriteBusy)
        assertEquals(DataFailure.Offline, vm.state.value.mediaFailure)
        favorites.writeResult = DataResult.Failure(DataFailure.Storage)
        vm.favorite(); runCurrent()
        assertTrue(vm.state.value.favorite)
        assertEquals(DataFailure.Storage, vm.state.value.favoriteFailure)
    }
    @Test fun changedIdCancelsRemoteAndRejectsWrongIdentity() = runTest {
        val repository = PokemonFake()
        var cancelled = false
        repository.remote = { id ->
            if (id.value == 25) try { awaitCancellation() } finally { cancelled = true }
            else DataResult.Success(detail(25))
        }
        val vm = vm(repository)
        vm.open(PokemonId(25)); runCurrent()
        vm.open(PokemonId(26)); runCurrent()
        assertTrue(cancelled)
        assertEquals(PokemonId(26), vm.state.value.id)
        assertNull(vm.state.value.detail)
        assertEquals(DataFailure.InvalidData, vm.state.value.contentFailure)
    }
    @Test fun markingDuringRemoteRefreshDefersCompletionUntilItFinishes() = runTest {
        val waiting = CompletableDeferred<DataResult<PokemonDetail?>>()
        val repository = PokemonFake().apply { remote = { waiting.await() } }
        val favorites = FavoritesFake()
        val vm = vm(repository, favorites)
        vm.open(PokemonId(25)); runCurrent()
        vm.favorite(); runCurrent()
        favorites.ids.value = DataResult.Success(setOf(PokemonId(25))); runCurrent()
        assertEquals(0, favorites.completionCalls)
        assertEquals(1, repository.calls.size)
        waiting.complete(DataResult.Success(detail())); runCurrent()
        assertEquals(1, favorites.completionCalls)
        assertTrue(vm.state.value.favorite)
    }
    @Test fun cachedAbsenceIsNotNotFoundAndSuccessfulRetryCompletesObservedData() = runTest {
        val repository = PokemonFake()
        val vm = vm(repository)
        vm.open(PokemonId(25)); runCurrent()
        assertFalse(vm.state.value.notFound)
        repository.remote = { DataResult.Success(detail().copy(statistics = PokemonStatistics(1, 2, 3, 4, 5, 6))) }
        vm.refresh(); runCurrent()
        assertTrue(vm.state.value.detail!!.statistics.isComplete)
        assertNull(vm.state.value.contentFailure)
        repository.local.value = DataResult.Success(detail().copy(summary = detail().summary.copy(localImageFile = "cached.image")))
        runCurrent()
        assertEquals("cached.image", vm.state.value.detail!!.summary.localImageFile)
    }
    @Test fun rotationDoesNotReloadAndRestoredIdKeepsScrollUntilIdChanges() = runTest {
        val repository = PokemonFake()
        val saved = SavedStateHandle()
        val vm = vm(repository, saved = saved)
        vm.open(PokemonId(25)); vm.onVisible(); runCurrent()
        vm.saveScroll(345)
        vm.onHidden(true); vm.open(PokemonId(25)); vm.onVisible(); runCurrent()
        assertEquals(1, repository.calls.size)
        val restored = vm(repository, saved = saved)
        restored.open(PokemonId(25)); runCurrent()
        assertEquals(345, restored.scrollOffset)
        restored.open(PokemonId(26)); runCurrent()
        assertEquals(0, restored.scrollOffset)
        vm.onHidden(false); vm.onVisible(); runCurrent()
        assertEquals(2, repository.calls.count { it.value == 25 } - 1)
    }
}
