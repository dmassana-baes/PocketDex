package com.example.pocketdex.feature.favorites.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

data class FavoritesUiState(
    val items: List<FavoritePokemon> = emptyList(),
    val loading: Boolean = true,
    val ready: Boolean = false,
    val readFailure: DataFailure? = null,
    val refreshing: Boolean = false,
    val downloading: Set<PokemonId> = emptySet(),
    val downloadFailures: Map<PokemonId, DataFailure> = emptyMap(),
    val writing: Set<PokemonId> = emptySet(),
    val writeFailures: Map<PokemonId, DataFailure> = emptyMap(),
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favorites: FavoritesRepository,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val mutable = MutableStateFlow(FavoritesUiState())
    val state = mutable.asStateFlow()
    private var observation: Job? = null
    private var batch: Job? = null
    private val downloads = mutableMapOf<PokemonId, Job>()
    private val writes = mutableMapOf<PokemonId, Job>()
    private val slots = Semaphore(4)
    private var visible = false
    private var refreshOnRead = true
    private var refreshGeneration = 0
    val scrollIndex: Int get() = saved.get<Int>("favorites.index")?.coerceAtLeast(0) ?: 0
    val scrollOffset: Int get() = saved.get<Int>("favorites.offset")?.coerceAtLeast(0) ?: 0
    val scrollAnchor: Int? get() = saved["favorites.anchor"]

    init { retryRead() }

    fun retryRead() {
        observation?.cancel()
        mutable.update { it.copy(loading = !it.ready && it.items.isEmpty(), readFailure = null) }
        observation = viewModelScope.launch {
            favorites.observeFavorites().collect { result ->
                when (result) {
                    is DataResult.Failure -> mutable.update { it.copy(loading = false, ready = false, readFailure = result.reason) }
                    is DataResult.Success -> {
                        val ids = result.value.map { it.known.id }.toSet()
                        mutable.update { it.copy(items = result.value, loading = false, ready = true, readFailure = null,
                            writing = it.writing.intersect(ids), writeFailures = it.writeFailures.filterKeys(ids::contains),
                            downloadFailures = it.downloadFailures.filterKeys(ids::contains)) }
                        downloads.filterKeys { it !in ids }.values.toList().forEach(Job::cancel)
                        if (visible && refreshOnRead) {
                            refreshOnRead = false
                            refresh()
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        if (!visible || !state.value.ready || batch?.isActive == true) return
        val ids = state.value.items.map { it.known.id }
        val generation = ++refreshGeneration
        batch = viewModelScope.launch {
            mutable.update { it.copy(refreshing = true) }
            try {
                coroutineScope { ids.map { id -> async { download(id) } }.awaitAll() }
            } finally { if (refreshGeneration == generation) mutable.update { it.copy(refreshing = false) } }
        }
    }

    fun retryDownload(id: PokemonId) {
        if (!visible || !state.value.ready || downloads[id]?.isActive == true) return
        viewModelScope.launch { download(id) }
    }

    private suspend fun download(id: PokemonId) = coroutineScope {
        if (state.value.items.none { it.known.id == id } || downloads[id]?.isActive == true) return@coroutineScope
        val job = launch(start = CoroutineStart.LAZY) {
            try {
                mutable.update { it.copy(downloading = it.downloading + id, downloadFailures = it.downloadFailures - id) }
                slots.withPermit {
                    val result = favorites.refresh(id)
                    if (result is DataResult.Failure && state.value.items.any { it.known.id == id })
                        mutable.update { it.copy(downloadFailures = it.downloadFailures + (id to result.reason)) }
                }
            } finally {
                if (downloads[id] === currentCoroutineContext()[Job]) mutable.update { it.copy(downloading = it.downloading - id) }
            }
        }
        downloads[id] = job
        job.start()
        try { job.join() } finally { if (downloads[id] === job) downloads.remove(id) }
    }

    fun remove(id: PokemonId) {
        val current = state.value
        if (!current.ready || id in current.writing || writes[id]?.isActive == true) return
        val item = current.items.firstOrNull { it.known.id == id } ?: return
        mutable.update { it.copy(writing = it.writing + id, writeFailures = it.writeFailures - id) }
        writes[id] = viewModelScope.launch {
            when (val result = favorites.setFavorite(item.known, false)) {
                is DataResult.Failure -> mutable.update { it.copy(writing = it.writing - id, writeFailures = it.writeFailures + (id to result.reason)) }
                is DataResult.Success -> if (state.value.items.none { it.known.id == id })
                    mutable.update { it.copy(writing = it.writing - id) }
            }
        }
    }

    fun onVisible() {
        if (visible) return
        visible = true
        if (state.value.ready) { refreshOnRead = false; refresh() }
        else refreshOnRead = true
    }
    fun onHidden(changingConfiguration: Boolean) {
        if (changingConfiguration) return
        visible = false
        refreshGeneration++
        mutable.update { it.copy(refreshing = false) }
        refreshOnRead = true
        batch?.cancel()
        downloads.values.toList().forEach(Job::cancel)
    }
    fun saveScroll(index: Int, offset: Int, anchor: Int?) {
        saved["favorites.index"] = index.coerceAtLeast(0)
        saved["favorites.offset"] = offset.coerceAtLeast(0)
        saved["favorites.anchor"] = anchor
    }
}
