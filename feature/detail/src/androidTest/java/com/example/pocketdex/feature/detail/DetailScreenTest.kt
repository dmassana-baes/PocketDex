package com.example.pocketdex.feature.detail

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pocketdex.core.designsystem.components.PokemonImageState
import com.example.pocketdex.core.designsystem.theme.PocketDexTheme
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.detail.presentation.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DetailScreenTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun actions(back: () -> Unit = {}, favorite: () -> Unit = {}) =
        DetailActions(back, {}, favorite, {}, {}, {})

    @Test fun backAndFavoriteRemainAvailableWhileRemoteIsLoading() {
        var returned = false
        var marked = false
        compose.setContent {
            PocketDexTheme {
                DetailScreen(DetailUiState(id = PokemonId(25), favoritesReady = true), PokemonLanguage.English,
                    PokemonImageState.Error, rememberLazyListState(), actions({ returned = true }, { marked = true }))
            }
        }
        compose.onNodeWithContentDescription(context.getString(com.example.pocketdex.core.designsystem.R.string.pd_add_favorite)).performClick()
        compose.onNodeWithContentDescription(context.getString(com.example.pocketdex.core.designsystem.R.string.pd_back)).performClick()
        compose.runOnIdle { assertTrue(returned); assertTrue(marked) }
    }

    @Test fun imageFailureKeepsIdentityTypesAndMissingStatsReachable() {
        val detail = PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(25), "pikachu", 25),
            listOf(PokemonType.Electric, PokemonType.Steel), null), PokemonStatistics(hp = 0, speed = 300))
        compose.setContent {
            PocketDexTheme {
                DetailScreen(DetailUiState(id = PokemonId(25), detail = detail, loading = false), PokemonLanguage.English,
                    PokemonImageState.Error, rememberLazyListState(), actions())
            }
        }
        compose.onNodeWithText("#025").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("pikachu").assertIsDisplayed()
        listOf(com.example.pocketdex.core.designsystem.R.string.pd_type_electric,
            com.example.pocketdex.core.designsystem.R.string.pd_type_steel).forEach { resource ->
            compose.onNodeWithText(context.getString(resource)).performScrollTo().assertIsDisplayed()
        }
        listOf(R.string.detail_hp, R.string.detail_attack, R.string.detail_defense,
            R.string.detail_special_attack, R.string.detail_special_defense, R.string.detail_speed).forEach { resource ->
            compose.onNodeWithText(context.getString(resource)).performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithText("300", substring = true).performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText(context.getString(com.example.pocketdex.core.designsystem.R.string.pd_missing), substring = true)
            .assertCountEquals(4)
    }
}
