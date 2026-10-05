package com.example.pocketdex.core.domain.model

import java.util.Locale

sealed interface PokemonSearch {
    data object All : PokemonSearch
    data class Name internal constructor(val normalized: String) : PokemonSearch
    data class Number internal constructor(val value: Int) : PokemonSearch
    data object NoMatch : PokemonSearch

    companion object {
        fun parse(input: String): PokemonSearch {
            val query = input.trim()
            if (query.isEmpty()) return All
            val digits = query.removePrefix("#")
            if (digits.isNotEmpty() && digits.all { it in '0'..'9' }) {
                val number = digits.trimStart('0').toIntOrNull()
                return if (number != null && number > 0) Number(number) else NoMatch
            }
            return Name(query.lowercase(Locale.ROOT))
        }
    }
}

data class CatalogQuery(
    val search: PokemonSearch = PokemonSearch.All,
    val type: PokemonType? = null,
    val language: PokemonLanguage = PokemonLanguage.English,
) {
    /** Name/number and the single selected type must both match. */
    fun matches(pokemon: PokemonSummary): Boolean {
        val matchesSearch = when (val term = search) {
            PokemonSearch.All -> true
            PokemonSearch.NoMatch -> false
            is PokemonSearch.Name -> listOf(pokemon.identity.name, pokemon.identity.visibleName(language))
                .any { it.lowercase(Locale.ROOT).contains(term.normalized) }
            is PokemonSearch.Number -> pokemon.identity.nationalNumber == term.value
        }
        return matchesSearch && (type == null || type in pokemon.types)
    }
}

/** Opaque continuation is valid only for the query that produced it. */
data class PageRequest(val cursor: String? = null, val size: Int = 30, val anchorId: PokemonId? = null, val offset: Int? = null) {
    init {
        require(size > 0)
        require(cursor == null || cursor.isNotBlank())
        require(offset == null || offset >= 0)
    }
}

data class CatalogPage(
    val items: List<PokemonSummary>,
    val nextCursor: String?,
    val origin: DataOrigin,
    val coverage: CatalogCoverage,
    val previousCursor: String? = null,
    val itemsBefore: Int = 0,
    val itemsAfter: Int = 0,
) {
    init {
        require(items.map { it.identity.id }.distinct().size == items.size)
        require(nextCursor == null || nextCursor.isNotBlank())
        require(itemsBefore >= 0 && itemsAfter >= 0)
    }
    val endReached: Boolean get() = nextCursor == null
}

/** Index metadata is not cached content. An empty partial query can coexist with other saved Pokémon. */
data class CachedCatalog(val items: List<PokemonSummary>, val totalStored: Int, val coverage: CatalogCoverage)
