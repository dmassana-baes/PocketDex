package com.example.pocketdex.feature.favorites.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.example.pocketdex.core.designsystem.components.PokemonImageState
import com.example.pocketdex.core.domain.model.FavoritePokemon
import com.example.pocketdex.core.domain.model.PokemonId
import com.example.pocketdex.core.domain.model.PokemonLanguage
import java.io.File
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun FavoritesRoute(onDetail: (PokemonId) -> Unit, onExplore: () -> Unit, viewModel: FavoritesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = if (LocalConfiguration.current.locales[0].language == "es") PokemonLanguage.Spanish else PokemonLanguage.English
    val grid = rememberLazyGridState()
    var restored by remember { mutableStateOf(false) }
    val owner = LocalLifecycleOwner.current
    val activity = LocalContext.current.activity()
    DisposableEffect(owner, activity, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.onVisible()
            if (event == Lifecycle.Event.ON_STOP) viewModel.onHidden(activity?.isChangingConfigurations == true)
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) viewModel.onVisible()
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.ready, state.items) {
        if (state.ready && !restored) {
            val anchor = state.items.indexOfFirst { it.known.id.value == viewModel.scrollAnchor }
            val index = if (anchor >= 0) anchor + 1 else viewModel.scrollIndex.coerceAtMost(state.items.size)
            grid.scrollToItem(index, viewModel.scrollOffset)
            restored = true
        }
    }
    LaunchedEffect(grid, restored, state.ready) {
        if (restored && state.ready) snapshotFlow { Triple(grid.firstVisibleItemIndex, grid.firstVisibleItemScrollOffset,
            grid.layoutInfo.visibleItemsInfo.firstOrNull { it.index == grid.firstVisibleItemIndex }?.key as? Int) }
            .distinctUntilChanged().collect { viewModel.saveScroll(it.first, it.second, it.third) }
    }
    FavoritesScreen(state, language, grid, FavoritesActions(onDetail, onExplore, viewModel::remove,
        viewModel::retryRead, viewModel::refresh, viewModel::retryDownload), image = { favorite -> FavoriteImage(favorite) })
}

@Composable
private fun FavoriteImage(favorite: FavoritePokemon): PokemonImageState {
    val summary = favorite.known.summary ?: favorite.known.detail?.summary
    var failedLocal by remember(summary?.localImageFile) { mutableStateOf(false) }
    val model = if (!failedLocal) summary?.localImageFile?.let(::File) ?: summary?.imageUrl else summary?.imageUrl
    val context = LocalContext.current
    val request = remember(context, model) { ImageRequest.Builder(context).data(model).size(256).build() }
    val painter = rememberAsyncImagePainter(request)
    val state by painter.state.collectAsState()
    LaunchedEffect(state, model) {
        if (model is File && state is AsyncImagePainter.State.Error && summary?.imageUrl != null) failedLocal = true
    }
    return when {
        model == null -> PokemonImageState.Error
        state is AsyncImagePainter.State.Success -> PokemonImageState.Ready(painter)
        state is AsyncImagePainter.State.Error -> PokemonImageState.Error
        else -> PokemonImageState.Loading
    }
}

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}
