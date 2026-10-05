package com.example.pocketdex.feature.compare.presentation

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.example.pocketdex.core.designsystem.components.PokemonImageState
import com.example.pocketdex.core.domain.model.*
import java.io.File
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
private fun language() = if (LocalConfiguration.current.locales[0].language == "es") PokemonLanguage.Spanish else PokemonLanguage.English

@Composable
fun CompareRoute(viewModel: CompareViewModel, onSelect: (Int, PokemonId?) -> Unit, onResults: (PokemonId, PokemonId) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CompareScreen(state, language(), CompareActions({ slot -> onSelect(slot, viewModel.editSlot(slot)) }, viewModel::remove,
        viewModel::retry, { viewModel.compare()?.let { onResults(it.first, it.second) } }), { CompareImage(it) })
}

@Composable
fun CompareResultsRoute(viewModel: CompareViewModel, left: PokemonId, right: PokemonId, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(left, right) { viewModel.restoreResults(left, right) }
    val valid = state.slots[0].id == left && state.slots[1].id == right
    CompareResultsScreen(if (valid) state else CompareUiState(), language(), onBack, viewModel::retry,
        image = { CompareImage(it) })
}

@Composable
fun SelectorRoute(slot: Int, excluded: PokemonId?, onSelected: (PokemonId) -> Unit, onBack: () -> Unit,
    owner: CompareViewModel, viewModel: SelectorViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = language()
    val list = rememberLazyGridState()
    var restored by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(language) { viewModel.start(language) }
    LaunchedEffect(state.revision) { if (state.revision > 0) { restored = false; list.scrollToItem(0) } }
    LaunchedEffect(state.loading, state.items, state.revision) {
        if (!state.loading && !restored) {
            val anchor = state.items.indexOfFirst { it.identity.id.value == viewModel.scrollAnchor }
            val headers = if (state.previousCursor == null) 1 else 2
            val target = if (anchor >= 0) anchor + headers else viewModel.scrollIndex.coerceAtMost(state.items.size + headers - 1)
            list.scrollToItem(target, viewModel.scrollOffset)
            restored = true
        }
    }
    LaunchedEffect(list, state.loading, restored) {
        if (!state.loading && restored) snapshotFlow { Triple(list.firstVisibleItemIndex, list.firstVisibleItemScrollOffset,
            list.layoutInfo.visibleItemsInfo.firstOrNull { it.index == list.firstVisibleItemIndex }?.key as? Int) }
            .distinctUntilChanged().collect { viewModel.saveScroll(it.first, it.second, it.third) }
    }
    SelectorScreen(state, language, list, { owner.cancelEditing(); onBack() }, viewModel::editText,
        { focus.clearFocus(); keyboard?.hide(); viewModel.searchNow() }, viewModel::retry, viewModel::loadMore, viewModel::loadPrevious,
        { id -> if (viewModel.pick(id, excluded)) { focus.clearFocus(); keyboard?.hide(); onSelected(id) } }, { CompareImage(it) })
}

@Composable
private fun CompareImage(summary: PokemonSummary?): PokemonImageState {
    val model = summary?.localImageFile?.let(::File) ?: summary?.imageUrl
    val context = LocalContext.current
    val request = remember(context, model) { ImageRequest.Builder(context).data(model).size(256).build() }
    val painter = rememberAsyncImagePainter(request)
    val state by painter.state.collectAsState()
    return when {
        model == null -> PokemonImageState.Error
        state is AsyncImagePainter.State.Success -> PokemonImageState.Ready(painter)
        state is AsyncImagePainter.State.Error -> PokemonImageState.Error
        else -> PokemonImageState.Loading
    }
}
