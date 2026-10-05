package com.example.pocketdex.core.data.catalog

import com.example.pocketdex.core.data.local.PokemonEntity
import com.example.pocketdex.core.data.remote.PokemonDto
import com.example.pocketdex.core.data.remote.resourceId
import com.example.pocketdex.core.domain.model.*

internal fun PokemonEntity.detail(): PokemonDetail = PokemonDetail(
    PokemonSummary(PokemonIdentity(PokemonId(speciesId), canonicalName, nationalNumber,
        LocalizedPokemonNames(englishName, spanishName)), types.split(',').filter(String::isNotBlank).map(PokemonType::valueOf), imageUrl, localImageFile),
    PokemonStatistics(hp, attack, defense, specialAttack, specialDefense, speed),
)

internal fun PokemonDetail.entity(now: Long): PokemonEntity {
    val identity = summary.identity
    return PokemonEntity(identity.id.value, identity.name, identity.nationalNumber, identity.localizedNames.english,
        identity.localizedNames.spanish, summary.types.joinToString(",") { it.name }, summary.imageUrl, summary.localImageFile,
        statistics.hp, statistics.attack, statistics.defense, statistics.specialAttack, statistics.specialDefense, statistics.speed, now)
}

internal fun PokemonDto.detail(index: CatalogIndexEntry): PokemonDetail {
    require(isDefault && id == index.defaultPokemonId && resourceId(species.url) == index.speciesId)
    require(types.map { it.slot }.distinct().size == types.size)
    val values = stats.associate { it.stat.name to it.value }
    require(values.size == stats.size)
    return PokemonDetail(index.summary().copy(
        types = types.sortedBy { it.slot }.map { type -> PokemonType.entries.first { it.name.equals(type.type.name, true) } },
        imageUrl = sprites.other?.artwork?.front ?: sprites.front,
    ), PokemonStatistics(values["hp"], values["attack"], values["defense"], values["special-attack"], values["special-defense"], values["speed"]))
}
