package com.example.pocketdex.core.data.catalog

import com.example.pocketdex.core.data.remote.*
import com.example.pocketdex.core.domain.model.PokemonType
import org.junit.Assert.*
import org.junit.Test

class PokemonMappingTest {
    private val entry = CatalogIndexEntry(25, 25, "pikachu", "Pikachu", "Pikachu", 1025, listOf("Electric"))
    private fun response() = PokemonDto(1025, true, ResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon-species/25/"),
        listOf(TypeDto(2, ResourceDto("steel", "unused")), TypeDto(1, ResourceDto("electric", "unused"))),
        listOf(StatDto(300, ResourceDto("speed", "unused")), StatDto(0, ResourceDto("hp", "unused")),
            StatDto(55, ResourceDto("attack", "unused"))), SpritesDto())

    @Test fun mapsKeysInsteadOfPositionAndKeepsMissingDistinctFromZero() {
        val detail = response().detail(entry)
        assertEquals(0, detail.statistics.hp)
        assertEquals(55, detail.statistics.attack)
        assertEquals(300, detail.statistics.speed)
        assertNull(detail.statistics.defense)
        assertNull(detail.statistics.specialAttack)
        assertFalse(detail.statistics.isComplete)
        assertEquals(listOf(PokemonType.Electric, PokemonType.Steel), detail.summary.types)
        assertEquals(25, detail.summary.identity.id.value)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsDifferentSpecies() {
        response().copy(species = ResourceDto("other", "https://pokeapi.co/api/v2/pokemon-species/26/")).detail(entry)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsDuplicateStatKeys() {
        val dto = response()
        dto.copy(stats = dto.stats + dto.stats.first()).detail(entry)
    }
}
