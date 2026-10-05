package com.example.pocketdex.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogQueryTest {
    private val pikachu = PokemonSummary(PokemonIdentity(PokemonId(25), "pikachu", 25), listOf(PokemonType.Electric), null)
    private val bulbasaur = PokemonSummary(PokemonIdentity(PokemonId(1), "bulbasaur", 1), listOf(PokemonType.Grass, PokemonType.Poison), null)

    @Test fun nameIsCaseInsensitiveSubstringWithTrim() {
        assertTrue(CatalogQuery(PokemonSearch.parse("  KAChU  ")).matches(pikachu))
        assertFalse(CatalogQuery(PokemonSearch.parse("  KAChU  ")).matches(bulbasaur))
    }

    @Test fun numberIsExactAndAcceptsPaddingAndHash() {
        listOf("25", "025", "#025", " #00025 ").forEach {
            assertEquals(PokemonSearch.parse("25"), PokemonSearch.parse(it))
            assertTrue(CatalogQuery(PokemonSearch.parse(it)).matches(pikachu))
        }
        assertFalse(CatalogQuery(PokemonSearch.parse("2")).matches(pikachu))
    }

    @Test fun typeAndSearchMustBothMatchIncludingSecondType() {
        assertTrue(CatalogQuery(PokemonSearch.parse("bulb"), PokemonType.Poison).matches(bulbasaur))
        assertFalse(CatalogQuery(PokemonSearch.parse("bulb"), PokemonType.Electric).matches(bulbasaur))
        assertFalse(CatalogQuery(PokemonSearch.parse("pikachu"), PokemonType.Grass).matches(bulbasaur))
    }

    @Test fun blankClearsSearchWhileInvalidNumbersNeverMatch() {
        assertTrue(CatalogQuery(PokemonSearch.parse("   ")).matches(pikachu))
        listOf("0", "#000", "99999999999999999999").forEach {
            assertEquals(PokemonSearch.NoMatch, PokemonSearch.parse(it))
            assertFalse(CatalogQuery(PokemonSearch.parse(it)).matches(pikachu))
        }
    }

    @Test fun localizedVisibleAndCanonicalNamesAreBothSearchableWithTypeAnd() {
        val localized = PokemonSummary(
            PokemonIdentity(PokemonId(122), "mr-mime", 122, LocalizedPokemonNames("Mr. Mime", "Sr. Mime")),
            listOf(PokemonType.Psychic, PokemonType.Fairy), null,
        )
        assertTrue(CatalogQuery(PokemonSearch.parse(" SR. MIME "), PokemonType.Fairy, PokemonLanguage.Spanish).matches(localized))
        assertTrue(CatalogQuery(PokemonSearch.parse("mr-mime"), PokemonType.Psychic, PokemonLanguage.Spanish).matches(localized))
        assertTrue(CatalogQuery(PokemonSearch.parse("mr. mime"), language = PokemonLanguage.English).matches(localized))
        assertFalse(CatalogQuery(PokemonSearch.parse("sr. mime"), language = PokemonLanguage.English).matches(localized))
        assertFalse(CatalogQuery(PokemonSearch.parse("sr. mime"), PokemonType.Fire, PokemonLanguage.Spanish).matches(localized))
    }

    @Test fun absentLocalizedNameFallsBackToEnglishThenCanonical() {
        val identity = PokemonIdentity(PokemonId(122), "mr-mime", 122, LocalizedPokemonNames(english = "Mr. Mime"))
        assertEquals("Mr. Mime", identity.visibleName(PokemonLanguage.Spanish))
        assertEquals("pikachu", pikachu.identity.visibleName(PokemonLanguage.Spanish))
    }
}
