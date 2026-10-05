package com.example.pocketdex.core.data.image

import android.content.Context
import android.graphics.BitmapFactory
import com.example.pocketdex.core.data.catalog.readNBytesCompat
import com.example.pocketdex.core.data.local.PocketDexDatabase
import com.example.pocketdex.core.domain.util.DispatcherProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.coroutines.coroutineContext

/** Private verified copies. Favorite/session protection is read from Room, independently of Coil. */
@Singleton
class PrivateImageStore @Inject constructor(
    @ApplicationContext context: Context, private val http: OkHttpClient,
    private val database: PocketDexDatabase, private val dispatchers: DispatcherProvider,
) {
    private val directory = File(context.filesDir, "pokemon-media")
    private val downloads = Semaphore(4)
    internal val references = Mutex()
    private val pending = java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicInteger>()

    internal fun holdPublication(path: String) {
        pending.compute(path) { _, count -> (count ?: java.util.concurrent.atomic.AtomicInteger()).apply { incrementAndGet() } }
    }
    internal fun finishPublication(path: String) {
        pending.computeIfPresent(path) { _, count -> if (count.decrementAndGet() == 0) null else count }
    }

    suspend fun verified(path: String): Boolean = withContext(dispatchers.io) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth !in 1..4096 || bounds.outHeight !in 1..4096) return@withContext false
        val options = BitmapFactory.Options().apply {
            inSampleSize = 1
            while (bounds.outWidth / inSampleSize > 1024 || bounds.outHeight / inSampleSize > 1024) inSampleSize *= 2
        }
        val bitmap = BitmapFactory.decodeFile(path, options) ?: return@withContext false
        bitmap.recycle()
        true
    }

    suspend fun download(id: Int, url: String): String {
        var publishedPath: String? = null
        var delivered = false
        try {
            val result = downloads.withPermit {
                withContext(dispatchers.io) {
                    require(url.startsWith("https://"))
                    if (!directory.exists() && !directory.mkdirs()) throw MediaStorageException("Cannot create private media directory")
                    val temporary = File(directory, "$id-${UUID.randomUUID()}.part")
                    try {
                        val bytes = bytes(url)
                        coroutineContext.ensureActive()
                        try { temporary.writeBytes(bytes) }
                        catch (error: IOException) { throw MediaStorageException("Cannot write private image", error) }
                        require(verified(temporary.path)) { "Invalid image" }
                        coroutineContext.ensureActive()
                        val published = File(directory, temporary.name.removeSuffix(".part") + ".image")
                        // Pin before rename so pruning cannot see an unprotected final file.
                        holdPublication(published.absolutePath)
                        if (!temporary.renameTo(published)) {
                            finishPublication(published.absolutePath)
                            throw MediaStorageException("Cannot publish image")
                        }
                        publishedPath = published.absolutePath
                        published.absolutePath
                    } finally { temporary.delete() }
                }
            }
            delivered = true
            return result
        } finally { if (!delivered) publishedPath?.let(::finishPublication) }
    }

    private suspend fun bytes(url: String): ByteArray = suspendCancellableCoroutine { continuation ->
        val call = http.newCall(Request.Builder().url(url).build())
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, error: IOException) {
                if (continuation.isActive) continuation.resumeWithException(error)
            }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val result = response.use {
                        if (!it.isSuccessful) throw IOException("Image request failed")
                        requireNotNull(it.body).byteStream().readNBytesCompat(8 * 1024 * 1024)
                    }
                    if (continuation.isActive) continuation.resume(result)
                } catch (error: Exception) {
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
            }
        })
    }

    suspend fun prune() = references.withLock { pruneUnprotected() }

    internal suspend fun pruneUnprotected() = withContext(dispatchers.io) {
        val dao = database.dao()
        val protected = dao.protectedPaths().toSet() + pending.keys
        val files = directory.listFiles().orEmpty().filter { it.extension == "image" && it.absolutePath !in protected }
            .sortedByDescending { it.lastModified() }
        var kept = 0L
        for (file in files) {
            coroutineContext.ensureActive()
            if (pending.containsKey(file.absolutePath)) continue
            kept += file.length()
            if (kept > 64L * 1024 * 1024) {
                dao.clearImage(file.absolutePath)
                if (!file.delete() && file.exists()) throw MediaStorageException("Cannot evict private image")
            }
        }
    }
}

internal class MediaStorageException(message: String, cause: Throwable? = null) : IOException(message, cause)
