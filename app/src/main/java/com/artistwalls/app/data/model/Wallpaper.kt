package com.artistwalls.app.data.model

/** Where a photograph is good enough to be used as a full device wallpaper. */
enum class ImageQuality {
    /** Native resolution is large enough to fill a modern phone screen. */
    WALLPAPER,

    /** Licensed and credited, but the original file is small — shown with a badge. */
    PREVIEW_ONLY
}

/** Everything the licence asks us to display, kept as one immutable value. */
data class LicenseInfo(
    val name: String,
    val url: String,
    val usageTerms: String,
    val attributionRequired: Boolean,
    val shareAlike: Boolean,
    val modificationNote: String
)

/**
 * Attribution block for a single photograph. Never imply ArtistWalls owns the image:
 * [author] and [sourcePageUrl] always point back to the Commons file page.
 */
data class Credit(
    val author: String,
    val creditLine: String,
    val sourcePageUrl: String,
    val license: LicenseInfo
)

/**
 * A photograph discovered live from Wikimedia Commons.
 *
 * [id] is the Commons file title (for example `File:Arijit Singh.jpg`) — it is the
 * stable, unique key used for duplicate prevention, favourites and offline copies.
 */
data class Wallpaper(
    val id: String,
    val title: String,
    val displayTitle: String,
    val artistName: String,
    val originalUrl: String,
    val thumbUrl: String,
    val previewUrl: String,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val description: String,
    val dateOriginal: String,
    val credit: Credit,
    val quality: ImageQuality
) {
    val resolution: String
        get() = if (width > 0 && height > 0) "$width × $height" else "Unknown"

    val isPortrait: Boolean get() = height >= width

    val megapixels: String
        get() {
            if (width <= 0 || height <= 0) return "—"
            val mp = (width.toLong() * height.toLong()) / 1_000_000.0
            return String.format("%.1f MP", mp)
        }
}

/** One page of live search results. */
data class PagedWallpapers(
    val items: List<Wallpaper>,
    val nextOffset: Int,
    val hasMore: Boolean,
    val rawCount: Int = items.size,
    val rejectedCount: Int = 0
) {
    companion object {
        val Empty = PagedWallpapers(emptyList(), 0, false)
    }
}
