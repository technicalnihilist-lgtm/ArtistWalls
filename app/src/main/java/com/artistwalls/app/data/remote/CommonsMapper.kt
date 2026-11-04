package com.artistwalls.app.data.remote

import com.artistwalls.app.data.model.Credit
import com.artistwalls.app.data.model.ImageQuality
import com.artistwalls.app.data.model.LicenseInfo
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.util.HtmlText
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Turns a raw Commons page into a [Wallpaper] — and is the place where licensing
 * is enforced. A page that fails any check is dropped, never shown.
 */
object CommonsMapper {

    private val ACCEPTED_MIME = setOf("image/jpeg", "image/png", "image/webp")

    /** Minimum original width for a photograph to be offered as a device wallpaper. */
    const val MIN_WALLPAPER_WIDTH = 1000

    private const val UNKNOWN_AUTHOR = "Wikimedia Commons contributor"

    fun toWallpaper(page: CommonsPage, artistName: String): Wallpaper? {
        val info = page.imageinfo?.firstOrNull() ?: return null

        val mime = info.mime?.lowercase().orEmpty()
        if (mime.isNotEmpty() && mime !in ACCEPTED_MIME) return null
        if (info.mediatype != null && info.mediatype != "BITMAP") return null

        val em = info.extmetadata.orEmpty()
        // Never assume a type: strings, numbers and booleans all turn up in extmetadata.
        fun meta(key: String): String? =
            (em[key]?.value as? JsonPrimitive)?.contentOrNull

        val licenseName = HtmlText.clean(meta("LicenseShortName"))
        val licenseUrl = meta("LicenseUrl").orEmpty()
        if (!LicensePolicy.isAccepted(licenseName, licenseUrl)) return null

        val original = info.url ?: return null
        val sourcePage = info.descriptionurl ?: info.descriptionshorturl ?: return null
        val preview = info.thumburl ?: original

        val width = info.width ?: 0
        val height = info.height ?: 0
        if (width <= 0 || height <= 0) return null

        val author = HtmlText.clean(meta("Artist"))
            .ifBlank { HtmlText.clean(meta("Credit")) }
            .ifBlank { HtmlText.clean(meta("Attribution")) }
            .ifBlank { UNKNOWN_AUTHOR }

        val shareAlike = LicensePolicy.isShareAlike(licenseName)
        val license = LicenseInfo(
            name = licenseName,
            url = licenseUrl.ifBlank { sourcePage },
            usageTerms = HtmlText.clean(meta("UsageTerms")),
            attributionRequired = LicensePolicy.requiresAttribution(licenseName),
            shareAlike = shareAlike,
            modificationNote = LicensePolicy.modificationNote(shareAlike)
        )

        val title = page.title.replace('_', ' ')
        val creditLine = buildString {
            append(author)
            append(" · ")
            append(licenseName)
            append(" · via Wikimedia Commons")
        }

        return Wallpaper(
            id = title,
            title = title,
            displayTitle = title.removePrefix("File:").substringBeforeLast('.').replace('_', ' '),
            artistName = artistName,
            originalUrl = original,
            thumbUrl = preview,
            previewUrl = preview,
            width = width,
            height = height,
            mimeType = mime.ifBlank { "image/jpeg" },
            description = HtmlText.truncate(meta("ImageDescription"), 600),
            dateOriginal = HtmlText.truncate(meta("DateTimeOriginal"), 80),
            credit = Credit(
                author = author,
                creditLine = creditLine,
                sourcePageUrl = sourcePage,
                license = license
            ),
            quality = if (width >= MIN_WALLPAPER_WIDTH) ImageQuality.WALLPAPER else ImageQuality.PREVIEW_ONLY
        )
    }

    /**
     * Keeps a feed on-topic. When Commons returns a group photo the artist is still
     * mentioned, so the match is checked against title *and* description.
     */
    fun isRelevant(wallpaper: Wallpaper, matchTokens: List<String>): Boolean {
        if (matchTokens.isEmpty()) return true
        val haystack = (wallpaper.title + " " + wallpaper.description).lowercase()
        return matchTokens.any { haystack.contains(it) }
    }

    /** Portrait photographs first, then highest resolution. */
    val galleryOrder: Comparator<Wallpaper> =
        compareByDescending<Wallpaper> { it.isPortrait }
            .thenByDescending { it.width.toLong() * it.height.toLong() }
}
