package com.example.pocketdex.core.data.repository

import com.example.pocketdex.core.data.catalog.CatalogIndexProvider
import com.example.pocketdex.core.data.catalog.CatalogRepositoryImpl
import com.example.pocketdex.core.data.catalog.dataResult
import com.example.pocketdex.core.data.catalog.detail
import com.example.pocketdex.core.data.image.PrivateImageStore
import com.example.pocketdex.core.data.local.PocketDexDatabase
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.quiz.canPrepareQuiz
import com.example.pocketdex.core.domain.repository.*
import com.example.pocketdex.core.domain.util.RandomSource
import com.example.pocketdex.core.domain.util.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizCandidateRepositoryImpl @Inject constructor(
    private val database: PocketDexDatabase,
    private val catalog: CatalogRepositoryImpl,
    private val index: CatalogIndexProvider,
    private val images: PrivateImageStore,
    private val random: RandomSource,
    private val dispatchers: DispatcherProvider,
) : QuizCandidateRepository {
    override suspend fun candidates(): DataResult<QuizCandidatePool> {
        val stored = dataResult { database.dao().pokemon().map { it.detail() } }
        if (stored is DataResult.Failure) return stored
        val local = (stored as DataResult.Success).value.filter { detail ->
            detail.summary.localImageFile?.let { images.verified(it) } == true
        }
        if (withContext(dispatchers.default) { canPrepareQuiz(local) })
            return DataResult.Success(QuizCandidatePool(local, DataOrigin.Local, CatalogCoverage.Partial))
        val remote = dataResult {
            val count = index.get().size
            val offset = random.nextInt((count - 29).coerceAtLeast(1))
            catalog.page(CatalogQuery(), PageRequest(size = 30, offset = offset))
        }
        val page = when (remote) {
            is DataResult.Success -> remote.value
            is DataResult.Failure -> return if (remote.reason == DataFailure.Storage) remote
                else DataResult.Failure(DataFailure.InsufficientLocalData)
        }
        if (page is DataResult.Failure) return if (page.reason == DataFailure.Storage) page
            else DataResult.Failure(DataFailure.InsufficientLocalData)
        val pageItems = (page as DataResult.Success).value.items
        val details = mutableListOf<PokemonDetail>()
        for (summary in pageItems) {
            val detail = catalog.cachedDetail(summary.identity.id)
            if (detail is DataResult.Success) detail.value?.let(details::add)
            else if (detail is DataResult.Failure && detail.reason == DataFailure.Storage) return detail
        }
        val pool = (details + local).distinctBy { it.summary.identity.id }
        return if (withContext(dispatchers.default) { canPrepareQuiz(pool) })
            DataResult.Success(QuizCandidatePool(pool, DataOrigin.Mixed, CatalogCoverage.Partial))
        else DataResult.Failure(DataFailure.InsufficientLocalData)
    }
}
