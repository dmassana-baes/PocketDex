package com.example.pocketdex.feature.compare.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketdex.core.domain.compare.*
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.flow.*

data class CompareSlot(val id: PokemonId? = null, val detail: PokemonDetail? = null, val loading: Boolean = false, val failure: DataFailure? = null)
data class CompareUiState(
    val slots: List<CompareSlot> = listOf(CompareSlot(), CompareSlot()),
    val editingSlot: Int? = null,
    val duplicate: Boolean = false,
    val result: PokemonComparison? = null,
) {
    val canCompare: Boolean get() = slots.all { !it.loading && it.detail?.statistics?.isComplete == true } && slots[0].id != slots[1].id
}

/** App retains this owner across selection/selector/results entries. */
@HiltViewModel
class CompareViewModel @Inject constructor(private val pokemon: PokemonRepository, private val saved: SavedStateHandle) : ViewModel() {
    private fun savedId(key: String) = saved.get<Int>(key)?.takeIf { it > 0 }?.let(::PokemonId)
    private val left = savedId("compare.left")
    private val right = savedId("compare.right")?.takeIf { it != left }
    private val mutable = MutableStateFlow(CompareUiState(slots = listOf(CompareSlot(left), CompareSlot(right)),
        editingSlot = saved.get<Int>("compare.editing")?.takeIf { it in 0..1 }))
    val state = mutable.asStateFlow()
    private val jobs = arrayOfNulls<Job>(2)
    private val generations = intArrayOf(0, 0)
    private var requested: Pair<PokemonId, PokemonId>? = null
    private val reads = Semaphore(2)
    init { listOf(left, right).forEachIndexed { slot, id -> if (id != null) load(slot, id) } }

    fun editSlot(slot: Int): PokemonId? {
        require(slot in 0..1)
        saved["compare.editing"] = slot
        mutable.update { it.copy(editingSlot = slot, duplicate = false) }
        return state.value.slots[1 - slot].id
    }
    fun cancelEditing() { saved["compare.editing"] = null; mutable.update { it.copy(editingSlot = null) } }

    fun select(slot: Int, id: PokemonId): Boolean {
        val pair = ComparePair(state.value.slots[0].id, state.value.slots[1].id)
        if (pair.select(slot, id) == null) { mutable.update { it.copy(duplicate = true) }; return false }
        cancelEditing()
        if (state.value.slots[slot].id == id) return true
        invalidateResult()
        updateSlot(slot, CompareSlot(id))
        savePair()
        load(slot, id)
        return true
    }
    fun remove(slot: Int) {
        require(slot in 0..1)
        ComparePair(state.value.slots[0].id, state.value.slots[1].id).remove(slot)
        jobs[slot]?.cancel(); generations[slot]++
        invalidateResult()
        updateSlot(slot, CompareSlot())
        savePair()
    }
    fun retry(slot: Int) { state.value.slots[slot].id?.let { if (jobs[slot]?.isActive != true) load(slot, it) } }

    private fun load(slot: Int, id: PokemonId) {
        jobs[slot]?.cancel()
        val generation = ++generations[slot]
        updateSlot(slot, state.value.slots[slot].copy(loading = true, failure = null))
        jobs[slot] = viewModelScope.launch { reads.withPermit {
            val cached = pokemon.cachedDetail(id)
            if (!current(slot, id, generation)) return@launch
            var detail = (cached as? DataResult.Success)?.value
            if (detail != null && detail.summary.identity.id != id) {
                updateSlot(slot, CompareSlot(id, failure = DataFailure.InvalidData)); return@withPermit
            }
            if (detail != null) updateSlot(slot, CompareSlot(id, detail, loading = !detail.statistics.isComplete))
            if (detail?.statistics?.isComplete != true) {
                when (val result = pokemon.refresh(id)) {
                    is DataResult.Failure -> if (current(slot, id, generation)) updateSlot(slot, CompareSlot(id, detail, failure = result.reason))
                    is DataResult.Success -> if (current(slot, id, generation)) {
                        val refreshed = result.value
                        if (refreshed?.summary?.identity?.id != id) updateSlot(slot, CompareSlot(id, detail, failure = DataFailure.InvalidData))
                        else {
                            val updated = requireNotNull(refreshed)
                            detail = updated
                            updateSlot(slot, CompareSlot(id, updated, failure = if (updated.statistics.isComplete) null else DataFailure.InsufficientLocalData))
                        }
                    }
                }
            }
            if (current(slot, id, generation)) updateResult()
        } }
    }
    private fun current(slot: Int, id: PokemonId, generation: Int) = generations[slot] == generation && state.value.slots[slot].id == id
    private fun updateSlot(slot: Int, value: CompareSlot) { mutable.update { it.copy(slots = it.slots.mapIndexed { index, old -> if (index == slot) value else old }) } }
    private fun savePair() { saved["compare.left"] = state.value.slots[0].id?.value; saved["compare.right"] = state.value.slots[1].id?.value }
    private fun invalidateResult() { requested = null; mutable.update { it.copy(result = null, duplicate = false) } }
    fun compare(): Pair<PokemonId, PokemonId>? {
        if (!state.value.canCompare) return null
        val a = state.value.slots[0].detail!!; val b = state.value.slots[1].detail!!
        val result = comparePokemon(a, b) ?: return null
        requested = result.left to result.right
        mutable.update { it.copy(result = result) }
        return requested
    }
    fun restoreResults(left: PokemonId, right: PokemonId) {
        if (state.value.slots[0].id != left || state.value.slots[1].id != right) return
        requested = left to right
        updateResult()
    }
    private fun updateResult() {
        val pair = requested ?: return
        if (state.value.slots[0].id == pair.first && state.value.slots[1].id == pair.second && state.value.canCompare)
            mutable.update { it.copy(result = comparePokemon(it.slots[0].detail!!, it.slots[1].detail!!)) }
    }
}
