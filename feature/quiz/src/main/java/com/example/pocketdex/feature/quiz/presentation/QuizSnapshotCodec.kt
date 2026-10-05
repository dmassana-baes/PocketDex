package com.example.pocketdex.feature.quiz.presentation

import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.quiz.*
import kotlinx.serialization.json.*

/** Small versioned task snapshot; no bitmap, IO, RNG or repeated score evaluation. */
object QuizSnapshotCodec {
    fun encode(session: QuizSession): String = buildJsonObject {
        put("version", 1); put("id", session.id); put("index", session.index)
        put("hints", session.hintsVisible); put("completed", session.completed)
        put("answers", JsonArray(session.answers.map { it?.let { JsonPrimitive(it.value) } ?: JsonNull }))
        put("questions", JsonArray(session.questions.map { question -> buildJsonObject {
            put("correct", pokemon(question.correct))
            put("options", JsonArray(question.options.map(::pokemon)))
        } }))
    }.toString()

    fun decode(text: String?): QuizSession? {
        if (text == null || text.length > 100_000) return null
        return try {
            val root = Json.parseToJsonElement(text).jsonObject
            require(root.getValue("version").jsonPrimitive.int == 1)
            QuizSession(root.getValue("id").jsonPrimitive.content,
                root.getValue("questions").jsonArray.map { element ->
                    val question = element.jsonObject
                    QuizQuestion(readPokemon(question.getValue("correct")), question.getValue("options").jsonArray.map(::readPokemon))
                }, root.getValue("index").jsonPrimitive.int,
                root.getValue("answers").jsonArray.map { if (it == JsonNull) null else PokemonId(it.jsonPrimitive.int) },
                root.getValue("hints").jsonPrimitive.boolean, root.getValue("completed").jsonPrimitive.boolean)
        } catch (_: IllegalArgumentException) { null }
        catch (_: NoSuchElementException) { null }
        catch (_: IllegalStateException) { null }
    }

    private fun pokemon(detail: PokemonDetail) = buildJsonObject {
        val summary = detail.summary; val identity = summary.identity
        put("id", identity.id.value); put("number", identity.nationalNumber); put("name", identity.name)
        put("en", identity.localizedNames.english?.let(::JsonPrimitive) ?: JsonNull)
        put("es", identity.localizedNames.spanish?.let(::JsonPrimitive) ?: JsonNull)
        put("types", JsonArray(summary.types.map { JsonPrimitive(it.name) }))
        put("url", summary.imageUrl?.let(::JsonPrimitive) ?: JsonNull)
        put("file", summary.localImageFile?.let(::JsonPrimitive) ?: JsonNull)
        put("stats", JsonArray(PokemonStat.entries.map { JsonPrimitive(requireNotNull(detail.statistics[it])) }))
    }

    private fun readPokemon(element: JsonElement): PokemonDetail {
        val data = element.jsonObject
        val stats = data.getValue("stats").jsonArray.map { it.jsonPrimitive.int }
        require(stats.size == 6)
        return PokemonDetail(PokemonSummary(PokemonIdentity(PokemonId(data.getValue("id").jsonPrimitive.int),
            data.getValue("name").jsonPrimitive.content, data.getValue("number").jsonPrimitive.int,
            LocalizedPokemonNames(data.getValue("en").jsonPrimitive.contentOrNull, data.getValue("es").jsonPrimitive.contentOrNull)),
            data.getValue("types").jsonArray.map { PokemonType.valueOf(it.jsonPrimitive.content) },
            data.getValue("url").jsonPrimitive.contentOrNull, data.getValue("file").jsonPrimitive.contentOrNull),
            PokemonStatistics(stats[0], stats[1], stats[2], stats[3], stats[4], stats[5]))
    }
}
