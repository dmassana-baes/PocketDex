package com.example.pocketdex.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.pocketdex.core.data.catalog.CatalogRepositoryImpl
import com.example.pocketdex.core.data.catalog.EpochClock
import com.example.pocketdex.core.data.local.PocketDexDao
import com.example.pocketdex.core.data.local.PocketDexDatabase
import com.example.pocketdex.core.data.remote.IndexApi
import com.example.pocketdex.core.data.remote.PokeApi
import com.example.pocketdex.core.data.repository.FavoritesRepositoryImpl
import com.example.pocketdex.core.data.repository.QuizMediaRepositoryImpl
import com.example.pocketdex.core.domain.repository.QuizMediaRepository
import com.example.pocketdex.core.domain.repository.QuizCandidateRepository
import com.example.pocketdex.core.data.repository.QuizCandidateRepositoryImpl
import com.example.pocketdex.core.domain.repository.CatalogRepository
import com.example.pocketdex.core.domain.repository.FavoritesRepository
import com.example.pocketdex.core.domain.repository.PokemonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton fun json(): Json = Json { ignoreUnknownKeys = true }
    @Provides @Singleton fun http(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).callTimeout(60, TimeUnit.SECONDS).build()
    @Provides @Singleton fun pokeApi(http: OkHttpClient, json: Json): PokeApi = Retrofit.Builder()
        .baseUrl("https://pokeapi.co/api/v2/").client(http)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build().create(PokeApi::class.java)
    @Provides @Singleton fun indexApi(http: OkHttpClient): IndexApi = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/").client(http).build().create(IndexApi::class.java)
    @Provides @Singleton fun database(@ApplicationContext context: Context): PocketDexDatabase =
        Room.databaseBuilder(context, PocketDexDatabase::class.java, "pocketdex.db").build()
    @Provides fun dao(database: PocketDexDatabase): PocketDexDao = database.dao()
    @Provides fun clock(): EpochClock = EpochClock(System::currentTimeMillis)
    @Provides fun catalog(repository: CatalogRepositoryImpl): CatalogRepository = repository
    @Provides fun pokemon(repository: CatalogRepositoryImpl): PokemonRepository = repository
    @Provides fun favorites(repository: FavoritesRepositoryImpl): FavoritesRepository = repository
    @Provides fun quizMedia(repository: QuizMediaRepositoryImpl): QuizMediaRepository = repository
    @Provides fun quizCandidates(repository: QuizCandidateRepositoryImpl): QuizCandidateRepository = repository
}
