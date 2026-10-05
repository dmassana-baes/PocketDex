package com.example.pocketdex.core.data.repository

import com.example.pocketdex.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class FavoriteMergeTest {
    private val summary = PokemonSummary(PokemonIdentity(PokemonId(25), "pikachu", 25), listOf(PokemonType.Electric), null)

    @Test fun oldUiSnapshotCannotReplaceNewStatisticsOrMedia() {
        val current = PokemonDetail(summary.copy(localImageFile = "new.image"), PokemonStatistics(1, 2, 3, 4, 5, 6))
        val old = PokemonDetail(summary.copy(localImageFile = "old.image"), PokemonStatistics(hp = 30))
        assertEquals(current, current.enrichedBy(old))
    }
    @Test fun fillsOnlyHolesAndPreservesRealZero() {
        val partial = PokemonDetail(summary.copy(types = emptyList()), PokemonStatistics(hp = 0))
        val supplied = PokemonDetail(summary.copy(imageUrl = "https://example.org/pikachu.png"), PokemonStatistics(hp = 35, speed = 90))
        val merged = partial.enrichedBy(supplied)
        assertEquals(0, merged.statistics.hp)
        assertEquals(90, merged.statistics.speed)
        assertEquals(summary.types, merged.summary.types)
        assertNull(merged.statistics.defense)
    }
}
