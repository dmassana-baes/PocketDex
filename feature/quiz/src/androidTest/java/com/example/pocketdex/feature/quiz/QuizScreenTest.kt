package com.example.pocketdex.feature.quiz

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pocketdex.core.designsystem.components.PokemonImageState
import com.example.pocketdex.core.designsystem.theme.PocketDexTheme
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.quiz.*
import com.example.pocketdex.core.domain.util.RandomSource
import com.example.pocketdex.feature.quiz.presentation.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class QuizScreenTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun session() = requireNotNull(generateQuiz("session", (1..8).map { id ->
        PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(id), "pokemon-$id", id), listOf(PokemonType.Fire),
            "https://example.test/$id.png"), PokemonStatistics(id, 2, 3, 4, 5, 6))
    }, RandomSource { 0 }))
    private fun actions(answer: (String, Int, PokemonId) -> Unit = { _, _, _ -> }, hints: (String, Int) -> Unit = { _, _ -> }) =
        QuizActions({}, {}, {}, {}, {}, {}, {}, answer, { _, _ -> }, hints)

    @Test fun fourOptionsLockAfterAnswerAndSilhouetteDoesNotAnnounceCorrectIdentityBeforehand() {
        val initial = session(); val correctName = initial.current.correct.summary.identity.visibleName(PokemonLanguage.English)
        compose.setContent {
            var current by remember { mutableStateOf(initial) }
            PocketDexTheme { QuizScreen(QuizUiState(current, phase = QuizPhase.Playing), PokemonLanguage.English,
                actions(answer = { id, index, option -> current = current.answer(id, index, option) }),
                { PokemonImageState.Ready(ColorPainter(Color.Red)) }) }
        }
        compose.onNodeWithContentDescription(correctName).assertDoesNotExist()
        initial.current.options.forEach { compose.onNodeWithText(it.summary.identity.name).performScrollTo().assertIsEnabled() }
        compose.onNodeWithText(context.getString(R.string.quiz_next)).performScrollTo().assertIsNotEnabled()
        val wrong = initial.current.options.first { it != initial.current.correct }
        compose.onNodeWithText(wrong.summary.identity.name).performScrollTo().performClick()
        initial.current.options.forEach { compose.onNodeWithText(it.summary.identity.name).performScrollTo().assertIsNotEnabled() }
        compose.onNodeWithText(context.getString(R.string.quiz_incorrect)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.quiz_next)).performScrollTo().assertIsEnabled()
        compose.onNodeWithContentDescription(correctName).assertExists()
        compose.onNodeWithText(correctName).assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,
            context.getString(R.string.quiz_option_correct)))
        compose.onNodeWithText(wrong.summary.identity.name).assertIsSelected()
    }

    @Test fun hintsPreserveOptionsAndDoNotExposeNationalNumberOrCorrectImageName() {
        val initial = session(); var shown: QuizSession? = null
        compose.setContent {
            var current by remember { mutableStateOf(initial) }
            PocketDexTheme { QuizScreen(QuizUiState(current, phase = QuizPhase.Playing), PokemonLanguage.English,
                actions(hints = { _, _ -> current = current.copy(hintsVisible = true); shown = current }),
                { PokemonImageState.Ready(ColorPainter(Color.Red)) }) }
        }
        compose.onNodeWithText(context.getString(R.string.quiz_hints)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(initial.current.options, shown!!.current.options); assertEquals(0, shown!!.score) }
        compose.onNodeWithContentDescription(initial.current.correct.summary.identity.name).assertDoesNotExist()
        compose.onNodeWithText("#" + initial.current.correct.summary.identity.nationalNumber.toString().padStart(3, '0')).assertDoesNotExist()
        initial.current.options.forEach { compose.onNodeWithText(it.summary.identity.name).performScrollTo().assertIsEnabled() }
    }

    @Test fun unavailableImageDisablesAllAnswersBeforeRecovery() {
        val initial = session()
        compose.setContent { PocketDexTheme { QuizScreen(QuizUiState(initial, phase = QuizPhase.Playing),
            PokemonLanguage.English, actions(), { PokemonImageState.Loading }) } }
        initial.current.options.forEach { compose.onNodeWithText(it.summary.identity.name).performScrollTo().assertIsNotEnabled() }
        compose.onNodeWithText(context.getString(R.string.quiz_next)).performScrollTo().assertIsNotEnabled()
    }

    @Test fun finalResultShowsFiveQuestionScoreAndAllowsRestart() {
        var finished = session()
        repeat(5) {
            finished = finished.answer(finished.id, finished.index, finished.current.correct.summary.identity.id)
                .advance(finished.id, finished.index)
        }
        var restarted = false
        compose.setContent { PocketDexTheme { QuizResultsScreen(finished, {}, { restarted = true }) } }
        compose.onNodeWithText(context.getString(R.string.quiz_score, 5)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.quiz_restart)).performScrollTo().performClick()
        compose.runOnIdle { assertTrue(restarted) }
    }
}
