package com.example.pocketdex.core.data.catalog

import android.database.sqlite.SQLiteException
import com.example.pocketdex.core.domain.model.DataFailure
import com.example.pocketdex.core.domain.model.DataResult
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import com.example.pocketdex.core.data.image.MediaStorageException

internal suspend fun <T> dataResult(block: suspend () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: UnknownHostException) {
    DataResult.Failure(DataFailure.Offline)
} catch (_: MediaStorageException) {
    DataResult.Failure(DataFailure.Storage)
} catch (_: IOException) {
    DataResult.Failure(DataFailure.Network)
} catch (_: HttpException) {
    DataResult.Failure(DataFailure.Network)
} catch (_: SQLiteException) {
    DataResult.Failure(DataFailure.Storage)
} catch (_: SerializationException) {
    DataResult.Failure(DataFailure.InvalidData)
} catch (_: IllegalArgumentException) {
    DataResult.Failure(DataFailure.InvalidData)
} catch (_: NoSuchElementException) {
    DataResult.Failure(DataFailure.InvalidData)
} catch (_: IndexOutOfBoundsException) {
    DataResult.Failure(DataFailure.InvalidData)
}
