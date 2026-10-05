package com.example.pocketdex.feature.home

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pocketdex.core.designsystem.theme.PocketDexTheme
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.home.presentation.*
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Screen interaction coverage. Storage/network behavior is exercised separately. */
class HomeScreenTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun cardAndFavoriteHaveIndependentAccessibleActions() {
        var opened: PokemonId? = null
        var marked: PokemonId? = null
        val pokemon = PokemonSummary(PokemonIdentity(PokemonId(25), "pikachu", 25), listOf(PokemonType.Electric), null)
        compose.setContent {
            val flow = remember { flowOf(PagingData.from(listOf(pokemon))) }
            PocketDexTheme {
                HomeScreen(HomeUiState(refreshing = false, favoritesReady = true, coverage = CatalogCoverage.Complete),
                    flow.collectAsLazyPagingItems(), rememberLazyGridState(), actions(
                        onFavorite = { marked = it.identity.id }, onDetail = { opened = it }))
            }
        }
        compose.onNodeWithContentDescription(context.getString(com.example.pocketdex.core.designsystem.R.string.pd_add_favorite))
            .performScrollTo().performClick()
        compose.runOnIdle { assertEquals(PokemonId(25), marked); assertEquals(null, opened) }
        compose.onNodeWithText("pikachu").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(PokemonId(25), opened) }
    }

    @Test fun searchClearAndFilterCloseAreReachableWithoutColorOrGesture() {
        var text = "pikachu"
        var dismissed = false
        compose.setContent {
            var state by remember { mutableStateOf(HomeUiState(text = text, refreshing = false, coverage = CatalogCoverage.Complete)) }
            val flow = remember { flowOf(PagingData.from(emptyList<PokemonSummary>())) }
            PocketDexTheme {
                HomeScreen(state, flow.collectAsLazyPagingItems(), rememberLazyGridState(), actions(
                    onText = { text = it; state = state.copy(text = it) },
                    onFilter = { state = state.copy(filterOpen = true) },
                    onDismiss = { dismissed = true; state = state.copy(filterOpen = false) }))
            }
        }
        compose.onNodeWithContentDescription(context.getString(com.example.pocketdex.core.designsystem.R.string.pd_clear_search)).performClick()
        compose.runOnIdle { assertEquals("", text) }
        compose.onNodeWithText(context.getString(R.string.home_filter)).performClick()
        compose.onNodeWithContentDescription(context.getString(R.string.home_close_filter)).performClick()
        compose.runOnIdle { assertEquals(true, dismissed) }
    }

    private fun actions(
        onText: (String) -> Unit = {}, onFilter: () -> Unit = {}, onDismiss: () -> Unit = {},
        onFavorite: (PokemonSummary) -> Unit = {}, onDetail: (PokemonId) -> Unit = {},
    ) = HomeActions(onText, {}, onFilter, {}, {}, onDismiss, {}, {}, {}, {}, onFavorite, onDetail)
}
