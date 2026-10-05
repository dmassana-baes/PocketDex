package com.example.pocketdex.core.domain.compare

import com.example.pocketdex.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class PokemonComparisonTest {
    private fun detail(id: Int, stats: PokemonStatistics) = PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(id), "pokemon-$id", id), emptyList(), null), stats)
    @Test fun pairRejectsDuplicateAndAllowsReplacingAndRemovingEitherSlot() {
        val a = PokemonId(1); val b = PokemonId(2)
        val one = ComparePair().select(0, a)!!
        assertNull(one.select(1, a))
        val pair = one.select(1, b)!!
        assertNull(pair.select(0, b))
        assertEquals(ComparePair(null, b), pair.remove(0))
        assertEquals(ComparePair(a, null), pair.remove(1))
        assertEquals(ComparePair(PokemonId(3), b), pair.select(0, PokemonId(3)))
    }
    @Test fun comparisonUsesSixRealValuesIncludingTieZeroAndAbove255() {
        val result = comparePokemon(detail(1, PokemonStatistics(0, 300, 10, 20, 30, 40)), detail(2, PokemonStatistics(0, 256, 11, 20, 29, 41)))!!
        assertEquals(PokemonStat.entries.toList(), result.rows.map { it.stat })
        assertEquals(listOf(StatWinner.Tie, StatWinner.Left, StatWinner.Right, StatWinner.Tie, StatWinner.Left, StatWinner.Right), result.rows.map { it.winner })
        assertEquals(300, result.rows[1].left)
        assertEquals(256, result.rows[1].right)
    }
    @Test fun missingStatsAndSameSpeciesCannotProduceComparison() {
        val full = detail(1, PokemonStatistics(1, 2, 3, 4, 5, 6))
        assertNull(comparePokemon(full, full))
        assertNull(comparePokemon(full, detail(2, PokemonStatistics(hp = 0))))
    }
}
