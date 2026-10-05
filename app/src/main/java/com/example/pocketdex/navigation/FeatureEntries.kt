package com.example.pocketdex.navigation

import androidx.compose.runtime.Composable
import com.example.pocketdex.core.domain.model.PokemonId

/** App owns navigation. Concrete feature entries are added in their own increments. */
data class FeatureEntries(
    val home: @Composable (onDetail: (PokemonId) -> Unit) -> Unit,
    val favorites: @Composable (onDetail: (PokemonId) -> Unit, onExplore: () -> Unit) -> Unit,
    val detail: @Composable (id: PokemonId, onBack: () -> Unit) -> Unit,
    val comparator: @Composable (
        onSelect: (slot: Int, excluded: PokemonId?) -> Unit,
        onResults: (left: PokemonId, right: PokemonId) -> Unit,
    ) -> Unit,
    val compareSelector: @Composable (slot: Int, excluded: PokemonId?, onSelected: (PokemonId) -> Unit, onBack: () -> Unit) -> Unit,
    /** Dispatch to compare's retained state owner; app transports IDs and never changes business selections. */
    val onCompareSelection: (slot: Int, id: PokemonId) -> Unit,
    val comparisonResults: @Composable (left: PokemonId, right: PokemonId, onBack: () -> Unit) -> Unit,
    val quiz: @Composable (onResults: (sessionId: String) -> Unit, onHome: () -> Unit) -> Unit,
    val quizResults: @Composable (sessionId: String, onBack: () -> Unit) -> Unit,
)
