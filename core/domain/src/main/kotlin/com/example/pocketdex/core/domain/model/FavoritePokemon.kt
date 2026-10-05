package com.example.pocketdex.core.domain.model

/** Marking can succeed with only an ID or summary; completing data is a separate operation. */
data class KnownPokemon(val id: PokemonId, val summary: PokemonSummary? = null, val detail: PokemonDetail? = null) {
    init {
        require(summary == null || summary.identity.id == id)
        require(detail == null || detail.summary.identity.id == id)
        require(summary == null || detail == null || summary == detail.summary)
    }
}

data class FavoritePokemon(
    val known: KnownPokemon,
    val addedAtEpochMillis: Long,
    val imageAvailableLocally: Boolean,
) {
    val hasCompleteData: Boolean
        get() = known.detail?.let { it.summary.types.isNotEmpty() && it.statistics.isComplete } == true
}
