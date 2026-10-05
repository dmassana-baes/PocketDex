package com.example.pocketdex.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
enum class TopLevel { Home, Comparator, Quiz, Favorites }

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data class Area(val destination: TopLevel) : AppRoute

    @Serializable
    data class Detail(val pokemonId: Int, val origin: TopLevel) : AppRoute {
        init {
            require(pokemonId > 0)
            require(origin == TopLevel.Home || origin == TopLevel.Favorites)
        }
    }

    @Serializable
    data class CompareSelector(val slot: Int, val excludedPokemonId: Int? = null) : AppRoute {
        init {
            require(slot in 0..1)
            require(excludedPokemonId == null || excludedPokemonId > 0)
        }
    }

    @Serializable
    data class ComparisonResults(val leftPokemonId: Int, val rightPokemonId: Int) : AppRoute {
        init {
            require(leftPokemonId > 0 && rightPokemonId > 0)
            require(leftPokemonId != rightPokemonId)
        }
    }

    @Serializable
    data class QuizResults(val sessionId: String) : AppRoute {
        init { require(sessionId.isNotBlank()) }
    }
}
