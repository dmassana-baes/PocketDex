package com.example.pocketdex.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val speciesId: Int, val canonicalName: String, val nationalNumber: Int,
    val englishName: String?, val spanishName: String?, val types: String, val imageUrl: String?,
    val localImageFile: String?, val hp: Int?, val attack: Int?, val defense: Int?,
    val specialAttack: Int?, val specialDefense: Int?, val speed: Int?, val accessedAt: Long,
)

// No FK: marking with only an ID must succeed before details arrive.
@Entity(tableName = "favorites") data class FavoriteEntity(@PrimaryKey val pokemonId: Int, val addedAt: Long)
@Entity(tableName = "catalog_index") data class IndexEntity(@PrimaryKey val id: Int = 1, val payload: String)
@Entity(tableName = "media_retention", primaryKeys = ["owner", "pokemonId"])
data class MediaRetentionEntity(val owner: String, val pokemonId: Int, val path: String)

data class FavoriteRecord(
    @Embedded val mark: FavoriteEntity,
    @Relation(parentColumn = "pokemonId", entityColumn = "speciesId") val data: List<PokemonEntity>,
)

@Dao
interface PocketDexDao {
    @Query("SELECT * FROM pokemon ORDER BY nationalNumber ASC") suspend fun pokemon(): List<PokemonEntity>
    @Query("SELECT * FROM pokemon WHERE speciesId = :id") suspend fun pokemon(id: Int): PokemonEntity?
    @Query("SELECT * FROM pokemon WHERE speciesId = :id") fun observePokemon(id: Int): Flow<PokemonEntity?>
    @Upsert suspend fun upsert(pokemon: PokemonEntity)
    @Query("SELECT * FROM catalog_index WHERE id = 1") suspend fun index(): IndexEntity?
    @Upsert suspend fun upsert(index: IndexEntity)
    @Query("SELECT pokemonId FROM favorites") fun observeFavoriteIds(): Flow<List<Int>>
    @Transaction
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC, pokemonId ASC") fun observeFavorites(): Flow<List<FavoriteRecord>>
    @Query("SELECT * FROM favorites WHERE pokemonId = :id") suspend fun favorite(id: Int): FavoriteEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(mark: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE pokemonId = :id") suspend fun unmark(id: Int)
    @Query("SELECT path FROM media_retention UNION SELECT localImageFile FROM pokemon WHERE speciesId IN (SELECT pokemonId FROM favorites) AND localImageFile IS NOT NULL")
    suspend fun protectedPaths(): List<String>
    @Query("SELECT * FROM media_retention WHERE owner = :owner") suspend fun retained(owner: String): List<MediaRetentionEntity>
    @Query("SELECT DISTINCT owner FROM media_retention") suspend fun mediaOwners(): List<String>
    @Upsert suspend fun retain(entries: List<MediaRetentionEntity>)
    @Query("DELETE FROM media_retention WHERE owner = :owner") suspend fun release(owner: String)
    @Query("UPDATE pokemon SET localImageFile = NULL WHERE localImageFile = :path") suspend fun clearImage(path: String)
    @Query("DELETE FROM pokemon WHERE speciesId IN (SELECT speciesId FROM pokemon WHERE speciesId NOT IN (SELECT pokemonId FROM favorites) AND speciesId NOT IN (SELECT pokemonId FROM media_retention) ORDER BY accessedAt DESC, speciesId ASC LIMIT -1 OFFSET :limit)")
    suspend fun pruneUnprotected(limit: Int)
}

@Database(entities = [PokemonEntity::class, FavoriteEntity::class, IndexEntity::class, MediaRetentionEntity::class], version = 1, exportSchema = true)
abstract class PocketDexDatabase : RoomDatabase() {
    abstract fun dao(): PocketDexDao
}
