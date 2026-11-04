package com.artistwalls.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.artistwalls.app.data.model.Credit
import com.artistwalls.app.data.model.ImageQuality
import com.artistwalls.app.data.model.LicenseInfo
import com.artistwalls.app.data.model.Wallpaper

/**
 * One row per photograph the user has touched: favourite, downloaded, or simply
 * viewed. Credits are stored *with* the row so attribution and offline access keep
 * working with no network at all.
 */
@Entity(tableName = "library")
data class LibraryEntry(
    @PrimaryKey val id: String,
    val title: String,
    val artistName: String,
    val thumbUrl: String,
    val originalUrl: String,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val description: String,
    val dateOriginal: String,
    val author: String,
    val creditLine: String,
    val sourcePageUrl: String,
    val licenseName: String,
    val licenseUrl: String,
    val usageTerms: String,
    val shareAlike: Boolean,
    val attributionRequired: Boolean,
    val modificationNote: String,
    val quality: String,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val bytes: Long = 0L,
    val savedAt: Long = 0L,
    val lastViewedAt: Long = 0L
)

fun Wallpaper.toLibraryEntry(): LibraryEntry = LibraryEntry(
    id = id,
    title = title,
    artistName = artistName,
    thumbUrl = previewUrl.ifBlank { thumbUrl },
    originalUrl = originalUrl,
    width = width,
    height = height,
    mimeType = mimeType,
    description = description,
    dateOriginal = dateOriginal,
    author = credit.author,
    creditLine = credit.creditLine,
    sourcePageUrl = credit.sourcePageUrl,
    licenseName = credit.license.name,
    licenseUrl = credit.license.url,
    usageTerms = credit.license.usageTerms,
    shareAlike = credit.license.shareAlike,
    attributionRequired = credit.license.attributionRequired,
    modificationNote = credit.license.modificationNote,
    quality = quality.name
)

fun LibraryEntry.toWallpaper(): Wallpaper = Wallpaper(
    id = id,
    title = title,
    displayTitle = title.removePrefix("File:").substringBeforeLast('.'),
    artistName = artistName,
    originalUrl = originalUrl,
    thumbUrl = thumbUrl,
    previewUrl = thumbUrl,
    width = width,
    height = height,
    mimeType = mimeType,
    description = description,
    dateOriginal = dateOriginal,
    credit = Credit(
        author = author,
        creditLine = creditLine,
        sourcePageUrl = sourcePageUrl,
        license = LicenseInfo(
            name = licenseName,
            url = licenseUrl,
            usageTerms = usageTerms,
            attributionRequired = attributionRequired,
            shareAlike = shareAlike,
            modificationNote = modificationNote
        )
    ),
    quality = runCatching { ImageQuality.valueOf(quality) }.getOrDefault(ImageQuality.WALLPAPER)
)
