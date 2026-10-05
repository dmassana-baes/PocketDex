package com.example.pocketdex.feature.detail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.components.*
import com.example.pocketdex.core.designsystem.model.formatPokemonNumber
import com.example.pocketdex.core.domain.model.DataFailure
import com.example.pocketdex.core.domain.model.PokemonLanguage
import com.example.pocketdex.core.domain.model.PokemonStat
import com.example.pocketdex.feature.detail.R
import com.example.pocketdex.core.designsystem.model.PokemonType as VisualType

data class DetailActions(
    val back: () -> Unit,
    val refresh: () -> Unit,
    val favorite: () -> Unit,
    val retryFavorites: () -> Unit,
    val retryMedia: () -> Unit,
    val retryImage: () -> Unit,
)

@Composable
fun DetailScreen(
    state: DetailUiState,
    language: PokemonLanguage,
    image: PokemonImageState,
    list: LazyListState,
    actions: DetailActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        PocketDexAppBar(stringResource(R.string.detail_title), onBack = actions.back, actions = {
            FavoriteButton(state.favorite, actions.favorite,
                enabled = state.favoritesReady && !state.favoriteBusy, busy = state.favoriteBusy)
        })
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val wide = maxWidth >= (600 * LocalDensity.current.fontScale).dp
            LazyColumn(state = list, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                item(key = "detail-content") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (state.loading && state.detail == null) {
                            PocketDexStateContent(PocketDexContentState.Loading, stringResource(R.string.detail_loading))
                        } else if (state.detail == null) {
                            PocketDexStateContent(if (state.notFound) PocketDexContentState.Empty else PocketDexContentState.Error,
                                stringResource(if (state.notFound) R.string.detail_not_found else R.string.detail_no_content))
                        }
                        state.detail?.let { detail ->
                            if (wide) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Identity(state, language, image, Modifier.weight(1f), actions.retryImage)
                                Statistics(state, Modifier.weight(1f).padding(end = 24.dp))
                            } else {
                                Identity(state, language, image, Modifier.fillMaxWidth(), actions.retryImage)
                                Statistics(state, Modifier.padding(horizontal = 24.dp))
                            }
                            if (!detail.statistics.isComplete || detail.summary.types.isEmpty() ||
                                (detail.summary.imageUrl == null && detail.summary.localImageFile == null)) {
                                PocketDexInlineWarning(stringResource(R.string.detail_partial), Modifier.padding(horizontal = 24.dp))
                            }
                        }
                        Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.contentFailure?.let { PocketDexInlineWarning(failureText(it), onRetry = actions.refresh) }
                            state.favoriteFailure?.let { failure ->
                                PocketDexInlineWarning(stringResource(R.string.detail_favorite_error, failureText(failure)),
                                    onRetry = if (state.favoritesReady) actions.favorite else actions.retryFavorites)
                            }
                            state.mediaFailure?.let { PocketDexInlineWarning(stringResource(R.string.detail_media_error), onRetry = actions.retryMedia) }
                            if (state.mediaBusy) Text(stringResource(R.string.detail_media_loading), style = MaterialTheme.typography.bodySmall)
                            PocketDexButton(stringResource(R.string.detail_refresh), actions.refresh,
                                enabled = !state.loading && !state.refreshing && !state.mediaBusy, busy = state.refreshing)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun Identity(state: DetailUiState, language: PokemonLanguage, image: PokemonImageState,
    modifier: Modifier, retryImage: () -> Unit) {
    val summary = requireNotNull(state.detail).summary
    val types = summary.types.map { VisualType.valueOf(it.name) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().background(pokemonHeaderGradient(types.firstOrNull())), contentAlignment = androidx.compose.ui.Alignment.Center) {
            PokemonImage(image, summary.identity.visibleName(language), Modifier.size(260.dp).padding(12.dp))
        }
        Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(formatPokemonNumber(summary.identity.nationalNumber), style = MaterialTheme.typography.titleMedium)
            Text(summary.identity.visibleName(language), Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineLarge)
            // A wrapping layout also supports translated labels and enlarged text.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { PokemonTypeBadge(stringResource(it.labelRes), it) }
            }
            if (types.isEmpty()) Text(stringResource(R.string.detail_types_missing))
            if (image is PokemonImageState.Error) PocketDexButton(stringResource(R.string.detail_retry_image), retryImage,
                style = PocketDexButtonStyle.Secondary, enabled = !state.mediaBusy && !state.refreshing)
        }
    }
}

@Composable
private fun Statistics(state: DetailUiState, modifier: Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.detail_statistics), Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
        PokemonStat.entries.forEach { stat ->
            val label = stringResource(when (stat) {
                PokemonStat.Hp -> R.string.detail_hp
                PokemonStat.Attack -> R.string.detail_attack
                PokemonStat.Defense -> R.string.detail_defense
                PokemonStat.SpecialAttack -> R.string.detail_special_attack
                PokemonStat.SpecialDefense -> R.string.detail_special_defense
                PokemonStat.Speed -> R.string.detail_speed
            })
            PokemonStatRow(label, state.detail?.statistics?.get(stat))
        }
    }
}

@Composable
private fun failureText(failure: DataFailure): String = stringResource(when (failure) {
    DataFailure.Offline -> R.string.detail_offline
    DataFailure.Network -> R.string.detail_network
    DataFailure.Storage -> R.string.detail_storage
    DataFailure.InvalidData -> R.string.detail_invalid
    DataFailure.InsufficientLocalData -> R.string.detail_no_content
})
