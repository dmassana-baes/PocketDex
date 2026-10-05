package com.example.pocketdex.core.data.catalog

import com.example.pocketdex.core.data.remote.DexDto
import com.example.pocketdex.core.data.remote.resourceId
import com.example.pocketdex.core.domain.model.*
import kotlinx.serialization.Serializable

/** Parses quoted CSV fields, escaped quotes, CRLF and embedded line breaks. */
internal fun csvRows(text: String): List<Map<String, String>> {
    val rows = mutableListOf<List<String>>()
    val row = mutableListOf<String>()
    val field = StringBuilder()
    var quoted = false
    var index = 0
    fun endField() { row.add(field.toString()); field.setLength(0) }
    fun endRow() { endField(); rows.add(row.toList()); row.clear() }
    while (index < text.length) {
        val c = text[index]
        when {
            c == '"' && quoted && text.getOrNull(index + 1) == '"' -> { field.append('"'); index++ }
            c == '"' -> quoted = !quoted
            c == ',' && !quoted -> endField()
            c == '\n' && !quoted -> endRow()
            c == '\r' && !quoted -> Unit
            else -> field.append(c)
        }
        index++
    }
    require(!quoted) { "Unclosed CSV field" }
    if (field.isNotEmpty() || row.isNotEmpty()) endRow()
    val header = rows.firstOrNull() ?: throw IllegalArgumentException("Missing CSV header")
    require(header.distinct().size == header.size)
    return rows.drop(1).filter { it.any(String::isNotEmpty) }.map {
        require(it.size == header.size)
        header.zip(it).toMap()
    }
}

@Serializable
data class CatalogIndexEntry(
    val speciesId: Int, val nationalNumber: Int, val canonicalName: String,
    val englishName: String?, val spanishName: String?, val defaultPokemonId: Int?, val types: List<String>,
) {
    fun summary(): PokemonSummary = PokemonSummary(
        PokemonIdentity(PokemonId(speciesId), canonicalName, nationalNumber, LocalizedPokemonNames(englishName, spanishName)),
        types.map(PokemonType::valueOf), null,
    )
}

/** CSV names/types are metadata only, intersected with the live national dex. No alternative forms. */
internal fun buildIndex(dex: DexDto, pokemonCsv: String, typesCsv: String, namesCsv: String): List<CatalogIndexEntry> {
    val defaults = csvRows(pokemonCsv).filter { it.getValue("is_default") == "1" }.groupBy { it.getValue("species_id").toInt() }
    require(defaults.isNotEmpty())
    require(defaults.values.all { it.size == 1 })
    val types = csvRows(typesCsv).groupBy { it.getValue("pokemon_id").toInt() }
    val names = csvRows(namesCsv).groupBy { it.getValue("pokemon_species_id").toInt() }
    return dex.entries.sortedBy { it.number }.map { entry ->
        val id = resourceId(entry.species.url)
        val default = defaults[id]?.single()
        val pokemonId = default?.getValue("id")?.toInt()
        val localized = names[id].orEmpty()
        fun name(languageId: String): String? = localized.firstOrNull { it.getValue("local_language_id") == languageId }?.getValue("name")
        val orderedTypes = types[pokemonId].orEmpty().sortedBy { it.getValue("slot").toInt() }.map {
            // PokéAPI's 18 supported type IDs; enum order is explicitly the same API order.
            PokemonType.entries[it.getValue("type_id").toInt() - 1].name
        }
        CatalogIndexEntry(id, entry.number, entry.species.name, name("9"), name("7"), pokemonId, orderedTypes)
    }.also { result ->
        require(result.isNotEmpty())
        require(result.map { it.speciesId }.distinct().size == result.size)
        require(result.map { it.nationalNumber }.distinct().size == result.size)
    }
}
