package com.example.pocketdex.feature.favorites

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pocketdex.core.designsystem.components.PokemonImageState
import com.example.pocketdex.core.designsystem.theme.PocketDexTheme
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.favorites.presentation.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FavoritesScreenTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun actions(open: (PokemonId) -> Unit = {}, remove: (PokemonId) -> Unit = {}, explore: () -> Unit = {}) =
        FavoritesActions(open, explore, remove, {}, {}, {})

    @Test fun emptyHasExplore() {
        var explored = false
        compose.setContent { PocketDexTheme { FavoritesScreen(FavoritesUiState(loading = false, ready = true),
            PokemonLanguage.English, rememberLazyGridState(), actions(explore = { explored = true }), { PokemonImageState.Error }) } }
        compose.onNodeWithText(context.getString(R.string.favorites_explore)).performClick()
        compose.runOnIdle { assertTrue(explored) }
    }
    @Test fun onlyIdFavoriteCanBeOpenedAndRemovedWithoutInventingNumber() {
        var opened: PokemonId? = null
        var removed: PokemonId? = null
        val item = FavoritePokemon(KnownPokemon(PokemonId(25)), 1, false)
        compose.setContent { PocketDexTheme { FavoritesScreen(FavoritesUiState(items = listOf(item), loading = false, ready = true),
            PokemonLanguage.English, rememberLazyGridState(), actions({ opened = it }, { removed = it }), { PokemonImageState.Error }) } }
        compose.onNodeWithText("#025").assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.favorites_unknown)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(PokemonId(25), opened); assertNull(removed) }
        compose.onNodeWithContentDescription(context.getString(com.example.pocketdex.core.designsystem.R.string.pd_remove_favorite))
            .performScrollTo().performClick()
        compose.runOnIdle { assertEquals(PokemonId(25), removed) }
    }
    @Test fun readFailureShowsRetryWithoutExploreOrFalseCount() {
        compose.setContent { PocketDexTheme { FavoritesScreen(FavoritesUiState(loading = false, readFailure = DataFailure.Storage),
            PokemonLanguage.English, rememberLazyGridState(), actions(), { PokemonImageState.Error }) } }
        compose.onNodeWithText(context.getString(R.string.favorites_read_error)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.favorites_explore)).assertDoesNotExist()
    }
}
