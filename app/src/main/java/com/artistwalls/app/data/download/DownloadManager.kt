package com.artistwalls.app.data.download

import android.content.Context
import com.artistwalls.app.data.local.LibraryDao
import com.artistwalls.app.data.model.Wallpaper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Real downloads of the original Commons file into app-private storage, so the
 * wallpaper keeps working with no network and can be applied by WallpaperManager.
 *
 * Files land in `getExternalFilesDir()/wallpapers`, which needs no runtime
 * permission on any supported API level.
 */
class DownloadManager(
    private val context: Context,
    private val client: OkHttpClient,
    private val dao: LibraryDao
) {

    private val _progress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val progress: StateFlow<Map<String, Int>> = _progress.asStateFlow()

    fun wallpaperDir(): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        return File(base, "wallpapers").apply { if (!exists()) mkdirs() }
    }

    /** Existing offline copy for a photograph, if any. */
    fun localFile(wallpaper: Wallpaper): File? {
        val candidate = File(wallpaperDir(), fileNameFor(wallpaper))
        return if (candidate.exists() && candidate.length() > 0) candidate else null
    }

    /**
     * Downloads (or reuses) the file and records it in the library table.
     * Returns the local file, or a failure carrying a human readable reason.
     */
    suspend fun download(wallpaper: Wallpaper): Result<File> = withContext(Dispatchers.IO) {
        localFile(wallpaper)?.let { existing ->
            dao.markDownloaded(wallpaper.id, existing.absolutePath, existing.length(), System.currentTimeMillis())
            return@withContext Result.success(existing)
        }

        val target = File(wallpaperDir(), fileNameFor(wallpaper))
        val temp = File(wallpaperDir(), fileNameFor(wallpaper) + ".part")

        try {
            val request = Request.Builder()
                .url(wallpaper.originalUrl)
                .header("Accept", "image/*")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("Commons returned HTTP ${response.code}")
                    )
                }
                val body = response.body
                    ?: return@withContext Result.failure(IllegalStateException("Empty response"))

                val total = body.contentLength()
                body.byteStream().use { input ->
                    temp.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var written = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            written += read
                            if (total > 0) {
                                publish(wallpaper.id, ((written * 100) / total).toInt().coerceIn(0, 100))
                            }
                        }
                    }
                }
            }

            if (!temp.exists() || temp.length() < 1024) {
                temp.delete()
                return@withContext Result.failure(IllegalStateException("Downloaded file was empty"))
            }

            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }

            dao.markDownloaded(wallpaper.id, target.absolutePath, target.length(), System.currentTimeMillis())
            publish(wallpaper.id, 100)
            Result.success(target)
        } catch (error: Exception) {
            temp.delete()
            publish(wallpaper.id, -1)
            Result.failure(error)
        }
    }

    /** Deletes the offline copy but keeps the favourite/credit row. */
    suspend fun deleteDownload(wallpaper: Wallpaper) = withContext(Dispatchers.IO) {
        localFile(wallpaper)?.delete()
        val entry = dao.find(wallpaper.id)
        if (entry != null) {
            if (entry.isFavorite) {
                dao.upsert(entry.copy(isDownloaded = false, localPath = null, bytes = 0))
            } else {
                dao.delete(entry.id)
            }
        }
        Unit
    }

    private fun publish(id: String, value: Int) {
        _progress.value = _progress.value.toMutableMap().apply { put(id, value) }
    }

    private fun fileNameFor(wallpaper: Wallpaper): String {
        val extension = when {
            wallpaper.mimeType.contains("png") -> "png"
            wallpaper.mimeType.contains("webp") -> "webp"
            else -> "jpg"
        }
        val safe = wallpaper.id.removePrefix("File:")
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .takeLast(80)
        return "${safe.ifBlank { "wallpaper" }}.$extension"
    }
}
