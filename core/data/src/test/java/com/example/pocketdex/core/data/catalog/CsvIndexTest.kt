package com.example.pocketdex.core.data.catalog

import com.example.pocketdex.core.data.remote.*
import com.example.pocketdex.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class CsvIndexTest {
    @Test fun quotedNamesPreserveCommasQuotesAndLineBreaks() {
        val rows = csvRows("id,name\r\n1,\"a,b\"\r\n2,\"a\"\"b\nnext\"\r\n")
        assertEquals("a,b", rows[0]["name"])
        assertEquals("a\"b\nnext", rows[1]["name"])
    }

    @Test(expected = IllegalArgumentException::class)
    fun malformedMetadataIsRejected() { csvRows("id,name\n1,\"unfinished") }

    @Test fun nationalNumbersSpeciesAndDefaultVarietiesRemainDistinct() {
        val dex = DexDto(listOf(DexEntryDto(55, ResourceDto("example", "https://pokeapi.co/api/v2/pokemon-species/44/"))))
        val index = buildIndex(dex,
            "id,species_id,is_default\n900,44,1\n901,44,0\n902,45,1\n",
            "pokemon_id,type_id,slot\n900,3,2\n900,10,1\n901,11,1\n",
            "pokemon_species_id,local_language_id,name\n44,9,Example\n44,7,Ejemplo\n")
        assertEquals(1, index.size)
        val entry = index.single()
        assertEquals(900, entry.defaultPokemonId)
        assertEquals(PokemonId(44), entry.summary().identity.id)
        assertEquals(55, entry.summary().identity.nationalNumber)
        assertEquals(listOf(PokemonType.Fire, PokemonType.Flying), entry.summary().types)
        assertTrue(CatalogQuery(PokemonSearch.parse("jemp"), PokemonType.Flying, PokemonLanguage.Spanish).matches(entry.summary()))
        assertFalse(CatalogQuery(PokemonSearch.parse("jemp"), PokemonType.Water, PokemonLanguage.Spanish).matches(entry.summary()))
        assertTrue(CatalogQuery(PokemonSearch.parse("#0055")).matches(entry.summary()))
        assertFalse(CatalogQuery(PokemonSearch.parse("#0044")).matches(entry.summary()))
    }

    @Test fun filteringFindsMetadataBeyondFirstPage() {
        val entries = (1..100).map { CatalogIndexEntry(it, it, "pokemon-$it", null, null, it,
            listOf(if (it == 99) "Water" else "Fire")) }
        val query = CatalogQuery(PokemonSearch.parse("99"), PokemonType.Water)
        assertEquals(listOf(99), entries.filter { query.matches(it.summary()) }.map { it.speciesId })
    }

    @Test fun statisticsAreMappedByNameAndMissingValuesStayUnknown() {
        val entry = CatalogIndexEntry(44, 55, "example", "Example", null, 900, listOf("Fire"))
        val dto = PokemonDto(900, true, ResourceDto("example", "https://pokeapi.co/api/v2/pokemon-species/44/"),
            listOf(TypeDto(1, ResourceDto("fire", "unused"))),
            listOf(StatDto(80, ResourceDto("speed", "unused")), StatDto(120, ResourceDto("attack", "unused"))), SpritesDto())
        val detail = dto.detail(entry)
        assertEquals(80, detail.statistics.speed)
        assertEquals(120, detail.statistics.attack)
        assertNull(detail.statistics.hp)
        assertFalse(detail.statistics.isComplete)
    }
}
