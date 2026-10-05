package com.example.pocketdex.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pocketdex.R
import com.example.pocketdex.core.domain.model.PokemonId
import com.example.pocketdex.ui.PocketDexApp
import com.example.pocketdex.ui.theme.PocketDexTheme
import org.junit.Rule
import org.junit.Test

/** Host fixtures only: these do not claim coverage of unimplemented product features. */
class NavigationHostTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun switchReselectAndExploreRetainHomeContext() {
        compose.setContent { TestApp() }
        compose.onNodeWithTag("change-context").performClick()
        compose.onNodeWithText(context.getString(R.string.destination_favorites)).performClick()
        compose.onNodeWithTag("favorites-content").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.destination_favorites)).performClick()
        compose.onNodeWithTag("explore").performClick()
        compose.onNodeWithTag("home-context").assertTextEquals("1")
    }

    @Test fun detailBackReturnsToFavorites() {
        compose.setContent { TestApp() }
        compose.onNodeWithText(context.getString(R.string.destination_favorites)).performClick()
        compose.onNodeWithTag("favorite-detail").performClick()
        compose.onNodeWithTag("detail-id").assertTextEquals("25")
        compose.onNodeWithTag("detail-back").performClick()
        compose.onNodeWithTag("favorites-content").assertIsDisplayed()
    }

    @Test fun savedStateRestoresInactiveEntryContextAndSelectedRoute() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { TestApp() }
        compose.onNodeWithTag("change-context").performClick()
        compose.onNodeWithText(context.getString(R.string.destination_favorites)).performClick()
        compose.onNodeWithTag("favorite-detail").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("detail-id").assertTextEquals("25")
        compose.onNodeWithTag("detail-back").performClick()
        compose.onNodeWithTag("explore").performClick()
        compose.onNodeWithTag("home-context").assertTextEquals("1")
    }
}

@Composable
private fun TestApp() {
    PocketDexTheme {
        PocketDexApp(emptyList(), FeatureEntries(
            home = { onDetail ->
                var context by rememberSaveable { mutableIntStateOf(0) }
                Column {
                    Text(context.toString(), Modifier.testTag("home-context"))
                    Button({ context++ }, Modifier.testTag("change-context")) { Text("Change") }
                    Button({ onDetail(PokemonId(1)) }) { Text("Open") }
                }
            },
            favorites = { onDetail, onExplore ->
                Column {
                    Text("Favorites fixture", Modifier.testTag("favorites-content"))
                    Button({ onDetail(PokemonId(25)) }, Modifier.testTag("favorite-detail")) { Text("Open") }
                    Button(onExplore, Modifier.testTag("explore")) { Text("Explore") }
                }
            },
            detail = { id, onBack ->
                Column {
                    Text(id.value.toString(), Modifier.testTag("detail-id"))
                    Button(onBack, Modifier.testTag("detail-back")) { Text("Back") }
                }
            },
            comparator = { _, _ -> Text("Compare fixture") },
            compareSelector = { _, _, _, _ -> Text("Selector fixture") },
            onCompareSelection = { _, _ -> },
            comparisonResults = { _, _, _ -> Text("Comparison results fixture") },
            quiz = { _, _ -> Text("Quiz fixture") },
            quizResults = { _, _ -> Text("Results fixture") },
        ))
    }
}
