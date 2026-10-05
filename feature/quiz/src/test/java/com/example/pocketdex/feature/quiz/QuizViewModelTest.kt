package com.example.pocketdex.feature.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.quiz.*
import com.example.pocketdex.core.domain.repository.*
import com.example.pocketdex.core.domain.util.*
import com.example.pocketdex.feature.quiz.presentation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val main = dispatcher; override val io = dispatcher; override val default = dispatcher
    }
    @Before fun before() { Dispatchers.setMain(dispatcher) }
    private val stores = mutableListOf<ViewModelStore>()
    @After fun after() { stores.forEach { it.clear() }; Dispatchers.resetMain() }
    private val pool = (1..8).map { id -> PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(id), "pokemon-$id", id),
        listOf(PokemonType.Fire), "https://example.test/$id.png"), PokemonStatistics(id, 2, 3, 4, 5, 6)) }
    private var sourceCalls = 0
    private var randomCalls = 0
    private var nextId = 0
    private val source = object : QuizCandidateRepository {
        override suspend fun candidates(): DataResult<QuizCandidatePool> {
            sourceCalls++; return DataResult.Success(QuizCandidatePool(pool, DataOrigin.Local, CatalogCoverage.Partial))
        }
    }
    private class Media : QuizMediaRepository {
        val protected = mutableMapOf<String, List<QuizMedia>>()
        var failure: DataFailure? = null
        var corrupt = false
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun reconcile(taskId: Int, currentSessionId: String?) = DataResult.Success(Unit)
        override suspend fun prepare(sessionId: String, pokemon: List<PokemonSummary>): DataResult<List<QuizMedia>> {
            gate?.await()
            failure?.let { return DataResult.Failure(it) }
            return DataResult.Success(pokemon.map { QuizMedia(it.identity.id, "file-${it.identity.id.value}") }.also {
                protected[sessionId] = it; corrupt = false
            })
        }
        override suspend fun restore(sessionId: String, pokemonIds: List<PokemonId>): DataResult<List<QuizMedia>> =
            if (corrupt) DataResult.Failure(DataFailure.InsufficientLocalData)
            else protected[sessionId]?.let { DataResult.Success(it) } ?: DataResult.Failure(DataFailure.InsufficientLocalData)
        override suspend fun release(sessionId: String): DataResult<Unit> { protected.remove(sessionId); return DataResult.Success(Unit) }
    }
    private fun viewModel(media: Media, saved: SavedStateHandle = SavedStateHandle()) = QuizViewModel(source, media,
        RandomSource { randomCalls++; 0 }, QuizSessionIdSource { (++nextId).toString() }, dispatchers, saved)
        .also { vm -> vm.setForeground(true); stores += ViewModelStore().apply { put("quiz", vm) } }
    private suspend fun TestScope.started(media: Media, saved: SavedStateHandle = SavedStateHandle()): QuizViewModel {
        val vm = viewModel(media, saved); vm.attach(42); advanceUntilIdle(); vm.enter(); vm.start(); advanceUntilIdle(); return vm
    }

    @Test fun doesNotStartBeforeAllFiveMediaAreVerifiedAndFailureCanRetry() = runTest(dispatcher) {
        val media = Media().apply { gate = CompletableDeferred() }; val vm = viewModel(media)
        vm.attach(42); advanceUntilIdle(); vm.enter(); vm.start(); runCurrent()
        assertEquals(QuizPhase.Preparing, vm.state.value.phase); assertNull(vm.state.value.session)
        vm.start(); assertEquals(1, sourceCalls)
        media.failure = DataFailure.Offline; media.gate!!.complete(Unit); advanceUntilIdle()
        assertEquals(QuizPhase.Error, vm.state.value.phase); assertNull(vm.state.value.session)
        assertTrue(media.protected.isEmpty())
        media.failure = null; media.gate = null; vm.retry(); advanceUntilIdle()
        assertEquals(QuizPhase.Playing, vm.state.value.phase); assertEquals(5, vm.state.value.files.size)
    }

    @Test fun locksResponsesRejectsStaleAdvanceAndResetsHintsOnNextQuestion() = runTest(dispatcher) {
        val vm = started(Media())
        vm.state.test {
            val session = awaitItem().session!!
            vm.showHints(session.id, 0); assertTrue(awaitItem().session!!.hintsVisible)
            vm.answer(session.id, 0, session.current.correct.summary.identity.id)
            assertEquals(1, awaitItem().session!!.score)
            vm.answer(session.id, 0, session.current.options.first().summary.identity.id); expectNoEvents()
            vm.advance(session.id, 0); val next = awaitItem().session!!
            assertEquals(1, next.index); assertFalse(next.hintsVisible)
            vm.advance(session.id, 0); vm.answer(session.id, 0, session.current.correct.summary.identity.id); expectNoEvents()
        }
    }

    @Test fun backContinueRestartConfirmationAndCancellationPreserveProgress() = runTest(dispatcher) {
        val media = Media(); val vm = started(media); val first = vm.state.value.session!!
        vm.answer(first.id, 0, first.current.correct.summary.identity.id)
        val answered = vm.state.value.session!!
        vm.leave(); vm.enter(); assertEquals(QuizPhase.Resume, vm.state.value.phase)
        vm.restart(); assertTrue(vm.state.value.confirmRestart)
        vm.cancelRestart(); assertEquals(answered, vm.state.value.session)
        vm.continueSession(); assertEquals(QuizPhase.Playing, vm.state.value.phase)
        vm.restart(); vm.confirmRestart(); advanceUntilIdle()
        assertEquals(0, vm.state.value.session!!.score); assertNotEquals(first.id, vm.state.value.session!!.id)
        assertFalse(media.protected.containsKey(first.id))
    }

    @Test fun restoresTaskWithoutRandomizationAndCorruptionRecoversSameProgress() = runTest(dispatcher) {
        val saved = SavedStateHandle(); val media = Media(); val vm = started(media, saved)
        val first = vm.state.value.session!!
        vm.answer(first.id, 0, first.current.correct.summary.identity.id); vm.showHints(first.id, 0)
        val answered = vm.state.value.session!!; val oldRandom = randomCalls; val oldSources = sourceCalls
        val restored = viewModel(media, saved); restored.attach(42); advanceUntilIdle()
        assertEquals(answered, restored.state.value.session); assertEquals(QuizPhase.Resume, restored.state.value.phase)
        assertEquals(oldRandom, randomCalls); assertEquals(oldSources, sourceCalls)
        restored.enter(); restored.continueSession(); media.corrupt = true
        restored.imageFailed(first.id, 0); media.failure = DataFailure.Offline; restored.retry(); advanceUntilIdle()
        assertEquals(QuizPhase.Error, restored.state.value.phase); assertEquals(answered, restored.state.value.session)
        media.failure = null; restored.retry(); advanceUntilIdle()
        assertEquals(answered, restored.state.value.session); assertEquals(oldRandom, randomCalls)
    }

    @Test fun completesFiveQuestionsOfflineAndFinalRestartNeedsNoConfirmation() = runTest(dispatcher) {
        val media = Media(); val vm = started(media)
        media.failure = DataFailure.Offline
        repeat(5) {
            val session = vm.state.value.session!!
            vm.answer(session.id, session.index, session.current.correct.summary.identity.id)
            val result = vm.advance(session.id, session.index)
            if (it < 4) assertNull(result) else assertEquals(session.id, result)
        }
        assertEquals(5, vm.state.value.session!!.score); assertTrue(vm.state.value.session!!.completed)
        vm.restart(); assertFalse(vm.state.value.confirmRestart); advanceUntilIdle()
        assertEquals(QuizPhase.Error, vm.state.value.phase) // Offline preparation failed; completed session remains.
        assertEquals(5, vm.state.value.session!!.score)
    }

    @Test fun snapshotRejectsBadVersionAndInvalidAnswersAndNewTaskHasNoSession() = runTest(dispatcher) {
        val session = generateQuiz("quiz:42:valid", pool, RandomSource { 0 })!!
        val text = QuizSnapshotCodec.encode(session)
        assertEquals(session, QuizSnapshotCodec.decode(text))
        assertNull(QuizSnapshotCodec.decode(text.replace("\"version\":1", "\"version\":2")))
        assertNull(QuizSnapshotCodec.decode(text.replace("\"answers\":[null", "\"answers\":[999")))
        val vm = viewModel(Media(), SavedStateHandle(mapOf("quiz.snapshot" to text)))
        vm.attach(43); advanceUntilIdle(); assertNull(vm.state.value.session); assertTrue(vm.state.value.recoveryNotice)
        val fresh = viewModel(Media()); fresh.attach(44); advanceUntilIdle(); assertNull(fresh.state.value.session)
    }

    @Test fun replacementFinishingInBackgroundKeepsOldSnapshotAndPinsUntilResumed() = runTest(dispatcher) {
        val saved = SavedStateHandle(); val media = Media(); val vm = started(media, saved)
        val first = vm.state.value.session!!
        vm.answer(first.id, 0, first.current.correct.summary.identity.id)
        val answered = vm.state.value.session!!
        media.gate = CompletableDeferred()
        vm.restart(); vm.confirmRestart(); runCurrent()
        vm.setForeground(false); media.gate!!.complete(Unit); advanceUntilIdle()
        assertEquals(answered, vm.state.value.session)
        assertEquals(answered, QuizSnapshotCodec.decode(saved["quiz.snapshot"]))
        assertTrue(media.protected.containsKey(first.id)); assertEquals(2, media.protected.size)
        vm.setForeground(true); advanceUntilIdle()
        assertNotEquals(first.id, vm.state.value.session!!.id)
        assertFalse(media.protected.containsKey(first.id)); assertEquals(1, media.protected.size)
    }

    @Test fun localizedOptionsSurviveSnapshotWithoutChangingFactsOrScores() {
        val localized = pool.map { it.copy(summary = it.summary.copy(identity = it.summary.identity.copy(
            localizedNames = LocalizedPokemonNames("English-${it.summary.identity.id.value}", "Español-${it.summary.identity.id.value}")))) }
        val original = generateQuiz("quiz:42:localized", localized, RandomSource { 0 })!!
        val restored = QuizSnapshotCodec.decode(QuizSnapshotCodec.encode(original))!!
        assertEquals(original, restored)
        assertNotEquals(restored.current.correct.summary.identity.visibleName(PokemonLanguage.English),
            restored.current.correct.summary.identity.visibleName(PokemonLanguage.Spanish))
        assertEquals(original.current.correct.statistics, restored.current.correct.statistics)
        assertEquals(original.score, restored.score)
    }
}
