package com.example.pocketdex.core.domain.quiz

import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.util.RandomSource
import java.util.Locale

private fun PokemonIdentity.label(language: PokemonLanguage) = visibleName(language).trim().lowercase(Locale.ROOT)
private fun PokemonDetail.signature() = summary.types.toSet() to statistics

fun eligibleQuizCandidates(candidates: List<PokemonDetail>): List<PokemonDetail> {
    val complete = candidates.distinctBy { it.summary.identity.id }.filter {
        it.statistics.isComplete && it.summary.types.isNotEmpty() &&
            (it.summary.imageUrl != null || it.summary.localImageFile != null)
    }
    return complete.filter { candidate -> PokemonLanguage.entries.all { language ->
        complete.count { it.summary.identity.label(language) == candidate.summary.identity.label(language) } == 1
    } }
}

private fun distractors(correct: PokemonDetail, pool: List<PokemonDetail>) = pool.filter {
    it.summary.identity.id != correct.summary.identity.id && it.signature() != correct.signature()
}

fun canPrepareQuiz(candidates: List<PokemonDetail>): Boolean {
    val pool = eligibleQuizCandidates(candidates)
    return pool.count { distractors(it, pool).size >= 3 } >= 5
}

data class QuizQuestion(val correct: PokemonDetail, val options: List<PokemonDetail>) {
    init {
        require(options.size == 4 && options.map { it.summary.identity.id }.distinct().size == 4)
        require(options.count { it.summary.identity.id == correct.summary.identity.id } == 1)
        require(options.single { it.summary.identity.id == correct.summary.identity.id } == correct)
        require(eligibleQuizCandidates(options).size == 4)
        require(distractors(correct, options).size == 3)
    }
}

data class QuizSession(
    val id: String,
    val questions: List<QuizQuestion>,
    val index: Int = 0,
    val answers: List<PokemonId?> = List(5) { null },
    val hintsVisible: Boolean = false,
    val completed: Boolean = false,
) {
    init {
        require(id.isNotBlank() && questions.size == 5 && index in 0..4 && answers.size == 5)
        require(questions.map { it.correct.summary.identity.id }.distinct().size == 5)
        require(answers.withIndex().all { (position, answer) ->
            (position >= index || answer != null) && (position <= index || answer == null) &&
                (answer == null || questions[position].options.any { it.summary.identity.id == answer })
        })
        require(!completed || (index == 4 && answers.all { it != null }))
    }
    val current: QuizQuestion get() = questions[index]
    val answered: Boolean get() = answers[index] != null
    val score: Int get() = answers.indices.count { answers[it] == questions[it].correct.summary.identity.id }
    val hasProgress: Boolean get() = answers.any { it != null }

    fun answer(sessionId: String, questionIndex: Int, option: PokemonId): QuizSession {
        if (id != sessionId || index != questionIndex || answered || completed ||
            current.options.none { it.summary.identity.id == option }) return this
        return copy(answers = answers.mapIndexed { position, old -> if (position == index) option else old })
    }

    fun advance(sessionId: String, questionIndex: Int): QuizSession {
        if (id != sessionId || index != questionIndex || !answered || completed) return this
        return if (index == 4) copy(completed = true) else copy(index = index + 1, hintsVisible = false)
    }
}

private fun <T> shuffled(values: List<T>, random: RandomSource): List<T> {
    val result = values.toMutableList()
    for (index in result.lastIndex downTo 1) {
        val other = random.nextInt(index + 1)
        require(other in 0..index)
        val old = result[index]; result[index] = result[other]; result[other] = old
    }
    return result
}

fun generateQuiz(id: String, candidates: List<PokemonDetail>, random: RandomSource): QuizSession? {
    val pool = eligibleQuizCandidates(candidates)
    val corrects = pool.filter { distractors(it, pool).size >= 3 }
    if (corrects.size < 5) return null
    return QuizSession(id, shuffled(corrects, random).take(5).map { correct ->
        QuizQuestion(correct, shuffled(listOf(correct) + shuffled(distractors(correct, pool), random).take(3), random))
    })
}

fun interface QuizSessionIdSource { fun nextId(): String }
