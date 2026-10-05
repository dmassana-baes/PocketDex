package com.example.pocketdex.feature.home.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.cachedIn
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.CatalogRepository
import com.example.pocketdex.core.domain.repository.FavoritesRepository
import com.example.pocketdex.feature.home.paging.HomePagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val text: String = "", val appliedType: PokemonType? = null, val draftType: PokemonType? = null,
    val filterOpen: Boolean = false, val language: PokemonLanguage = PokemonLanguage.English,
    val favoriteIds: Set<PokemonId> = emptySet(), val favoriteBusy: Set<PokemonId> = emptySet(),
    val favoritesReady: Boolean = false, val favoriteReadFailure: DataFailure? = null,
    val notice: DataFailure? = null, val completionFailureId: PokemonId? = null,
    val coverage: CatalogCoverage = CatalogCoverage.Partial, val refreshing: Boolean = true,
    val queryRevision: Int = 0,
    val hasLocalData: Boolean = false, val restoringScroll: Boolean = false,
    val favoriteWriteFailureId: PokemonId? = null,
    val recoveryRevision: Int = 0,
)

private data class QueryRequest(val query: CatalogQuery, val refresh: Int = 0)
private val localLoadStates = LoadStates(LoadState.NotLoading(true), LoadState.NotLoading(true), LoadState.NotLoading(true))

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository, private val favorites: FavoritesRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val initialType = savedState.get<String>("type")?.let(PokemonType::valueOf)
    private val initial = HomeUiState(
        text = savedState["text"] ?: "", appliedType = initialType,
        draftType = savedState.get<String>("draft")?.let(PokemonType::valueOf), filterOpen = savedState["filterOpen"] ?: false,
        language = savedState.get<String>("language")?.let(PokemonLanguage::valueOf) ?: PokemonLanguage.English,
        restoringScroll = (savedState.get<Int>("scrollIndex") ?: 0) > 0,
    )
    private val mutableState = MutableStateFlow(initial)
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()
    private val request = MutableStateFlow(QueryRequest(CatalogQuery(PokemonSearch.parse(initial.text), initialType, initial.language)))
    private val favoriteRetry = MutableStateFlow(0)
    private val pendingFavorites = mutableMapOf<PokemonId, Boolean>()
    private val completionJobs = mutableMapOf<PokemonId, Job>()
    private var debounceJob: Job? = null
    private var visible = false
    private var hasStarted = false
    private var displayedQuery: CatalogQuery? = null
    private var failedWrite: PokemonSummary? = null
    var recoveryItem: PokemonSummary? = null
        private set

    val scrollIndex: Int get() = savedState["scrollIndex"] ?: 0
    val scrollOffset: Int get() = savedState["scrollOffset"] ?: 0
    val scrollAnchor: Int? get() = savedState["scrollAnchor"]

    val paging: Flow<PagingData<PokemonSummary>> = request.flatMapLatest { current ->
        flow {
            failedWrite = null
            mutableState.update { it.copy(refreshing = true, notice = null, completionFailureId = null, favoriteWriteFailureId = null) }
            val anchor = scrollAnchor?.let(::PokemonId)
            if (displayedQuery != current.query) {
                mutableState.update { it.copy(hasLocalData = false) }
                if (displayedQuery != null) emit(PagingData.empty())
                displayedQuery = current.query
            }
            val cached = repository.cached(current.query)
            val local = (cached as? DataResult.Success)?.value
            if (local != null && local.totalStored > 0) {
                mutableState.update { it.copy(coverage = local.coverage, hasLocalData = true) }
                prepareRecovery(local.items.firstOrNull())
                emit(PagingData.from(local.items, sourceLoadStates = localLoadStates))
            }
            val loadSize = 30
            val first = repository.page(current.query, PageRequest(size = loadSize, anchorId = anchor))
            mutableState.update { it.copy(refreshing = false) }
            if (first is DataResult.Failure && local != null && local.totalStored > 0) {
                mutableState.update { it.copy(notice = first.reason, coverage = local.coverage) }
                // Keep the local PagingData even for an empty partial query. Retry refresh is explicit.
            } else {
                if (first is DataResult.Success) {
                    prepareRecovery(first.value.items.firstOrNull())
                    mutableState.update { it.copy(coverage = first.value.coverage) }
                }
                emitAll(Pager(PagingConfig(pageSize = 30, initialLoadSize = loadSize, prefetchDistance = 6,
                    enablePlaceholders = true, jumpThreshold = 90)) {
                    HomePagingSource(repository, current.query, first) { page, initialLoad ->
                        if (request.value == current) {
                            if (initialLoad && state.value.restoringScroll && recoveryItem == null)
                                prepareRecovery(page.items.firstOrNull())
                            mutableState.update { it.copy(coverage = page.coverage) }
                        }
                    }
                }.flow)
            }
        }
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            favoriteRetry.flatMapLatest { favorites.observeIds() }.collect { result ->
                when (result) {
                    is DataResult.Failure -> mutableState.update { it.copy(favoritesReady = false, favoriteReadFailure = result.reason) }
                    is DataResult.Success -> {
                        val confirmed = pendingFavorites.filter { (id, desired) -> (id in result.value) == desired }.keys
                        confirmed.forEach { pendingFavorites.remove(it) }
                        mutableState.update { it.copy(favoriteIds = result.value.toSet(), favoriteBusy = it.favoriteBusy - confirmed,
                            favoritesReady = true, favoriteReadFailure = null) }
                    }
                }
            }
        }
    }

    fun onVisible() {
        if (visible) return
        visible = true
        if (hasStarted && !state.value.refreshing) refresh()
        hasStarted = true
    }

    fun onHidden(changingConfiguration: Boolean) { if (!changingConfiguration) visible = false }

    fun setLanguage(language: PokemonLanguage) {
        if (state.value.language == language) return
        mutableState.update { it.copy(language = language) }
        savedState["language"] = language.name
        applyQuery()
    }

    fun editText(text: String) {
        savedState["text"] = text
        mutableState.update { it.copy(text = text) }
        debounceJob?.cancel()
        if (text.isEmpty()) applyQuery()
        else debounceJob = viewModelScope.launch { delay(300); applyQuery() }
    }

    fun searchNow() { debounceJob?.cancel(); applyQuery() }

    fun openFilter() {
        mutableState.update { it.copy(filterOpen = true, draftType = it.appliedType) }
        saveFilter()
    }
    fun draftType(type: PokemonType?) { mutableState.update { it.copy(draftType = type) }; saveFilter() }
    fun dismissFilter() {
        mutableState.update { it.copy(filterOpen = false, draftType = it.appliedType) }; saveFilter()
    }
    fun applyFilter() {
        mutableState.update { it.copy(filterOpen = false, appliedType = it.draftType) }
        savedState["type"] = state.value.appliedType?.name
        saveFilter(); applyQuery()
    }
    fun clearType() {
        mutableState.update { it.copy(appliedType = null) }
        savedState["type"] = null
        applyQuery()
    }

    private fun saveFilter() {
        savedState["draft"] = state.value.draftType?.name
        savedState["filterOpen"] = state.value.filterOpen
    }

    private fun applyQuery() {
        val next = CatalogQuery(PokemonSearch.parse(state.value.text), state.value.appliedType, state.value.language)
        if (request.value.query == next) return
        // Changing locale preserves context; changing search/type starts at the first result.
        val criteriaChanged = request.value.query.search != next.search || request.value.query.type != next.type
        if (criteriaChanged) {
            saveScroll(0, 0, null)
            mutableState.update { it.copy(queryRevision = it.queryRevision + 1, restoringScroll = false, hasLocalData = false) }
        }
        mutableState.update { it.copy(refreshing = true) }
        request.value = QueryRequest(next)
    }

    fun saveScroll(index: Int, offset: Int, anchorId: Int?) {
        savedState["scrollIndex"] = index; savedState["scrollOffset"] = offset; savedState["scrollAnchor"] = anchorId
    }
    private fun prepareRecovery(first: PokemonSummary?) {
        recoveryItem = first
        mutableState.update { it.copy(restoringScroll = scrollIndex > 0, recoveryRevision = it.recoveryRevision + 1) }
    }
    fun restoredScroll() { mutableState.update { it.copy(restoringScroll = false) } }
    fun refresh() {
        if (state.value.refreshing) return
        mutableState.update { it.copy(refreshing = true) }
        request.value = request.value.copy(refresh = request.value.refresh + 1)
    }
    fun retryFavorites() { favoriteRetry.value++ }
    fun dismissNotice() { mutableState.update { it.copy(notice = null, completionFailureId = null) } }
    fun retryNotice() {
        val write = failedWrite
        when {
            state.value.completionFailureId != null -> retryCompletion()
            write != null -> favorite(write)
            else -> refresh()
        }
    }

    fun favorite(pokemon: PokemonSummary) {
        val id = pokemon.identity.id
        if (!state.value.favoritesReady || id in state.value.favoriteBusy) return
        val desired = id !in state.value.favoriteIds
        pendingFavorites[id] = desired
        failedWrite = null
        mutableState.update { it.copy(favoriteBusy = it.favoriteBusy + id, favoriteWriteFailureId = null,
            notice = null, completionFailureId = null) }
        viewModelScope.launch {
            when (val result = favorites.setFavorite(KnownPokemon(id, pokemon), desired)) {
                is DataResult.Failure -> {
                    pendingFavorites.remove(id)
                    failedWrite = pokemon
                    mutableState.update { it.copy(favoriteBusy = it.favoriteBusy - id, notice = result.reason, favoriteWriteFailureId = id) }
                }
                is DataResult.Success -> if (desired) launchCompletion(id)
            }
        }
    }

    fun retryCompletion() { state.value.completionFailureId?.let(::launchCompletion) }
    private fun launchCompletion(id: PokemonId) {
        if (completionJobs[id]?.isActive == true) return
        val job = viewModelScope.launch { completeFavorite(id) }
        completionJobs[id] = job
        job.invokeOnCompletion { if (completionJobs[id] === job) completionJobs.remove(id) }
    }
    private suspend fun completeFavorite(id: PokemonId) {
        val result = favorites.complete(id)
        if (result is DataResult.Failure && id in state.value.favoriteIds)
            mutableState.update { it.copy(notice = result.reason, completionFailureId = id) }
    }
}
