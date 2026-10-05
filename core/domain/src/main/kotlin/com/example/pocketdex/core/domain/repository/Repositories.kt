package com.example.pocketdex.core.domain.repository

import com.example.pocketdex.core.domain.model.CatalogPage
import com.example.pocketdex.core.domain.model.CatalogQuery
import com.example.pocketdex.core.domain.model.CachedCatalog
import com.example.pocketdex.core.domain.model.DataResult
import com.example.pocketdex.core.domain.model.FavoritePokemon
import com.example.pocketdex.core.domain.model.KnownPokemon
import com.example.pocketdex.core.domain.model.PageRequest
import com.example.pocketdex.core.domain.model.PokemonDetail
import com.example.pocketdex.core.domain.model.PokemonId
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    suspend fun cached(query: CatalogQuery): DataResult<CachedCatalog>
    /** Returns a valid empty page for no matches. Does not expose Paging or preload on app startup. */
    suspend fun page(query: CatalogQuery, request: PageRequest): DataResult<CatalogPage>
}

interface PokemonRepository {
    /** Local absence does not establish absence in the national catalog. */
    suspend fun cachedDetail(id: PokemonId): DataResult<PokemonDetail?>
    fun observeDetail(id: PokemonId): Flow<DataResult<PokemonDetail?>>
    suspend fun refresh(id: PokemonId): DataResult<PokemonDetail?>
    /** Null is a verified absence, not a network/storage failure. Locally available partial data is valid. */
    suspend fun detail(id: PokemonId): DataResult<PokemonDetail?>
}

interface FavoritesRepository {
    /** Completes missing content/media without redownloading complete local favorites. */
    suspend fun complete(id: PokemonId): DataResult<Unit>
    fun observeIds(): Flow<DataResult<Set<PokemonId>>>
    /** Most recently added first; observe failures without manufacturing an empty list. */
    fun observeFavorites(): Flow<DataResult<List<FavoritePokemon>>>
    /** Idempotent desired state. Persist mark/known data before attempting missing downloads.
     * Unmark preserves cached data/media, releasing only favorite retention.
     * Success confirms persistence, not completion of missing data or images.
     */
    suspend fun setFavorite(pokemon: KnownPokemon, favorite: Boolean): DataResult<Unit>
    /** Called while app is open; refresh failure leaves existing content and mark intact. */
    suspend fun refresh(id: PokemonId): DataResult<Unit>
}
