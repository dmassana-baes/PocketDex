package com.example.pocketdex.core.domain.repository

import com.example.pocketdex.core.domain.model.CatalogCoverage
import com.example.pocketdex.core.domain.model.DataOrigin
import com.example.pocketdex.core.domain.model.DataResult
import com.example.pocketdex.core.domain.model.PokemonDetail
import com.example.pocketdex.core.domain.model.PokemonId
import com.example.pocketdex.core.domain.model.PokemonSummary

data class QuizCandidatePool(
    val candidates: List<PokemonDetail>,
    val origin: DataOrigin,
    val coverage: CatalogCoverage,
) {
    init { require(candidates.map { it.summary.identity.id }.distinct().size == candidates.size) }
}

interface QuizCandidateRepository {
    suspend fun candidates(): DataResult<QuizCandidatePool>
}

/** Data owns the file and verifies it before returning; no Android Uri or Bitmap in domain. */
data class QuizMedia(val pokemonId: PokemonId, val localFile: String) {
    init { require(localFile.isNotBlank()) }
}

interface QuizMediaRepository {
    /** Release orphaned quiz owners, preserving other recoverable tasks and favorites. */
    suspend fun reconcile(taskId: Int, currentSessionId: String?): DataResult<Unit>
    /** All requested images must be verified and protected independently of favorites before success. */
    suspend fun prepare(sessionId: String, pokemon: List<PokemonSummary>): DataResult<List<QuizMedia>>
    /** Revalidate the same IDs/media; never generate replacement questions during recovery. */
    suspend fun restore(sessionId: String, pokemonIds: List<PokemonId>): DataResult<List<QuizMedia>>
    suspend fun release(sessionId: String): DataResult<Unit>
}
