package com.artistwalls.app.data.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.artistwalls.app.data.model.Wallpaper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Shares the photograph plus its full attribution block, so the credit travels with
 * the image wherever it is posted.
 */
class ShareHelper(private val context: Context) {

    suspend fun shareWallpaper(wallpaper: Wallpaper, source: File): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val shareDir = File(context.cacheDir, "shared").apply { mkdirs() }
                val target = File(shareDir, source.name)
                if (!target.exists() || target.length() != source.length()) {
                    source.copyTo(target, overwrite = true)
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    target
                )

                val caption = buildString {
                    append(wallpaper.artistName.ifBlank { wallpaper.displayTitle })
                    append("\n\nPhoto: ").append(wallpaper.credit.author)
                    append("\nLicence: ").append(wallpaper.credit.license.name)
                    append("\nSource: ").append(wallpaper.credit.sourcePageUrl)
                    append("\n\nShared from ArtistWalls — photographs are credited to their " +
                        "original creators on Wikimedia Commons.")
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = wallpaper.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, caption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "Share wallpaper").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }

    fun shareCredit(wallpaper: Wallpaper): Result<Unit> = runCatching {
        val text = buildString {
            append(wallpaper.artistName.ifBlank { wallpaper.displayTitle })
            append("\nPhoto: ").append(wallpaper.credit.author)
            append("\nLicence: ").append(wallpaper.credit.license.name)
            append(" (").append(wallpaper.credit.license.url).append(")")
            append("\nSource: ").append(wallpaper.credit.sourcePageUrl)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share credit"))
    }
}
