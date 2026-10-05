package com.example.pocketdex.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.R
import com.example.pocketdex.core.designsystem.components.*
import com.example.pocketdex.core.designsystem.model.ComparisonResult
import com.example.pocketdex.core.designsystem.model.NavigationPresentation
import com.example.pocketdex.core.designsystem.model.PokemonType
import com.example.pocketdex.core.designsystem.model.formatPokemonNumber
import com.example.pocketdex.core.designsystem.theme.PocketDexTheme

/** Offline visual fixtures only. No sprite, IO, navigation, DI or application state. */
@Preview(name = "English / narrow", widthDp = 360, heightDp = 800, locale = "en")
@Preview(name = "Español / landscape", widthDp = 640, heightDp = 360, locale = "es")
@Preview(name = "Español / large text", widthDp = 360, heightDp = 800, locale = "es", fontScale = 2f)
@Preview(name = "Night system / light app", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Unsupported language / English fallback", widthDp = 360, heightDp = 800, locale = "fr")
@Composable
private fun DesignSystemGallery() {
    PocketDexTheme {
        Surface {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PocketDexAppBar(stringResource(R.string.pd_home), onBack = {})
                PocketDexButton(stringResource(R.string.pd_retry), onClick = {})
                PocketDexButton(stringResource(R.string.pd_retry), onClick = {}, busy = true)
                PocketDexButton(stringResource(R.string.pd_retry), onClick = {}, enabled = false,
                    style = PocketDexButtonStyle.Secondary)
                PocketDexButton(stringResource(R.string.pd_filters), onClick = {}, style = PocketDexButtonStyle.Ghost)
                PokemonSearchField("", {}, {}, Modifier.fillMaxWidth())
                PokemonSearchField("Pikachu", {}, {}, Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FavoriteButton(false, {})
                    FavoriteButton(true, {})
                    FavoriteButton(true, {}, busy = true)
                    FavoriteButton(false, {}, enabled = false)
                }
                PokemonCard(
                    name = "Bulbasaur — a deliberately long preview label", number = formatPokemonNumber(1),
                    types = listOf(
                        PokemonTypeVisual(stringResource(PokemonType.Grass.labelRes), PokemonType.Grass),
                        PokemonTypeVisual(stringResource(PokemonType.Poison.labelRes), PokemonType.Poison),
                    ), image = PokemonImageState.Error, favorite = true, onOpen = {}, onFavoriteClick = {},
                )
                PokemonTypeBadge("Unknown visual style", null)
                PokemonType.entries.forEach { type ->
                    PokemonTypeChip(stringResource(type.labelRes), type, selected = type == PokemonType.Grass, onSelectedChange = {})
                }
                PokemonStatRow(stringResource(R.string.pd_attack), 100)
                PokemonStatRow(stringResource(R.string.pd_hp), 300)
                PokemonStatRow(stringResource(R.string.pd_speed), null)
                PokemonComparisonRow(stringResource(R.string.pd_defense), 80, 80, ComparisonResult.Tie)
                PokemonComparisonRow(stringResource(R.string.pd_hp), null, 80, ComparisonResult.Unavailable)
                PocketDexStateContent(PocketDexContentState.Loading, stringResource(R.string.pd_loading))
                PocketDexStateContent(PocketDexContentState.Empty, stringResource(R.string.pd_empty))
                PocketDexStateContent(PocketDexContentState.Error, stringResource(R.string.pd_error),
                    actionLabel = stringResource(R.string.pd_retry), onAction = {})
                PocketDexInlineWarning(stringResource(R.string.pd_error), onRetry = {})
            }
        }
    }
}

@Preview(name = "Wide window / rail", widthDp = 840, heightDp = 400, locale = "es")
@Composable
private fun RailPreview() {
    PocketDexTheme {
        Surface {
            Row {
                PocketDexNavigation(
                    previewNavigationItems("home"), "home", NavigationPresentation.SideRail, {}, Modifier.width(120.dp),
                )
                Text(stringResource(R.string.pd_home), Modifier.padding(24.dp), style = MaterialTheme.typography.displaySmall)
            }
        }
    }
}

@Preview(name = "Bottom navigation", widthDp = 360, heightDp = 160, locale = "en")
@Composable
private fun BottomNavigationPreview() {
    PocketDexTheme {
        PocketDexNavigation(previewNavigationItems("favorites"), "favorites", NavigationPresentation.BottomBar, {})
    }
}

// The host supplies icons from its controlled selection; the design system has no route model.
@Composable
private fun previewNavigationItems(selectedId: String) = listOf(
    PocketDexNavigationItem("home", stringResource(R.string.pd_home)) {
        PocketDexIcon(if (selectedId == "home") PocketDexIconAsset.HomeFilled else PocketDexIconAsset.Home, null)
    },
    PocketDexNavigationItem("compare", stringResource(R.string.pd_comparator)) {
        PocketDexIcon(PocketDexIconAsset.Comparator, null)
    },
    PocketDexNavigationItem("quiz", stringResource(R.string.pd_quiz)) {
        PocketDexIcon(if (selectedId == "quiz") PocketDexIconAsset.QuizFilled else PocketDexIconAsset.Quiz, null)
    },
    PocketDexNavigationItem("favorites", stringResource(R.string.pd_favorites)) {
        PocketDexIcon(if (selectedId == "favorites") PocketDexIconAsset.HeartFilled else PocketDexIconAsset.Heart, null)
    },
)
