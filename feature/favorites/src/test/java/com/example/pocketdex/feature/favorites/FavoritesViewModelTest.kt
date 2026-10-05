package com.example.pocketdex.feature.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.FavoritesRepository
import com.example.pocketdex.feature.favorites.presentation.FavoritesViewModel
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

private fun favorite(id: Int) = FavoritePokemon(KnownPokemon(PokemonId(id), PokemonSummary(
    PokemonIdentity(PokemonId(id), "pokemon-$id", id), listOf(PokemonType.Fire), null)), id.toLong(), false)

private class FavoritesFake : FavoritesRepository {
    val local = MutableStateFlow<DataResult<List<FavoritePokemon>>>(DataResult.Success(emptyList()))
    val requests = mutableListOf<PokemonId>()
    val writes = mutableListOf<PokemonId>()
    var writeResult: DataResult<Unit> = DataResult.Success(Unit)
    var remote: suspend (PokemonId) -> DataResult<Unit> = { DataResult.Failure(DataFailure.Offline) }
    override fun observeFavorites() = local
    override fun observeIds() = local.map { result -> when (result) {
        is DataResult.Success -> DataResult.Success(result.value.map { it.known.id }.toSet())
        is DataResult.Failure -> result
    } }
    override suspend fun setFavorite(pokemon: KnownPokemon, favorite: Boolean): DataResult<Unit> { writes += pokemon.id; return writeResult }
    override suspend fun complete(id: PokemonId) = refresh(id)
    override suspend fun refresh(id: PokemonId): DataResult<Unit> { requests += id; return remote(id) }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val stores = mutableListOf<ViewModelStore>()
    @After fun clear() { stores.forEach { it.clear() } }
    private fun vm(repository: FavoritesFake, saved: SavedStateHandle = SavedStateHandle()) =
        FavoritesViewModel(repository, saved).also { stores += ViewModelStore().apply { put("favorites", it) } }

    @Test fun localListIsImmediateAndOfflineRefreshKeepsIt() = runTest {
        val repository = FavoritesFake().apply { local.value = DataResult.Success(listOf(favorite(2), favorite(1))) }
        val waiting = CompletableDeferred<DataResult<Unit>>()
        repository.remote = { waiting.await() }
        val vm = vm(repository)
        vm.state.test {
            awaitItem(); vm.onVisible(); runCurrent()
            val local = expectMostRecentItem()
            assertEquals(listOf(PokemonId(2), PokemonId(1)), local.items.map { it.known.id })
            assertTrue(local.refreshing)
            waiting.complete(DataResult.Failure(DataFailure.Offline)); runCurrent()
            val failed = expectMostRecentItem()
            assertEquals(local.items, failed.items)
            assertEquals(setOf(PokemonId(1), PokemonId(2)), failed.downloadFailures.keys)
            assertFalse(failed.refreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun readingFailureIsDistinctFromEmptyAndRetainsUsefulContent() = runTest {
        val repository = FavoritesFake().apply { local.value = DataResult.Failure(DataFailure.Storage) }
        val vm = vm(repository); runCurrent()
        assertFalse(vm.state.value.ready)
        assertEquals(DataFailure.Storage, vm.state.value.readFailure)
        repository.local.value = DataResult.Success(listOf(favorite(1))); runCurrent()
        repository.local.value = DataResult.Failure(DataFailure.Storage); runCurrent()
        assertEquals(1, vm.state.value.items.size)
        vm.remove(PokemonId(1)); runCurrent()
        assertTrue(repository.writes.isEmpty())
        vm.retryRead(); runCurrent()
        repository.local.value = DataResult.Success(emptyList()); runCurrent()
        assertTrue(vm.state.value.ready)
        assertTrue(vm.state.value.items.isEmpty())
    }
    @Test fun removeWaitsForObservedPersistenceAndFailureKeepsFavorite() = runTest {
        val repository = FavoritesFake().apply { local.value = DataResult.Success(listOf(favorite(1))) }
        val vm = vm(repository); runCurrent()
        repository.writeResult = DataResult.Failure(DataFailure.Storage)
        vm.remove(PokemonId(1)); vm.remove(PokemonId(1)); runCurrent()
        assertEquals(1, repository.writes.size)
        assertEquals(1, vm.state.value.items.size)
        assertEquals(DataFailure.Storage, vm.state.value.writeFailures[PokemonId(1)])
        repository.writeResult = DataResult.Success(Unit)
        vm.remove(PokemonId(1)); runCurrent()
        assertTrue(PokemonId(1) in vm.state.value.writing)
        repository.local.value = DataResult.Success(emptyList()); runCurrent()
        assertTrue(vm.state.value.items.isEmpty())
        assertTrue(vm.state.value.writing.isEmpty())
    }
    @Test fun rotationDoesNotRefreshAndHiddenCancelsThenVisibleResumes() = runTest {
        val repository = FavoritesFake().apply { local.value = DataResult.Success(listOf(favorite(1))) }
        var cancelled = 0
        repository.remote = { try { awaitCancellation() } finally { cancelled++ } }
        val vm = vm(repository); vm.onVisible(); runCurrent()
        vm.onHidden(true); vm.onVisible(); runCurrent()
        assertEquals(1, repository.requests.size)
        vm.onHidden(false); runCurrent()
        assertEquals(1, cancelled)
        vm.onVisible(); runCurrent()
        assertEquals(2, repository.requests.size)
        vm.onHidden(false); runCurrent()
    }
    @Test fun refreshIsBoundedAndChangesToLocalDataDoNotRestartIt() = runTest {
        val repository = FavoritesFake().apply { local.value = DataResult.Success((1..10).map(::favorite)) }
        var active = 0
        var maximum = 0
        val waiting = CompletableDeferred<Unit>()
        repository.remote = { active++; maximum = maxOf(maximum, active)
            try { waiting.await(); DataResult.Success(Unit) } finally { active-- } }
        val vm = vm(repository); vm.onVisible(); runCurrent()
        assertEquals(4, maximum)
        repository.local.value = DataResult.Success((1..10).map { favorite(it).copy(imageAvailableLocally = true) }); runCurrent()
        assertEquals(4, repository.requests.size)
        waiting.complete(Unit); runCurrent()
        assertEquals(10, repository.requests.size)
        assertTrue(maximum <= 4)
    }
    @Test fun savedScrollSurvivesSameTaskAndFreshHandleStartsAtTop() = runTest {
        val saved = SavedStateHandle()
        val vm = vm(FavoritesFake(), saved)
        vm.saveScroll(5, 123, 25)
        val restored = vm(FavoritesFake(), saved)
        assertEquals(5, restored.scrollIndex)
        assertEquals(123, restored.scrollOffset)
        assertEquals(25, restored.scrollAnchor)
        assertEquals(0, vm(FavoritesFake()).scrollIndex)
    }
}
