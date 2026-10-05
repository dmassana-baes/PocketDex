package com.example.pocketdex.feature.quiz.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.quiz.*
import com.example.pocketdex.core.domain.repository.*
import com.example.pocketdex.core.domain.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

enum class QuizPhase { Idle, Preparing, Playing, Resume, Error, Results }
data class QuizUiState(
    val session: QuizSession? = null,
    val files: Map<PokemonId, String> = emptyMap(),
    val phase: QuizPhase = QuizPhase.Idle,
    val failure: DataFailure? = null,
    val confirmRestart: Boolean = false,
    val recoveryNotice: Boolean = false,
    val attached: Boolean = false,
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val candidates: QuizCandidateRepository,
    private val media: QuizMediaRepository,
    private val random: RandomSource,
    private val sessionIds: QuizSessionIdSource,
    private val dispatchers: DispatcherProvider,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val initial = QuizSnapshotCodec.decode(saved["quiz.snapshot"])
    private val mutable = MutableStateFlow(QuizUiState(session = initial,
        recoveryNotice = saved.get<String>("quiz.snapshot") != null && initial == null))
    val state = mutable.asStateFlow()
    private var taskId: Int? = null
    private var active = false
    private val foreground = MutableStateFlow(false)
    private var job: Job? = null
    private enum class Work { Attach, Prepare, Restore }
    private var lastWork = Work.Attach
    fun setForeground(resumed: Boolean) { foreground.value = resumed }

    fun attach(task: Int) {
        if (taskId != null) return
        require(task >= 0)
        taskId = task
        if (mutable.value.session?.id?.startsWith("quiz:$task:") == false) {
            mutable.value = mutable.value.copy(session = null, recoveryNotice = true)
            saved.remove<String>("quiz.snapshot")
        }
        runWork(if (mutable.value.session == null) Work.Attach else Work.Restore)
    }

    fun enter() {
        active = true
        if (mutable.value.session != null && mutable.value.phase in listOf(QuizPhase.Playing, QuizPhase.Results))
            mutable.value = mutable.value.copy(phase = QuizPhase.Resume)
    }

    fun leave() {
        active = false
        if (mutable.value.phase in listOf(QuizPhase.Playing, QuizPhase.Results))
            mutable.value = mutable.value.copy(phase = QuizPhase.Resume)
    }

    fun start() { if (foreground.value && mutable.value.attached && mutable.value.session == null) runWork(Work.Prepare) }
    fun retry() { if (mutable.value.phase == QuizPhase.Error) runWork(lastWork) }

    fun continueSession(): String? {
        val current = mutable.value
        if (current.phase != QuizPhase.Resume) return null
        val session = current.session ?: return null
        mutable.value = current.copy(phase = if (session.completed) QuizPhase.Results else QuizPhase.Playing)
        return session.id.takeIf { session.completed }
    }

    fun restart() {
        if (!mutable.value.attached || job?.isActive == true) return
        val session = mutable.value.session
        if (session != null && session.hasProgress && !session.completed)
            mutable.value = mutable.value.copy(confirmRestart = true)
        else runWork(Work.Prepare)
    }
    fun cancelRestart() { mutable.value = mutable.value.copy(confirmRestart = false) }
    fun confirmRestart() { if (mutable.value.confirmRestart) runWork(Work.Prepare) }

    fun answer(sessionId: String, index: Int, option: PokemonId) {
        if (!foreground.value || mutable.value.phase != QuizPhase.Playing) return
        val old = mutable.value.session ?: return
        val updated = old.answer(sessionId, index, option)
        if (updated != old) publish(updated)
    }

    fun advance(sessionId: String, index: Int): String? {
        if (!foreground.value || mutable.value.phase != QuizPhase.Playing) return null
        val old = mutable.value.session ?: return null
        val updated = old.advance(sessionId, index)
        if (updated == old) return null
        publish(updated)
        if (updated.completed) mutable.value = mutable.value.copy(phase = QuizPhase.Results)
        return updated.id.takeIf { updated.completed }
    }

    fun showHints(sessionId: String, index: Int) {
        val session = mutable.value.session ?: return
        if (foreground.value && mutable.value.phase == QuizPhase.Playing && session.id == sessionId && session.index == index)
            publish(session.copy(hintsVisible = true))
    }

    fun imageFailed(sessionId: String, index: Int) {
        val current = mutable.value.session ?: return
        if (current.id == sessionId && current.index == index && mutable.value.phase in listOf(QuizPhase.Playing, QuizPhase.Results)) {
            lastWork = Work.Restore
            mutable.value = mutable.value.copy(phase = QuizPhase.Error, failure = DataFailure.InsufficientLocalData)
        }
    }

    private fun publish(session: QuizSession) {
        saved["quiz.snapshot"] = QuizSnapshotCodec.encode(session)
        mutable.value = mutable.value.copy(session = session)
    }

    private fun fail(reason: DataFailure) { mutable.value = mutable.value.copy(phase = QuizPhase.Error, failure = reason) }

    private fun runWork(work: Work) {
        if (job?.isActive == true || taskId == null) return
        lastWork = work
        mutable.value = mutable.value.copy(phase = QuizPhase.Preparing, failure = null, confirmRestart = false)
        job = viewModelScope.launch {
            if (work != Work.Prepare) {
                when (val result = media.reconcile(requireNotNull(taskId), mutable.value.session?.id)) {
                    is DataResult.Failure -> { fail(result.reason); return@launch }
                    is DataResult.Success -> mutable.value = mutable.value.copy(attached = true)
                }
            }
            if (work == Work.Attach) { mutable.value = mutable.value.copy(phase = QuizPhase.Idle); return@launch }
            if (work == Work.Restore) {
                val session = mutable.value.session ?: return@launch
                val ids = session.questions.map { it.correct.summary.identity.id }
                val restored = media.restore(session.id, ids)
                val result = if (restored is DataResult.Failure && restored.reason == DataFailure.InsufficientLocalData)
                    media.prepare(session.id, session.questions.map { it.correct.summary }) else restored
                when (result) {
                    is DataResult.Failure -> fail(result.reason)
                    is DataResult.Success -> {
                        while (!foreground.value) foreground.first { it }
                        acceptFiles(session, result.value, QuizPhase.Resume)
                    }
                }
                return@launch
            }
            val pool = when (val result = candidates.candidates()) {
                is DataResult.Failure -> { fail(result.reason); return@launch }
                is DataResult.Success -> result.value
            }
            val id = "quiz:${requireNotNull(taskId)}:${sessionIds.nextId()}"
            val next = withContext(dispatchers.default) { generateQuiz(id, pool.candidates, random) }
            if (next == null) { fail(DataFailure.InsufficientLocalData); return@launch }
            var published = false
            try {
                when (val result = media.prepare(id, next.questions.map { it.correct.summary })) {
                    is DataResult.Failure -> fail(result.reason)
                    is DataResult.Success -> {
                        // Publish only while RESUMED, before the next pause/state-save cycle.
                        // Keep both owners pinned while waiting; no snapshot replacement in background.
                        while (!foreground.value) foreground.first { it }
                        val previous = mutable.value.session
                        if (acceptFiles(next, result.value, if (active) QuizPhase.Playing else QuizPhase.Resume)) {
                            publish(next)
                            published = true
                            if (previous != null) {
                                val released = media.release(previous.id)
                                if (released is DataResult.Failure) mutable.value = mutable.value.copy(recoveryNotice = true)
                            }
                        }
                    }
                }
            } finally {
                if (!published) withContext(NonCancellable) { media.release(id) }
            }
        }
    }

    private fun acceptFiles(session: QuizSession, files: List<QuizMedia>, phase: QuizPhase): Boolean {
        val expected = session.questions.map { it.correct.summary.identity.id }.toSet()
        if (files.size != 5 || files.map { it.pokemonId }.toSet() != expected) {
            fail(DataFailure.InvalidData); return false
        }
        mutable.value = mutable.value.copy(files = files.associate { it.pokemonId to it.localFile }, phase = phase)
        return true
    }
}
