package com.example.pocketdex.core.domain.compare

import com.example.pocketdex.core.domain.model.*

data class ComparePair(val left: PokemonId? = null, val right: PokemonId? = null) {
    init { require(left == null || right == null || left != right) }
    operator fun get(slot: Int): PokemonId? = when (slot) { 0 -> left; 1 -> right; else -> error("Invalid slot") }
    fun select(slot: Int, id: PokemonId): ComparePair? {
        require(slot in 0..1)
        if (this[1 - slot] == id) return null
        return if (slot == 0) copy(left = id) else copy(right = id)
    }
    fun remove(slot: Int): ComparePair {
        require(slot in 0..1)
        return if (slot == 0) copy(left = null) else copy(right = null)
    }
}

enum class StatWinner { Left, Right, Tie }
data class ComparedStat(val stat: PokemonStat, val left: Int, val right: Int, val winner: StatWinner)
data class PokemonComparison(val left: PokemonId, val right: PokemonId, val rows: List<ComparedStat>) {
    init { require(left != right && rows.map { it.stat } == PokemonStat.entries.toList()) }
}

/** Only complete, distinct species can be compared; the visual scale never affects the winner. */
fun comparePokemon(left: PokemonDetail, right: PokemonDetail): PokemonComparison? {
    if (left.summary.identity.id == right.summary.identity.id || !left.statistics.isComplete || !right.statistics.isComplete) return null
    return PokemonComparison(left.summary.identity.id, right.summary.identity.id, PokemonStat.entries.map { stat ->
        val a = requireNotNull(left.statistics[stat])
        val b = requireNotNull(right.statistics[stat])
        ComparedStat(stat, a, b, when { a > b -> StatWinner.Left; a < b -> StatWinner.Right; else -> StatWinner.Tie })
    })
}
