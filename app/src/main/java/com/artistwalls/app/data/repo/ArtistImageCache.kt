package com.artistwalls.app.data.repo

import com.artistwalls.app.data.model.Artist
import java.util.concurrent.ConcurrentHashMap

/**
 * An artist's lead photograph, resolved lazily the first time a card becomes visible.
 * Kept in memory for the session so rails do not re-query Commons while scrolling.
 */
class ArtistImageCache(private val repository: WallpaperRepository) {

    private val hits = ConcurrentHashMap<String, String>()
    private val empty = ConcurrentHashMap.newKeySet<String>()

    fun cached(artist: Artist): String? = hits[artist.slug]

    suspend fun thumbnail(artist: Artist): String? {
        hits[artist.slug]?.let { return it }
        if (artist.slug in empty) return null

        val page = runCatching { repository.artistFeed(artist, offset = 0, limit = 8) }.getOrNull()
            ?: return null

        val url = page.items.firstOrNull { it.isPortrait }?.thumbUrl
            ?: page.items.firstOrNull()?.thumbUrl

        if (url != null) hits[artist.slug] = url else empty.add(artist.slug)
        return url
    }
}
