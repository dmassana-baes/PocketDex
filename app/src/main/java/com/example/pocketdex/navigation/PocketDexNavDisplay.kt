package com.example.pocketdex.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.pocketdex.core.domain.model.PokemonId

@Composable
fun PocketDexNavDisplay(navigator: AppNavigator, features: FeatureEntries, modifier: Modifier = Modifier) {
    val snapshot = navigator.snapshot
    // Decorate every stack even while inactive, keeping its saved state and ViewModelStore alive.
    val decorated = TopLevel.entries.associateWith { destination ->
        key(destination) {
            rememberDecoratedNavEntries(
                backStack = snapshot.stack(destination),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator<AppRoute>(),
                    rememberViewModelStoreNavEntryDecorator<AppRoute>(),
                ),
                entryProvider = { route ->
                    NavEntry(route) {
                        when (route) {
                            is AppRoute.Area -> when (route.destination) {
                                TopLevel.Home -> features.home { navigator.openDetail(it, TopLevel.Home) }
                                TopLevel.Favorites -> features.favorites(
                                    { navigator.openDetail(it, TopLevel.Favorites) }, navigator::exploreHomeFromFavorites,
                                )
                                TopLevel.Comparator -> features.comparator(navigator::openCompareSelector, navigator::showComparisonResults)
                                TopLevel.Quiz -> features.quiz(navigator::showQuizResults, navigator::exploreHomeFromQuiz)
                            }
                            is AppRoute.Detail -> features.detail(PokemonId(route.pokemonId)) { navigator.backFrom(route) }
                            is AppRoute.CompareSelector -> features.compareSelector(
                                route.slot, route.excludedPokemonId?.let(::PokemonId),
                                { selected ->
                                    if (navigator.snapshot.current == route && selected.value != route.excludedPokemonId) {
                                        features.onCompareSelection(route.slot, selected)
                                        navigator.backFrom(route)
                                    }
                                }, { navigator.backFrom(route) },
                            )
                            is AppRoute.QuizResults -> features.quizResults(route.sessionId) { navigator.backFrom(route) }
                            is AppRoute.ComparisonResults -> features.comparisonResults(
                                PokemonId(route.leftPokemonId), PokemonId(route.rightPokemonId),
                                { navigator.backFrom(route) },
                            )
                        }
                    }
                },
            )
        }
    }
    // Home beneath a non-Home stack gives NavDisplay the same exit-through-Home policy as back().
    val entries = if (snapshot.selected == TopLevel.Home) decorated.getValue(TopLevel.Home)
    else decorated.getValue(TopLevel.Home).take(1) + decorated.getValue(snapshot.selected)
    NavDisplay(entries = entries, onBack = navigator::back, modifier = modifier)
}
