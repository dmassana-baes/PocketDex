package com.example.pocketdex.feature.compare.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

data class SelectorUiState(
    val text: String = "", val language: PokemonLanguage = PokemonLanguage.English,
    val items: List<PokemonSummary> = emptyList(), val loading: Boolean = true,
    val failure: DataFailure? = null, val coverage: CatalogCoverage = CatalogCoverage.Partial,
    val nextCursor: String? = null, val previousCursor: String? = null,
    val duplicate: Boolean = false, val revision: Int = 0,
)

@HiltViewModel
class SelectorViewModel @Inject constructor(private val catalog: CatalogRepository, private val saved: SavedStateHandle) : ViewModel() {
    private val mutable = MutableStateFlow(SelectorUiState(text = saved["selector.text"] ?: ""))
    val state = mutable.asStateFlow()
    private var started = false
    private var picked = false
    private var generation = 0
    private var job: Job? = null
    private var debounce: Job? = null
    private var activeQuery: CatalogQuery? = null
    private var lastCursor: String? = null
    private var lastPrevious = false
    private var lastAnchor: PokemonId? = null
    val scrollIndex: Int get() = saved.get<Int>("selector.index")?.coerceAtLeast(0) ?: 0
    val scrollOffset: Int get() = saved.get<Int>("selector.offset")?.coerceAtLeast(0) ?: 0
    val scrollAnchor: Int? get() = saved["selector.anchor"]
    fun start(language: PokemonLanguage) {
        if (started && state.value.language == language) return
        val first = !started
        started = true
        mutable.update { it.copy(language = language) }
        search(resetScroll = !first)
    }
    fun editText(text: String) {
        saved["selector.text"] = text
        mutable.update { it.copy(text = text, duplicate = false) }
        debounce?.cancel()
        if (text.isBlank()) searchNow()
        else debounce = viewModelScope.launch { delay(300); searchNow() }
    }
    fun searchNow() {
        search(resetScroll = true)
    }
    private fun search(resetScroll: Boolean) {
        debounce?.cancel()
        val query = CatalogQuery(PokemonSearch.parse(state.value.text), language = state.value.language)
        if (query == activeQuery) return
        job?.cancel()
        val version = ++generation
        activeQuery = query
        mutable.update { it.copy(items = emptyList(), loading = true, failure = null, nextCursor = null, previousCursor = null,
            revision = if (resetScroll) it.revision + 1 else it.revision) }
        if (resetScroll) { saved["selector.index"] = 0; saved["selector.offset"] = 0; saved["selector.anchor"] = null }
        job = viewModelScope.launch {
            if (query.search == PokemonSearch.NoMatch) {
                mutable.update { it.copy(loading = false, coverage = CatalogCoverage.Complete) }; return@launch
            }
            when (val cached = catalog.cached(query)) {
                is DataResult.Success -> if (generation == version) mutable.update { it.copy(items = cached.value.items, coverage = cached.value.coverage) }
                is DataResult.Failure -> if (generation == version) mutable.update { it.copy(failure = cached.reason) }
            }
            if (generation == version) page(query, version, null, anchor = scrollAnchor?.takeIf { it > 0 }?.let(::PokemonId))
        }
    }
    fun loadMore() {
        if (job?.isActive == true) return
        val cursor = state.value.nextCursor ?: return
        val query = activeQuery ?: return
        val version = generation
        job = viewModelScope.launch { page(query, version, cursor) }
    }
    fun loadPrevious() {
        if (job?.isActive == true) return
        val cursor = state.value.previousCursor ?: return
        val query = activeQuery ?: return
        val version = generation
        job = viewModelScope.launch { page(query, version, cursor, previous = true) }
    }
    fun retry() {
        if (job?.isActive == true) return
        val query = activeQuery ?: return
        val version = generation
        job = viewModelScope.launch { page(query, version, lastCursor, lastPrevious, lastAnchor) }
    }
    private suspend fun page(query: CatalogQuery, version: Int, cursor: String?, previous: Boolean = false, anchor: PokemonId? = null) {
        lastCursor = cursor; lastPrevious = previous; lastAnchor = anchor
        mutable.update { it.copy(loading = true, failure = null) }
        when (val result = catalog.page(query, PageRequest(cursor = cursor, anchorId = anchor))) {
            is DataResult.Failure -> if (generation == version) mutable.update { it.copy(loading = false, failure = result.reason) }
            is DataResult.Success -> if (generation == version) mutable.update { it.copy(loading = false, failure = null,
                items = (if (cursor == null) result.value.items else if (previous) result.value.items + it.items else it.items + result.value.items)
                    .distinctBy { pokemon -> pokemon.identity.id },
                coverage = result.value.coverage,
                nextCursor = if (previous) it.nextCursor else result.value.nextCursor,
                previousCursor = if (cursor == null || previous) result.value.previousCursor else it.previousCursor) }
        }
    }
    fun pick(id: PokemonId, excluded: PokemonId?): Boolean {
        if (picked) return false
        if (id == excluded) { mutable.update { it.copy(duplicate = true) }; return false }
        picked = true
        return true
    }
    fun saveScroll(index: Int, offset: Int, anchor: Int?) {
        saved["selector.index"] = index; saved["selector.offset"] = offset; saved["selector.anchor"] = anchor
    }
}
