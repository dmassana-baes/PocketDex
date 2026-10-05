package com.example.pocketdex.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonDataTest {
    @Test fun missingStatsStayMissingAndLargeValuesStayReal() {
        val stats = PokemonStatistics(hp = 300, specialDefense = 0)
        assertEquals(300, stats[PokemonStat.Hp])
        assertEquals(0, stats[PokemonStat.SpecialDefense])
        assertNull(stats[PokemonStat.Speed])
        assertFalse(stats.isComplete)
    }

    @Test fun completeStatisticsRequireAllSixIdentifiedFields() {
        assertTrue(PokemonStatistics(1, 2, 3, 4, 5, 6).isComplete)
        assertFalse(PokemonStatistics(1, 2, 3, 4, 5).isComplete)
    }

    @Test fun favoriteCanKeepOnlyIdWithoutInventedData() {
        val favorite = FavoritePokemon(KnownPokemon(PokemonId(25)), 123, false)
        assertFalse(favorite.hasCompleteData)
        assertNull(favorite.known.summary)
        assertNull(favorite.known.detail)
    }

    @Test(expected = IllegalArgumentException::class)
    fun favoriteCannotContainAnotherPokemonsData() {
        KnownPokemon(PokemonId(25), PokemonSummary(PokemonIdentity(PokemonId(1), "bulbasaur", 1), emptyList(), null))
    }

    @Test fun emptyCatalogHasExplicitCoverageAndEnd() {
        val page = CatalogPage(emptyList(), null, DataOrigin.Local, CatalogCoverage.Partial)
        assertTrue(page.endReached)
        assertEquals(CatalogCoverage.Partial, page.coverage)
    }
}
