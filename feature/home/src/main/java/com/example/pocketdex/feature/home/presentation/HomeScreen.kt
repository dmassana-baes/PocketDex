package com.example.pocketdex.feature.home.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.example.pocketdex.core.designsystem.components.*
import com.example.pocketdex.core.designsystem.model.formatPokemonNumber
import com.example.pocketdex.core.designsystem.theme.PocketDexColors
import com.example.pocketdex.core.designsystem.theme.PocketDexShapes
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.home.R

data class HomeActions(
    val onText: (String) -> Unit, val onSearch: () -> Unit, val onFilter: () -> Unit,
    val onDraft: (PokemonType?) -> Unit, val onApply: () -> Unit, val onDismissFilter: () -> Unit,
    val onClearType: () -> Unit, val onRefresh: () -> Unit, val onRetryNotice: () -> Unit,
    val onRetryFavorites: () -> Unit, val onFavorite: (PokemonSummary) -> Unit, val onDetail: (PokemonId) -> Unit,
)

internal const val HOME_HEADER_ITEMS = 4

@Composable
fun HomeScreen(state: HomeUiState, items: LazyPagingItems<PokemonSummary>, grid: LazyGridState, actions: HomeActions) {
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp * fontScale), state = grid,
            modifier = Modifier.fillMaxSize().then(if (state.filterOpen) Modifier.clearAndSetSemantics {} else Modifier),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "title", span = { GridItemSpan(maxLineSpan) }) { PocketDexAppBar(stringResource(R.string.home_title)) }
            item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
                PokemonSearchField(state.text, actions.onText, actions.onSearch, Modifier.fillMaxWidth())
            }
            item(key = "filters", span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PocketDexButton(stringResource(R.string.home_filter), actions.onFilter, Modifier.weight(1f),
                            style = PocketDexButtonStyle.Secondary,
                            leadingIcon = { PocketDexIcon(PocketDexIconAsset.Filter, contentDescription = null) })
                        PocketDexButton(stringResource(R.string.home_update), actions.onRefresh,
                            Modifier.weight(1f), style = PocketDexButtonStyle.Ghost, enabled = !state.refreshing)
                    }
                    state.appliedType?.let { type ->
                        val visual = visualType(type)
                        PokemonTypeChip(stringResource(visual.labelRes), visual, true, { actions.onClearType() })
                    }
                }
            }
            item(key = "status", span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.coverage == CatalogCoverage.Partial && state.hasLocalData)
                        Text(stringResource(R.string.home_partial), style = MaterialTheme.typography.bodyMedium)
                    if (state.refreshing && state.hasLocalData) Text(stringResource(R.string.home_updating))
                    state.notice?.let {
                        PocketDexInlineWarning(stringResource(if (state.completionFailureId != null)
                            R.string.home_completion_failed else if (state.favoriteWriteFailureId != null)
                            R.string.home_favorite_write_failed else R.string.home_refresh_failed), onRetry = actions.onRetryNotice)
                    }
                    state.favoriteReadFailure?.let { PocketDexInlineWarning(stringResource(R.string.home_favorite_read_failed), onRetry = actions.onRetryFavorites) }
                    val error = items.loadState.refresh as? LoadState.Error
                    when {
                        items.itemCount == 0 && (state.refreshing && !state.hasLocalData || items.loadState.refresh is LoadState.Loading) ->
                            PocketDexStateContent(PocketDexContentState.Loading, stringResource(R.string.home_loading))
                        items.itemCount == 0 && error != null -> PocketDexStateContent(
                            PocketDexContentState.Error, stringResource(R.string.home_load_failed),
                            actionLabel = stringResource(R.string.home_retry), onAction = items::retry,
                        )
                        items.itemCount == 0 && !state.refreshing -> PocketDexStateContent(PocketDexContentState.Empty,
                            stringResource(if (state.coverage == CatalogCoverage.Partial) R.string.home_empty_partial else R.string.home_empty))
                    }
                }
            }
            items(count = items.itemCount, key = items.itemKey { it.identity.id.value }) { index ->
                val pokemon = items[index]
                if (pokemon == null) {
                    Box(Modifier.fillMaxWidth().height(200.dp).background(PocketDexColors.Gray100, PocketDexShapes.Container))
                } else HomeCard(pokemon, state, actions)
            }
            item(key = "append", span = { GridItemSpan(maxLineSpan) }) {
                when (items.loadState.append) {
                    is LoadState.Loading -> PocketDexStateContent(PocketDexContentState.Loading, stringResource(R.string.home_loading_more))
                    is LoadState.Error -> PocketDexInlineWarning(stringResource(R.string.home_load_more_failed), onRetry = items::retry)
                    else -> Unit
                }
            }
        }
        if (items.loadState.append is LoadState.Error || items.loadState.prepend is LoadState.Error) {
            PocketDexInlineWarning(stringResource(R.string.home_load_more_failed),
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().heightIn(max = 180.dp)
                    .verticalScroll(rememberScrollState()), onRetry = items::retry)
        } else if (items.loadState.append is LoadState.Loading || items.loadState.prepend is LoadState.Loading) {
            Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth().heightIn(max = 180.dp)
                .verticalScroll(rememberScrollState()), color = PocketDexColors.White) {
                PocketDexStateContent(PocketDexContentState.Loading, stringResource(R.string.home_loading_more))
            }
        }
        if (state.filterOpen) TypeFilter(state, actions)
    }
}

@Composable
private fun HomeCard(pokemon: PokemonSummary, state: HomeUiState, actions: HomeActions) {
    val context = LocalContext.current
    val model = pokemon.localImageFile ?: pokemon.imageUrl
    val request = remember(context, model) { ImageRequest.Builder(context).data(model).size(128).build() }
    val painter = rememberAsyncImagePainter(request)
    val image by painter.state.collectAsState()
    val imageState = when (val current = image) {
        is AsyncImagePainter.State.Success -> PokemonImageState.Ready(current.painter)
        is AsyncImagePainter.State.Error -> PokemonImageState.Error
        else -> if (model == null) PokemonImageState.Error else PokemonImageState.Loading
    }
    val types = pokemon.types.map { type ->
        val visual = visualType(type)
        PokemonTypeVisual(stringResource(visual.labelRes), visual)
    }
    PokemonCard(pokemon.identity.visibleName(state.language), formatPokemonNumber(pokemon.identity.nationalNumber),
        types, imageState, pokemon.identity.id in state.favoriteIds, { actions.onDetail(pokemon.identity.id) },
        { actions.onFavorite(pokemon) }, Modifier.fillMaxWidth(), favoriteEnabled = state.favoritesReady,
        favoriteBusy = pokemon.identity.id in state.favoriteBusy)
}

private fun visualType(type: PokemonType): com.example.pocketdex.core.designsystem.model.PokemonType =
    com.example.pocketdex.core.designsystem.model.PokemonType.valueOf(type.name)

/** Overlay remains in the hinge-free host pane, including scrim and Back handling. */
@Composable
private fun BoxScope.TypeFilter(state: HomeUiState, actions: HomeActions) {
    val panelTitle = stringResource(R.string.home_filter)
    BackHandler(onBack = actions.onDismissFilter)
    Box(Modifier.matchParentSize().background(PocketDexColors.Black.copy(alpha = .32f))
        .pointerInput(actions.onDismissFilter) { detectTapGestures { actions.onDismissFilter() } })
    Surface(Modifier.align(Alignment.Center).padding(16.dp).widthIn(max = 560.dp).fillMaxWidth().fillMaxHeight(.9f)
        .semantics { paneTitle = panelTitle; isTraversalGroup = true }
        .pointerInput(Unit) { detectTapGestures { /* Keep taps inside the panel out of the scrim. */ } },
        shape = PocketDexShapes.Container, color = PocketDexColors.White) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PocketDexAppBar(stringResource(R.string.home_filter), actions = {
                IconButton(actions.onDismissFilter) { PocketDexIcon(PocketDexIconAsset.Close, stringResource(R.string.home_close_filter)) }
            })
            PocketDexButton(stringResource(R.string.home_reset_type), { actions.onDraft(null) }, style = PocketDexButtonStyle.Ghost)
            PokemonType.entries.forEach { type ->
                val visual = visualType(type)
                PokemonTypeChip(stringResource(visual.labelRes), visual, state.draftType == type,
                    { selected -> actions.onDraft(if (selected) type else null) }, Modifier.fillMaxWidth())
            }
            PocketDexButton(stringResource(R.string.home_apply), actions.onApply, Modifier.fillMaxWidth())
        }
    }
}
