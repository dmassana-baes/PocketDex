package com.example.pocketdex.feature.compare

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pocketdex.core.designsystem.components.PokemonImageState
import com.example.pocketdex.core.designsystem.theme.PocketDexTheme
import com.example.pocketdex.core.domain.compare.comparePokemon
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.compare.presentation.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CompareScreenTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun detail(id: Int) = PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(id), "pokemon-$id", id), listOf(PokemonType.Fire), null),
        PokemonStatistics(0, 300, 2, 3, 4, 5))

    @Test fun emptyPairDisablesCompareAndChooseIsAccessible() {
        var chosen: Int? = null
        compose.setContent { PocketDexTheme { CompareScreen(CompareUiState(), PokemonLanguage.English,
            CompareActions({ chosen = it }, {}, {}, {}), { PokemonImageState.Error }) } }
        compose.onNodeWithText(context.getString(R.string.compare_action)).performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText(context.getString(R.string.compare_choose, 1)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(0, chosen) }
    }
    @Test fun resultsShowSixAccessibleRowsAndTieWithBack() {
        var back = false
        val left = detail(1); val right = detail(2)
        val state = CompareUiState(listOf(CompareSlot(PokemonId(1), left), CompareSlot(PokemonId(2), right)), result = comparePokemon(left, right))
        compose.setContent { PocketDexTheme { CompareResultsScreen(state, PokemonLanguage.English, { back = true }, {}) } }
        val labels = listOf(R.string.compare_hp, R.string.compare_attack, R.string.compare_defense,
            R.string.compare_special_attack, R.string.compare_special_defense, R.string.compare_speed)
        labels.forEach { label ->
            compose.onNodeWithText(context.getString(label)).performScrollTo().assertIsDisplayed()
        }
        val attack = context.getString(R.string.compare_attack)
        compose.onNodeWithContentDescription("$attack, pokemon-1: 300").performScrollTo().assertExists()
        compose.onAllNodesWithText(context.getString(com.example.pocketdex.core.designsystem.R.string.pd_tie)).assertCountEquals(6)
        compose.onNodeWithContentDescription(context.getString(com.example.pocketdex.core.designsystem.R.string.pd_back)).performClick()
        compose.runOnIdle { assertTrue(back) }
    }

    @Test fun occupiedSlotsExposeSeparateAccessibleReplaceAndRemoveActions() {
        var chosen: Int? = null
        var removed: Int? = null
        val left = detail(1); val right = detail(2)
        val state = CompareUiState(listOf(CompareSlot(PokemonId(1), left), CompareSlot(PokemonId(2), right)))
        compose.setContent { PocketDexTheme { CompareScreen(state, PokemonLanguage.English,
            CompareActions({ chosen = it }, { removed = it }, {}, {}), { PokemonImageState.Error }) } }
        compose.onNodeWithContentDescription(context.getString(R.string.compare_replace, 1))
            .performScrollTo().performClick()
        compose.onNodeWithContentDescription(context.getString(R.string.compare_remove, 2, "pokemon-2"))
            .performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(0, chosen)
            assertEquals(1, removed)
        }
    }
}
