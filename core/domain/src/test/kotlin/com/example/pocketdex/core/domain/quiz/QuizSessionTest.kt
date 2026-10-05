package com.example.pocketdex.core.domain.quiz

import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.util.RandomSource
import org.junit.Assert.*
import org.junit.Test

class QuizSessionTest {
    private val random = RandomSource { 0 }
    private fun pokemon(id: Int) = PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(id), "pokemon-$id", id),
        listOf(PokemonType.Fire), "https://example.test/$id.png"), PokemonStatistics(id, 2, 3, 4, 5, 6))
    private val pool get() = (1..8).map(::pokemon)
    private fun session() = requireNotNull(generateQuiz("session", pool, random))

    @Test fun generatesFiveDistinctCorrectsFourUniqueOptionsAndDistinguishableHints() {
        val first = session(); val second = session()
        assertEquals(5, first.questions.size)
        assertEquals(5, first.questions.map { it.correct.summary.identity.id }.distinct().size)
        first.questions.forEach { question ->
            assertEquals(4, question.options.map { it.summary.identity.id }.distinct().size)
            assertEquals(1, question.options.count { it.summary.identity.id == question.correct.summary.identity.id })
            assertTrue(question.options.filter { it != question.correct }.all { it.statistics != question.correct.statistics })
        }
        assertEquals(first.questions, second.questions) // Repetition across sessions is permitted.
    }

    @Test fun insufficientIncompleteAmbiguousAndIdenticalHintPoolsCannotGenerate() {
        assertNull(generateQuiz("session", pool.take(4), random))
        assertNull(generateQuiz("session", pool.map { it.copy(statistics = PokemonStatistics()) }, random))
        assertNull(generateQuiz("session", pool.map { it.copy(summary = it.summary.copy(types = emptyList())) }, random))
        assertNull(generateQuiz("session", pool.map { it.copy(statistics = PokemonStatistics(1, 2, 3, 4, 5, 6)) }, random))
        val ambiguous = pool.map { it.copy(summary = it.summary.copy(identity = it.summary.identity.copy(
            localizedNames = LocalizedPokemonNames(spanish = " SAME ")))) }
        assertNull(generateQuiz("session", ambiguous, random))
    }

    @Test fun firstValidResponseLocksAndRejectsWrongSessionQuestionAndOption() {
        val initial = session(); val correct = initial.current.correct.summary.identity.id
        assertEquals(initial, initial.answer("old-session", 0, correct))
        assertEquals(initial, initial.answer(initial.id, 1, correct))
        assertEquals(initial, initial.answer(initial.id, 0, PokemonId(999)))
        assertEquals(initial, initial.advance(initial.id, 0))
        val answered = initial.answer(initial.id, 0, correct)
        assertEquals(1, answered.score)
        assertEquals(answered, answered.answer(initial.id, 0, initial.current.options.first { it != initial.current.correct }.summary.identity.id))
        val next = answered.copy(hintsVisible = true).advance(initial.id, 0)
        assertEquals(1, next.index); assertFalse(next.hintsVisible)
        assertEquals(next, next.advance(initial.id, 0))
        assertEquals(next, next.answer(initial.id, 0, correct))
    }

    @Test fun scoreIsDerivedAndFinalResultRequiresFifthAnswerAndExplicitAdvance() {
        for (correctAnswers in 0..5) {
            var current = session()
            repeat(5) { index ->
                val option = if (index < correctAnswers) current.current.correct else current.current.options.first { it != current.current.correct }
                current = current.answer(current.id, index, option.summary.identity.id)
                assertFalse(current.completed)
                current = current.advance(current.id, index)
            }
            assertTrue(current.completed); assertEquals(correctAnswers, current.score)
            assertEquals(current, current.advance(current.id, 4))
        }
        assertEquals(0, session().score)
    }
}
