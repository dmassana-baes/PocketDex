package com.example.pocketdex.feature.detail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.FavoritesRepository
import com.example.pocketdex.core.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val id: PokemonId? = null,
    val detail: PokemonDetail? = null,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val notFound: Boolean = false,
    val contentFailure: DataFailure? = null,
    val favorite: Boolean = false,
    val favoritesReady: Boolean = false,
    val favoriteBusy: Boolean = false,
    val favoriteFailure: DataFailure? = null,
    val mediaBusy: Boolean = false,
    val mediaFailure: DataFailure? = null,
    val imageRevision: Int = 0,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val pokemon: PokemonRepository,
    private val favorites: FavoritesRepository,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val mutable = MutableStateFlow(DetailUiState())
    val state = mutable.asStateFlow()
    private var observation: Job? = null
    private var favoriteObservation: Job? = null
    private var loading: Job? = null
    private var write: Job? = null
    private var media: Job? = null
    private var desiredFavorite: Boolean? = null
    private var pendingCompletion = false
    private var completionRequested = false
    private var visible = false
    private var hasShown = false
    val scrollOffset: Int get() = saved.get<Int>("detail.scroll")?.coerceAtLeast(0) ?: 0

    fun open(id: PokemonId) {
        if (state.value.id == id) return
        observation?.cancel(); favoriteObservation?.cancel(); loading?.cancel(); write?.cancel(); media?.cancel()
        if (saved.get<Int>("detail.id") != id.value) saved["detail.scroll"] = 0
        saved["detail.id"] = id.value
        desiredFavorite = null
        pendingCompletion = false
        completionRequested = false
        visible = false; hasShown = false
        mutable.value = DetailUiState(id = id)
        observation = viewModelScope.launch {
            pokemon.observeDetail(id).collect { result ->
                if (state.value.id != id) return@collect
                when (result) {
                    is DataResult.Success -> result.value?.let { accept(id, it) }
                    is DataResult.Failure -> mutable.update { it.copy(contentFailure = result.reason) }
                }
            }
        }
        retryFavorites()
        loading = viewModelScope.launch {
            when (val cached = pokemon.cachedDetail(id)) {
                is DataResult.Success -> cached.value?.let { accept(id, it) }
                is DataResult.Failure -> if (state.value.id == id) mutable.update { it.copy(contentFailure = cached.reason) }
            }
            if (state.value.id == id) loadRemote(id)
        }
    }

    private fun accept(id: PokemonId, detail: PokemonDetail) {
        if (state.value.id != id) return
        if (detail.summary.identity.id != id) {
            mutable.update { it.copy(contentFailure = DataFailure.InvalidData) }
        } else mutable.update { it.copy(detail = detail, loading = false, notFound = false) }
    }

    fun refresh() {
        val id = state.value.id ?: return
        if (loading?.isActive == true || media?.isActive == true) return
        loading = viewModelScope.launch { loadRemote(id) }
    }

    private suspend fun loadRemote(id: PokemonId) {
        if (state.value.id != id) return
        mutable.update { it.copy(refreshing = true, contentFailure = null) }
        when (val result = pokemon.refresh(id)) {
            is DataResult.Failure -> if (state.value.id == id) mutable.update { it.copy(contentFailure = result.reason) }
            is DataResult.Success -> if (state.value.id == id) {
                val detail = result.value
                if (detail == null) mutable.update {
                    it.copy(notFound = it.detail == null, contentFailure = if (it.detail != null) DataFailure.InvalidData else null)
                } else accept(id, detail)
            }
        }
        if (state.value.id == id) {
            mutable.update { it.copy(loading = false, refreshing = false) }
            if (state.value.favorite || pendingCompletion) completeMedia()
        }
    }

    fun retryFavorites() {
        val id = state.value.id ?: return
        favoriteObservation?.cancel()
        favoriteObservation = viewModelScope.launch {
            favorites.observeIds().collect { result ->
                if (state.value.id != id) return@collect
                when (result) {
                    is DataResult.Failure -> {
                        desiredFavorite = null
                        mutable.update { it.copy(favoritesReady = false, favoriteBusy = false, favoriteFailure = result.reason) }
                    }
                    is DataResult.Success -> {
                        val marked = id in result.value
                        if (!marked && state.value.favorite) {
                            pendingCompletion = false
                            media?.cancel()
                            mutable.update { it.copy(mediaBusy = false, mediaFailure = null) }
                        }
                        if (desiredFavorite == marked) desiredFavorite = null
                        mutable.update { it.copy(favorite = marked, favoritesReady = true,
                            favoriteBusy = desiredFavorite != null, favoriteFailure = null) }
                        if (marked && !completionRequested) completeMedia()
                    }
                }
            }
        }
    }

    fun favorite() {
        val current = state.value
        val id = current.id ?: return
        if (!current.favoritesReady || current.favoriteBusy || write?.isActive == true) return
        val desired = !current.favorite
        desiredFavorite = desired
        mutable.update { it.copy(favoriteBusy = true, favoriteFailure = null) }
        write = viewModelScope.launch {
            val known = KnownPokemon(id, current.detail?.summary, current.detail)
            when (val result = favorites.setFavorite(known, desired)) {
                is DataResult.Failure -> if (state.value.id == id) {
                    desiredFavorite = null
                    mutable.update { it.copy(favoriteBusy = false, favoriteFailure = result.reason) }
                }
                is DataResult.Success -> if (state.value.id == id) {
                    if (state.value.favorite == desired) {
                        desiredFavorite = null
                        mutable.update { it.copy(favoriteBusy = false) }
                    }
                    if (desired) completeMedia()
                    else {
                        pendingCompletion = false
                        media?.cancel()
                        mutable.update { it.copy(mediaBusy = false, mediaFailure = null) }
                    }
                }
            }
        }
    }

    fun completeMedia(force: Boolean = false) {
        val id = state.value.id ?: return
        if (state.value.loading || state.value.refreshing) {
            pendingCompletion = true
            return
        }
        if (media?.isActive == true) return
        pendingCompletion = false
        completionRequested = true
        media = viewModelScope.launch {
            mutable.update { it.copy(mediaBusy = true, mediaFailure = null) }
            val result = if (force) favorites.refresh(id) else favorites.complete(id)
            if (state.value.id == id) mutable.update { it.copy(mediaBusy = false,
                mediaFailure = (result as? DataResult.Failure)?.reason, imageRevision = it.imageRevision + 1) }
        }
    }

    fun retryImage() {
        mutable.update { it.copy(imageRevision = it.imageRevision + 1) }
        if (state.value.favorite) completeMedia(force = true) else refresh()
    }

    fun onVisible() {
        if (visible) return
        visible = true
        if (hasShown) refresh()
        hasShown = true
    }
    fun onHidden(changingConfiguration: Boolean) { if (!changingConfiguration) visible = false }
    fun saveScroll(offset: Int) { saved["detail.scroll"] = offset.coerceAtLeast(0) }
}
