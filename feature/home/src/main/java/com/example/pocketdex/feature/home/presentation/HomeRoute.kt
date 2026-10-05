package com.example.pocketdex.feature.home.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.pocketdex.core.domain.model.PokemonId
import com.example.pocketdex.core.domain.model.PokemonLanguage
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HomeRoute(onDetail: (PokemonId) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items = viewModel.paging.collectAsLazyPagingItems()
    val grid = rememberLazyGridState(viewModel.scrollIndex, viewModel.scrollOffset)
    val configuration = LocalConfiguration.current
    val language = if (configuration.locales[0].language == "es") PokemonLanguage.Spanish else PokemonLanguage.English
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val lifecycle = LocalLifecycleOwner.current
    val activity = LocalContext.current.activity()
    LaunchedEffect(language) { viewModel.setLanguage(language) }
    DisposableEffect(lifecycle, activity, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onVisible()
                Lifecycle.Event.ON_STOP -> viewModel.onHidden(activity?.isChangingConfigurations == true)
                else -> Unit
            }
        }
        lifecycle.lifecycle.addObserver(observer)
        if (lifecycle.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) viewModel.onVisible()
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.queryRevision) { if (state.queryRevision > 0) grid.scrollToItem(0) }
    LaunchedEffect(state.restoringScroll, state.recoveryRevision, state.refreshing, items.itemSnapshotList, items.loadState.refresh) {
        val expected = viewModel.recoveryItem
        val sourceArrived = if (expected != null) items.itemSnapshotList.items.any { it === expected }
            else !state.refreshing && items.itemCount == 0
        if (state.restoringScroll && sourceArrived && items.loadState.refresh is LoadState.NotLoading) {
            val snapshot = items.itemSnapshotList
            val anchor = viewModel.scrollAnchor
            val position = snapshot.items.indexOfFirst { it.identity.id.value == anchor }
            val target = if (position >= 0) snapshot.placeholdersBefore + position + HOME_HEADER_ITEMS else viewModel.scrollIndex
            grid.scrollToItem(target.coerceIn(0, (items.itemCount + HOME_HEADER_ITEMS - 1).coerceAtLeast(0)), viewModel.scrollOffset)
            viewModel.restoredScroll()
        }
    }
    LaunchedEffect(grid, viewModel) {
        snapshotFlow { Pair(Triple(grid.firstVisibleItemIndex, grid.firstVisibleItemScrollOffset,
            grid.layoutInfo.visibleItemsInfo.firstOrNull { it.index == grid.firstVisibleItemIndex }?.key as? Int),
            !state.restoringScroll && !state.refreshing) }
            .distinctUntilChanged().collect { (position, canSave) ->
                if (canSave) viewModel.saveScroll(position.first, position.second, position.third)
            }
    }
    HomeScreen(state, items, grid, HomeActions(
        viewModel::editText, { focus.clearFocus(); keyboard?.hide(); viewModel.searchNow() },
        { focus.clearFocus(); keyboard?.hide(); viewModel.openFilter() },
        viewModel::draftType, viewModel::applyFilter, viewModel::dismissFilter, viewModel::clearType,
        viewModel::refresh, viewModel::retryNotice, viewModel::retryFavorites, viewModel::favorite, onDetail,
    ))
}

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}
