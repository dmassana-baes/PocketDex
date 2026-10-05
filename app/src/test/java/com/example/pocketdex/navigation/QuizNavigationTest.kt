package com.example.pocketdex.navigation

import com.example.pocketdex.core.domain.model.PokemonId
import org.junit.Assert.*
import org.junit.Test

class QuizNavigationTest {
    @Test fun quizBackGoesHomeButOutgoingQuizCallbackCannotChangeAnotherDestination() {
        val navigator = AppNavigator()
        navigator.select(TopLevel.Quiz); navigator.exploreHomeFromQuiz()
        assertEquals(TopLevel.Home, navigator.snapshot.selected)
        navigator.select(TopLevel.Comparator); navigator.openCompareSelector(0, PokemonId(1))
        val before = navigator.snapshot
        navigator.exploreHomeFromQuiz()
        assertEquals(before, navigator.snapshot)
    }
}
