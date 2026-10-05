package com.example.pocketdex.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pocketdex.core.designsystem.components.*
import com.example.pocketdex.core.designsystem.model.ComparisonResult
import com.example.pocketdex.core.designsystem.model.NavigationPresentation
import com.example.pocketdex.core.designsystem.model.PokemonType
import com.example.pocketdex.core.designsystem.theme.PocketDexTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DesignSystemInteractionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun disabledAndBusyActionsNeverInvokeCallbacks() {
        var clicks = 0
        compose.setContent {
            PocketDexTheme {
                Column {
                    PocketDexButton("Disabled", { clicks++ }, enabled = false)
                    PocketDexButton("Busy", { clicks++ }, busy = true)
                    PocketDexButton("Ready", { clicks++ })
                }
            }
        }
        compose.onNodeWithText("Disabled").assertIsNotEnabled().performClick()
        compose.onNodeWithText("Busy").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(0, clicks) }
        compose.onNodeWithText("Ready").performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    fun favoriteDoesNotOpenCardAndReflectsControlledState() {
        var opened = 0
        var favorites = 0
        val checked = mutableStateOf(false)
        compose.setContent {
            PocketDexTheme {
                PokemonCard("Bulbasaur", "#001", emptyList(), PokemonImageState.Error, checked.value,
                    onOpen = { opened++ }, onFavoriteClick = { favorites++; checked.value = true })
            }
        }
        // Resource IDs, rather than hard-coded locale text, identify the callback's semantics.
        compose.onNode(isSelectable()).assertIsNotSelected().performClick()
        compose.onNode(isSelectable()).assertIsSelected()
        compose.runOnIdle { assertEquals(0, opened); assertEquals(1, favorites) }
        compose.onNodeWithText("Bulbasaur").performClick()
        compose.runOnIdle { assertEquals(1, opened); assertEquals(1, favorites) }
    }

    @Test
    fun chipIsControlledAndDisabledChipCannotChangeSelection() {
        var changes = 0
        val selected = mutableStateOf(false)
        compose.setContent {
            PocketDexTheme {
                Column {
                    PokemonTypeChip("Grass", PokemonType.Grass, selected.value, { selected.value = it; changes++ })
                    PokemonTypeChip("Water", PokemonType.Water, false, { changes++ }, enabled = false)
                }
            }
        }
        compose.onNodeWithText("Grass").assertIsOff().performClick().assertIsOn()
        compose.onNodeWithText("Water").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(1, changes) }
    }

    @Test
    fun silhouetteDoesNotExposeTheCorrectName() {
        val hidden = mutableStateOf(true)
        compose.setContent {
            PocketDexTheme {
                PokemonImage(PokemonImageState.Ready(ColorPainter(Color.Black)), "Pikachu", Modifier.size(100.dp), hidden.value)
            }
        }
        compose.onNodeWithContentDescription("Pikachu").assertDoesNotExist()
        compose.runOnIdle { hidden.value = false }
        compose.onNodeWithContentDescription("Pikachu").assertExists()
    }

    @Test
    fun missingStatIsNotAnnouncedAsZeroAndOverflowKeepsItsNumber() {
        compose.setContent {
            PocketDexTheme {
                Column {
                    PokemonStatRow("HP", null, Modifier.testTag("missing"))
                    PokemonStatRow("Attack", 300)
                    PokemonComparisonRow("Speed", 80, 80, ComparisonResult.Tie)
                }
            }
        }
        compose.onNodeWithText("0", substring = false).assertDoesNotExist()
        compose.onNodeWithText("300").assertExists()
        compose.onNodeWithTag("missing").assertExists()
    }

    @Test
    fun changingNavigationPresentationKeepsSelectionInTheHost() {
        val presentation = mutableStateOf(NavigationPresentation.BottomBar)
        val selected = mutableStateOf("home")
        compose.setContent {
            PocketDexTheme {
                PocketDexNavigation(
                    listOf(PocketDexNavigationItem("home", "Home") {}, PocketDexNavigationItem("quiz", "Quiz") {}),
                    selected.value, presentation.value, { selected.value = it },
                )
            }
        }
        compose.onNodeWithText("Quiz").performClick().assertIsSelected()
        compose.runOnIdle { presentation.value = NavigationPresentation.SideRail }
        compose.onNodeWithText("Quiz").assertIsSelected()
    }

    @Test
    fun comparisonAnnouncesStatSideAndValueIncludingMissingAndTie() {
        var missing = ""
        compose.setContent {
            PocketDexTheme {
                missing = stringResource(R.string.pd_missing)
                Column {
                    PokemonComparisonRow("HP", null, 80, ComparisonResult.Unavailable,
                        leftLabel = "Pikachu", rightLabel = "Bulbasaur")
                    PokemonComparisonRow("Speed", 50, 50, ComparisonResult.Tie,
                        leftLabel = "Pikachu", rightLabel = "Bulbasaur")
                }
            }
        }
        compose.onNodeWithContentDescription("HP, Pikachu: $missing").assertExists()
        compose.onNodeWithContentDescription("HP, Bulbasaur: 80").assertExists()
        compose.onNodeWithContentDescription("Speed, Pikachu: 50").assertExists()
        compose.onNodeWithContentDescription("Speed, Bulbasaur: 50").assertExists()
    }

    @Test
    fun busyFavoritePreservesSelectionAndCannotInvokeTheAction() {
        var clicks = 0
        compose.setContent {
            PocketDexTheme {
                FavoriteButton(true, { clicks++ }, Modifier.testTag("favorite"), busy = true)
            }
        }
        compose.onNodeWithTag("favorite").assertIsSelected().assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(0, clicks) }
    }

    @Test
    fun searchUsesHostTextAndExplicitImeAndClearActions() {
        val text = mutableStateOf("")
        var searches = 0
        var clearLabel = ""
        compose.setContent {
            PocketDexTheme {
                clearLabel = stringResource(R.string.pd_clear_search)
                PokemonSearchField(text.value, { text.value = it }, { searches++ })
            }
        }
        compose.onNode(hasSetTextAction()).performTextInput("Pikachu")
        compose.runOnIdle { assertEquals("Pikachu", text.value); assertEquals(0, searches) }
        compose.onNode(hasSetTextAction()).performImeAction()
        compose.runOnIdle { assertEquals(1, searches) }
        compose.onNodeWithContentDescription(clearLabel).performClick()
        compose.runOnIdle { assertEquals("", text.value); assertEquals(1, searches) }
    }
}
