package com.example.pocketdex.feature.favorites.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.components.*
import com.example.pocketdex.core.designsystem.model.formatPokemonNumber
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.favorites.R
import com.example.pocketdex.core.designsystem.model.PokemonType as VisualType

data class FavoritesActions(
    val open: (PokemonId) -> Unit,
    val explore: () -> Unit,
    val remove: (PokemonId) -> Unit,
    val retryRead: () -> Unit,
    val refresh: () -> Unit,
    val retryDownload: (PokemonId) -> Unit,
)

/** Networking and image decoding are supplied by the route, keeping screen fixtures deterministic. */
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    language: PokemonLanguage,
    grid: LazyGridState,
    actions: FavoritesActions,
    image: @Composable (FavoritePokemon) -> PokemonImageState,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        PocketDexAppBar(stringResource(R.string.favorites_title))
        val minimum = (156 * LocalDensity.current.fontScale.coerceAtLeast(1f)).dp
        LazyVerticalGrid(GridCells.Adaptive(minimum), state = grid, modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(key = "favorites-header", span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.ready) Text(pluralStringResource(R.plurals.favorites_count, state.items.size, state.items.size),
                        style = MaterialTheme.typography.titleMedium)
                    state.readFailure?.let { PocketDexInlineWarning(stringResource(R.string.favorites_read_error), onRetry = actions.retryRead) }
                    if (state.loading) PocketDexStateContent(PocketDexContentState.Loading, stringResource(R.string.favorites_loading))
                    if (state.ready && state.items.isEmpty()) PocketDexStateContent(PocketDexContentState.Empty,
                        stringResource(R.string.favorites_empty), actionLabel = stringResource(R.string.favorites_explore), onAction = actions.explore)
                    if (state.ready && state.items.isNotEmpty()) PocketDexButton(stringResource(R.string.favorites_refresh), actions.refresh,
                        enabled = !state.refreshing, busy = state.refreshing)
                }
            }
            items(state.items, key = { it.known.id.value }) { favorite ->
                val id = favorite.known.id
                val summary = favorite.known.summary ?: favorite.known.detail?.summary
                val types = summary?.types.orEmpty().map { type ->
                    val visual = VisualType.valueOf(type.name)
                    PokemonTypeVisual(stringResource(visual.labelRes), visual)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PokemonCard(
                        name = summary?.identity?.visibleName(language) ?: stringResource(R.string.favorites_unknown),
                        number = summary?.identity?.nationalNumber?.let(::formatPokemonNumber) ?: stringResource(R.string.favorites_number_missing),
                        types = types, image = image(favorite), favorite = true,
                        onOpen = { actions.open(id) }, onFavoriteClick = { actions.remove(id) },
                        favoriteEnabled = state.ready && id !in state.writing, favoriteBusy = id in state.writing,
                    )
                    if (!favorite.hasCompleteData || !favorite.imageAvailableLocally)
                        Text(stringResource(R.string.favorites_partial), style = MaterialTheme.typography.bodySmall)
                    state.writeFailures[id]?.let { PocketDexInlineWarning(stringResource(R.string.favorites_write_error), onRetry = { actions.remove(id) }) }
                    state.downloadFailures[id]?.let { PocketDexInlineWarning(stringResource(R.string.favorites_download_error), onRetry = { actions.retryDownload(id) }) }
                    if (id in state.downloading) Text(stringResource(R.string.favorites_updating), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
