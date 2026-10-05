package com.example.pocketdex.feature.detail.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.pocketdex.core.domain.model.PokemonId
import com.example.pocketdex.core.domain.model.PokemonLanguage
import java.io.File
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun DetailRoute(id: PokemonId, onBack: () -> Unit, viewModel: DetailViewModel = hiltViewModel()) {
    val observed by viewModel.state.collectAsStateWithLifecycle()
    val state = if (observed.id == id) observed else DetailUiState(id = id)
    val language = if (LocalConfiguration.current.locales[0].language == "es") PokemonLanguage.Spanish else PokemonLanguage.English
    val owner = LocalLifecycleOwner.current
    val activity = LocalContext.current.activity()
    DisposableEffect(owner, activity, viewModel, id) {
        viewModel.open(id)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.onVisible()
            if (event == Lifecycle.Event.ON_STOP) viewModel.onHidden(activity?.isChangingConfigurations == true)
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) viewModel.onVisible()
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    key(id) {
        val list = rememberLazyListState()
        var restored by remember { mutableStateOf(false) }
        LaunchedEffect(state.loading, state.detail) {
            if (!restored && state.detail != null) {
                list.scrollToItem(0, viewModel.scrollOffset)
                restored = true
            }
        }
        LaunchedEffect(list, restored) {
            if (restored) snapshotFlow { list.firstVisibleItemScrollOffset }
                .distinctUntilChanged().collect(viewModel::saveScroll)
        }
        val summary = state.detail?.summary
        var failedLocal by remember(id, summary?.localImageFile, state.imageRevision) { mutableStateOf(false) }
        val model = if (!failedLocal) summary?.localImageFile?.let(::File) ?: summary?.imageUrl else summary?.imageUrl
        key(model, state.imageRevision) {
            val context = LocalContext.current
            val request = remember(context, model) { ImageRequest.Builder(context).data(model).size(512).build() }
            val painter = rememberAsyncImagePainter(request)
            val imageState by painter.state.collectAsState()
            LaunchedEffect(imageState, model) {
                if (model is File && imageState is AsyncImagePainter.State.Error && summary?.imageUrl != null) failedLocal = true
            }
            val image = when {
                model == null -> PokemonImageState.Error
                imageState is AsyncImagePainter.State.Success -> PokemonImageState.Ready(painter)
                imageState is AsyncImagePainter.State.Error -> PokemonImageState.Error
                else -> PokemonImageState.Loading
            }
            DetailScreen(state, language, image, list, DetailActions(onBack, viewModel::refresh,
                viewModel::favorite, viewModel::retryFavorites, { viewModel.completeMedia() }, viewModel::retryImage))
        }
    }
}

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}
