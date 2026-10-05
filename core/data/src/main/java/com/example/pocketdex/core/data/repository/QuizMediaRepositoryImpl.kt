package com.example.pocketdex.core.data.repository

import androidx.room.withTransaction
import com.example.pocketdex.core.data.catalog.dataResult
import com.example.pocketdex.core.data.image.PrivateImageStore
import com.example.pocketdex.core.data.local.MediaRetentionEntity
import com.example.pocketdex.core.data.local.PocketDexDatabase
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.repository.*
import javax.inject.Inject
import javax.inject.Singleton
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.withLock

/** Session references are independent of a favorite mark; questions remain owned by quiz. */
@Singleton
class QuizMediaRepositoryImpl @Inject constructor(
    private val database: PocketDexDatabase,
    private val images: PrivateImageStore,
    @ApplicationContext private val context: Context,
) : QuizMediaRepository {
    @Suppress("DEPRECATION")
    override suspend fun reconcile(taskId: Int, currentSessionId: String?): DataResult<Unit> = dataResult {
        require(taskId >= 0)
        val tasks = context.getSystemService(ActivityManager::class.java).appTasks.mapNotNull { task ->
            val info = task.taskInfo ?: return@mapNotNull null
            if (Build.VERSION.SDK_INT >= 29) info.taskId else info.persistentId
        }.toSet() + taskId
        images.references.withLock {
            database.withTransaction {
                database.dao().mediaOwners().filter { owner ->
                    if (!owner.startsWith("quiz:")) false else {
                        val ownerTask = owner.split(':').getOrNull(1)?.toIntOrNull()
                        ownerTask !in tasks || (ownerTask == taskId && owner != currentSessionId)
                    }
                }.forEach { database.dao().release(it) }
            }
        }
    }
    override suspend fun prepare(sessionId: String, pokemon: List<PokemonSummary>): DataResult<List<QuizMedia>> = dataResult {
        require(sessionId.isNotBlank() && pokemon.isNotEmpty())
        require(pokemon.map { it.identity.id }.distinct().size == pokemon.size)
        val published = mutableListOf<String>()
        try {
            val records = pokemon.map { summary ->
                val existing = summary.localImageFile
                // Check/reuse and pin under the pruning lock. Downloads never hold that lock.
                val reused = images.references.withLock {
                    if (existing != null && images.verified(existing)) {
                        images.holdPublication(existing)
                        published += existing
                        existing
                    } else null
                }
                val path = reused ?: images.download(summary.identity.id.value, requireNotNull(summary.imageUrl)).also { published += it }
                QuizMedia(summary.identity.id, path)
            }
            images.references.withLock {
                database.withTransaction {
                    database.dao().release(sessionId)
                    database.dao().retain(records.map { MediaRetentionEntity(sessionId, it.pokemonId.value, it.localFile) })
                    records.forEach { record -> database.dao().pokemon(record.pokemonId.value)?.let { stored ->
                        database.dao().upsert(stored.copy(localImageFile = record.localFile))
                    } }
                }
                images.pruneUnprotected()
            }
            records
        } finally { published.forEach(images::finishPublication) }
    }

    override suspend fun restore(sessionId: String, pokemonIds: List<PokemonId>): DataResult<List<QuizMedia>> {
        if (sessionId.isBlank() || pokemonIds.isEmpty() || pokemonIds.distinct().size != pokemonIds.size)
            return DataResult.Failure(DataFailure.InvalidData)
        return images.references.withLock {
            val records = dataResult { database.dao().retained(sessionId) }
            if (records is DataResult.Failure) return@withLock records
            val stored = (records as DataResult.Success).value.associateBy { it.pokemonId }
            if (stored.keys != pokemonIds.map { it.value }.toSet()) return@withLock DataResult.Failure(DataFailure.InsufficientLocalData)
            if (pokemonIds.any { !images.verified(stored.getValue(it.value).path) })
                return@withLock DataResult.Failure(DataFailure.InsufficientLocalData)
            DataResult.Success(pokemonIds.map { QuizMedia(it, stored.getValue(it.value).path) })
        }
    }

    override suspend fun release(sessionId: String): DataResult<Unit> = dataResult {
        require(sessionId.isNotBlank())
        images.references.withLock { database.dao().release(sessionId) }
    }
}
