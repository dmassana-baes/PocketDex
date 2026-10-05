package com.example.pocketdex.core.data.di

import com.example.pocketdex.core.domain.util.DispatcherProvider
import com.example.pocketdex.core.domain.util.RandomSource
import com.example.pocketdex.core.domain.quiz.QuizSessionIdSource
import java.util.UUID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object ExecutionModule {
    @Provides
    @Singleton
    fun dispatchers(): DispatcherProvider = object : DispatcherProvider {
        override val io = Dispatchers.IO
        override val default = Dispatchers.Default
        override val main = Dispatchers.Main.immediate
    }

    @Provides
    @Singleton
    fun randomSource(): RandomSource = RandomSource { bound -> Random.nextInt(bound) }
    @Provides fun quizSessionIds(): QuizSessionIdSource = QuizSessionIdSource { UUID.randomUUID().toString() }
}
