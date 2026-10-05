package com.example.pocketdex.core.domain.model

/** Cancellation is thrown, never represented by Failure. UI translates failure codes. */
sealed interface DataResult<out T> {
    data class Success<T>(val value: T) : DataResult<T>
    data class Failure(val reason: DataFailure) : DataResult<Nothing>
}

enum class DataFailure { Offline, Network, Storage, InvalidData, InsufficientLocalData }
enum class DataOrigin { Remote, Local, Mixed }
enum class CatalogCoverage { Complete, Partial }
