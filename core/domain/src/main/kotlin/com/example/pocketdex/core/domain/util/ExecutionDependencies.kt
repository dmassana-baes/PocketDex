package com.example.pocketdex.core.domain.util

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

fun interface RandomSource {
    /** Returns a number in 0 until bound. Called only when creating a new quiz session. */
    fun nextInt(bound: Int): Int
}
