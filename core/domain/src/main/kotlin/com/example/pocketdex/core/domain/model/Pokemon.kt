package com.example.pocketdex.core.domain.model

/** Species ID, never an index, localized name or alternate-variety ID. */
@JvmInline
value class PokemonId(val value: Int) {
    init { require(value > 0) }
}

enum class PokemonLanguage { English, Spanish }

data class LocalizedPokemonNames(val english: String? = null, val spanish: String? = null) {
    init {
        require(english == null || english.isNotBlank())
        require(spanish == null || spanish.isNotBlank())
    }
}

data class PokemonIdentity(
    val id: PokemonId,
    val name: String,
    val nationalNumber: Int,
    val localizedNames: LocalizedPokemonNames = LocalizedPokemonNames(),
) {
    init {
        require(name.isNotBlank())
        require(nationalNumber > 0)
    }

    fun visibleName(language: PokemonLanguage): String = when (language) {
        PokemonLanguage.English -> localizedNames.english ?: name
        PokemonLanguage.Spanish -> localizedNames.spanish ?: localizedNames.english ?: name
    }
}

enum class PokemonType {
    Normal, Fighting, Flying, Poison, Ground, Rock, Bug, Ghost, Steel,
    Fire, Water, Grass, Electric, Psychic, Ice, Dragon, Dark, Fairy,
}

/** Type order is meaningful: the first type supplies the visual accent. Empty means unknown. */
data class PokemonSummary(
    val identity: PokemonIdentity,
    val types: List<PokemonType>,
    val imageUrl: String?,
    val localImageFile: String? = null,
) {
    init {
        require(types.size <= 2 && types.distinct().size == types.size)
        require(imageUrl == null || imageUrl.isNotBlank())
        require(localImageFile == null || localImageFile.isNotBlank())
    }
}

enum class PokemonStat { Hp, Attack, Defense, SpecialAttack, SpecialDefense, Speed }

/** Missing data is null. Values above the visual reference 255 remain valid. */
data class PokemonStatistics(
    val hp: Int? = null,
    val attack: Int? = null,
    val defense: Int? = null,
    val specialAttack: Int? = null,
    val specialDefense: Int? = null,
    val speed: Int? = null,
) {
    init {
        require(listOf(hp, attack, defense, specialAttack, specialDefense, speed).all { it == null || it >= 0 })
    }

    operator fun get(stat: PokemonStat): Int? = when (stat) {
        PokemonStat.Hp -> hp
        PokemonStat.Attack -> attack
        PokemonStat.Defense -> defense
        PokemonStat.SpecialAttack -> specialAttack
        PokemonStat.SpecialDefense -> specialDefense
        PokemonStat.Speed -> speed
    }

    val isComplete: Boolean get() = PokemonStat.entries.all { this[it] != null }
}

data class PokemonDetail(val summary: PokemonSummary, val statistics: PokemonStatistics)
