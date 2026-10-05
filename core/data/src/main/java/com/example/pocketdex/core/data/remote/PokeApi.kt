package com.example.pocketdex.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface PokeApi {
    @GET("pokedex/1/") suspend fun nationalDex(): DexDto
    @GET("pokemon-species/{id}/") suspend fun species(@Path("id") id: Int): SpeciesDto
    @GET("pokemon/{id}/") suspend fun pokemon(@Path("id") id: Int): PokemonDto
}

interface IndexApi {
    @Streaming
    @GET("PokeAPI/pokeapi/master/data/v2/csv/{file}.csv")
    suspend fun csv(@Path("file") file: String): ResponseBody
}

@Serializable data class ResourceDto(val name: String, val url: String)
@Serializable data class DexEntryDto(@SerialName("entry_number") val number: Int, @SerialName("pokemon_species") val species: ResourceDto)
@Serializable data class DexDto(@SerialName("pokemon_entries") val entries: List<DexEntryDto>)
@Serializable data class NameDto(val name: String, val language: ResourceDto)
@Serializable data class VarietyDto(@SerialName("is_default") val isDefault: Boolean, val pokemon: ResourceDto)
@Serializable data class SpeciesDto(val id: Int, val name: String, val names: List<NameDto>, val varieties: List<VarietyDto>)
@Serializable data class TypeDto(val slot: Int, val type: ResourceDto)
@Serializable data class StatDto(@SerialName("base_stat") val value: Int, val stat: ResourceDto)
@Serializable data class ArtworkDto(@SerialName("front_default") val front: String? = null)
@Serializable data class OtherSpritesDto(@SerialName("official-artwork") val artwork: ArtworkDto? = null)
@Serializable data class SpritesDto(@SerialName("front_default") val front: String? = null, val other: OtherSpritesDto? = null)
@Serializable data class PokemonDto(
    val id: Int,
    @SerialName("is_default") val isDefault: Boolean,
    val species: ResourceDto,
    val types: List<TypeDto>,
    val stats: List<StatDto>,
    val sprites: SpritesDto,
)

fun resourceId(url: String): Int = url.trimEnd('/').substringAfterLast('/').toInt().also { require(it > 0) }
