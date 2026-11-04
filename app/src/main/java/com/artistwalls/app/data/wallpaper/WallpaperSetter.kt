package com.artistwalls.app.data.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Applies a downloaded photograph through the platform [WallpaperManager].
 * The bitmap is decoded with a sample size so very large originals cannot blow up
 * the heap, and never cropped by us — Android's own cropping UI is used when the
 * device shows one.
 */
class WallpaperSetter(private val context: Context) {

    enum class Target { HOME, LOCK, BOTH }

    suspend fun setWallpaper(file: File, target: Target): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val manager = WallpaperManager.getInstance(context)
            if (!manager.isWallpaperSupported) {
                return@withContext Result.failure(UnsupportedOperationException("This device does not allow wallpaper changes"))
            }

            val bitmap = decodeSampled(file)
                ?: return@withContext Result.failure(IllegalStateException("Could not decode the image"))

            val flags = when (target) {
                Target.HOME -> WallpaperManager.FLAG_SYSTEM
                Target.LOCK -> WallpaperManager.FLAG_LOCK
                Target.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            manager.setBitmap(bitmap, null, true, flags)
            bitmap.recycle()
            Result.success(Unit)
        } catch (security: SecurityException) {
            Result.failure(security)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun decodeSampled(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        val maxDimension = 4096
        while (bounds.outWidth / sample > maxDimension || bounds.outHeight / sample > maxDimension) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.absolutePath, options)
    }
}
