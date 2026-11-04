package com.artistwalls.app.data.repo

import com.artistwalls.app.data.catalog.ArtistCatalog
import com.artistwalls.app.data.model.Artist
import com.artistwalls.app.data.model.ImageQuality
import com.artistwalls.app.data.model.PagedWallpapers
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.data.model.WallpaperCategory
import com.artistwalls.app.data.remote.CommonsMapper
import com.artistwalls.app.data.remote.WikimediaApi
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Thrown when a live Commons call fails after every retry. */
class CommonsUnavailableException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * All image discovery happens here, live, against the Wikimedia Commons API.
 *
 * Three behaviours matter for a "live" gallery:
 *  - a feed remembers which search candidate worked, so pagination stays consistent;
 *  - pages are cached in memory for a short TTL, so scrolling back is instant while
 *    brand new uploads still appear within minutes;
 *  - duplicates are removed by Commons file title, the stable unique key.
 */
class WallpaperRepository(private val api: WikimediaApi) {

    private data class CacheEntry(val at: Long, val page: PagedWallpapers)

    private val resolvedQueries = ConcurrentHashMap<String, String>()
    private val pageCache = ConcurrentHashMap<String, CacheEntry>()
    private val dailyCache = ConcurrentHashMap<Int, Wallpaper>()

    // ------------------------------------------------------------------ Feeds

    suspend fun artistFeed(artist: Artist, offset: Int = 0, limit: Int = 24, refresh: Boolean = false) =
        feed(
            feedId = "artist:${artist.slug}",
            candidates = artist.queryCandidates,
            artistName = artist.name,
            matchTokens = artist.matchTokens,
            offset = offset,
            limit = limit,
            refresh = refresh
        )

    suspend fun categoryFeed(
        category: WallpaperCategory,
        offset: Int = 0,
        limit: Int = 24,
        refresh: Boolean = false
    ) = feed(
        feedId = "category:${category.id}",
        candidates = category.queryCandidates,
        artistName = "",
        matchTokens = emptyList(),
        offset = offset,
        limit = limit,
        refresh = refresh
    )

    suspend fun searchWallpapers(query: String, offset: Int = 0, limit: Int = 24) = feed(
        feedId = "search:${query.lowercase()}",
        candidates = listOf(
            "incategory:\"$query\" filetype:bitmap",
            "$query filetype:bitmap"
        ),
        artistName = "",
        matchTokens = emptyList(),
        offset = offset,
        limit = limit
    )

    /** Most-linked music photographs on Commons — a stable, genuinely popular feed. */
    suspend fun trending(offset: Int = 0, limit: Int = 24) = feed(
        feedId = "trending",
        candidates = listOf(
            "singer concert portrait filetype:bitmap",
            "incategory:\"Live performances\" filetype:bitmap",
            "musician performing filetype:bitmap"
        ),
        artistName = "",
        matchTokens = emptyList(),
        offset = offset,
        limit = limit,
        sort = "incoming_links_desc"
    )

    /** Newest music uploads first — this is what keeps the app feeling alive. */
    suspend fun recentlyAdded(offset: Int = 0, limit: Int = 24, refresh: Boolean = false) = feed(
        feedId = "recent",
        candidates = listOf(
            "incategory:\"Musicians\" filetype:bitmap",
            "musician portrait filetype:bitmap",
            "singer portrait filetype:bitmap"
        ),
        artistName = "",
        matchTokens = emptyList(),
        offset = offset,
        limit = limit,
        sort = "create_timestamp_desc",
        refresh = refresh
    )

    // ------------------------------------------------------------ Single file

    /** Used for deep links, favourites and offline entries that need fresh metadata. */
    suspend fun wallpaperByTitle(title: String): Wallpaper? {
        val normalised = if (title.startsWith("File:")) title else "File:$title"
        val page = withRetry {
            api.query(
                baseParams() + mapOf(
                    "action" to "query",
                    "titles" to normalised,
                    "prop" to "imageinfo",
                    "iiprop" to "url|size|mime|extmetadata",
                    "iiurlwidth" to PREVIEW_WIDTH
                )
            )
        }
        val raw = page.query?.pages?.firstOrNull() ?: return null
        val wallpaper = CommonsMapper.toWallpaper(raw, "") ?: return null
        return wallpaper.copy(artistName = artistNameFor(wallpaper.title))
    }

    // --------------------------------------------------------- Daily wallpaper

    /**
     * Deterministic for a given calendar day, and different every day: the artist is
     * picked from the curated catalogue, the photograph from their live feed.
     */
    suspend fun dailyWallpaper(): Wallpaper? {
        val calendar = Calendar.getInstance()
        val dayKey = calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
        dailyCache[dayKey]?.let { return it }

        val artists = ArtistCatalog.allArtists
        if (artists.isEmpty()) return null

        // Try the day's artist first, then walk forward so a thin feed never
        // leaves the home hero empty.
        for (step in 0 until 5) {
            val artist = artists[(dayKey + step) % artists.size]
            val page = runCatching { artistFeed(artist, offset = 0, limit = 24) }.getOrNull()
            val pick = page?.items?.firstOrNull { it.quality == ImageQuality.WALLPAPER }
                ?: page?.items?.firstOrNull()
            if (pick != null) {
                dailyCache[dayKey] = pick
                return pick
            }
        }
        val fallback = runCatching { trending(limit = 12) }.getOrNull()?.items?.firstOrNull()
        if (fallback != null) dailyCache[dayKey] = fallback
        return fallback
    }

    // -------------------------------------------------------------- Internals

    private suspend fun feed(
        feedId: String,
        candidates: List<String>,
        artistName: String,
        matchTokens: List<String>,
        offset: Int,
        limit: Int,
        sort: String? = null,
        refresh: Boolean = false
    ): PagedWallpapers {
        val cacheKey = "$feedId|$offset|$sort"

        if (!refresh && offset == 0) {
            pageCache[cacheKey]?.let { entry ->
                if (System.currentTimeMillis() - entry.at < CACHE_TTL_MS) return entry.page
            }
        }

        val page: PagedWallpapers = if (offset == 0) {
            var result: PagedWallpapers? = null
            for (candidate in candidates) {
                val attempt = request(candidate, 0, limit, artistName, matchTokens, sort)
                if (attempt.items.isNotEmpty()) {
                    resolvedQueries[feedId] = candidate
                    result = attempt
                    break
                }
            }
            result ?: PagedWallpapers.Empty
        } else {
            val query = resolvedQueries[feedId] ?: candidates.first()
            request(query, offset, limit, artistName, matchTokens, sort)
        }

        if (offset == 0) {
            pageCache[cacheKey] = CacheEntry(System.currentTimeMillis(), page)
        }
        return page
    }

    private suspend fun request(
        query: String,
        offset: Int,
        limit: Int,
        artistName: String,
        matchTokens: List<String>,
        sort: String?
    ): PagedWallpapers {
        val params = baseParams() + buildMap {
            put("action", "query")
            put("generator", "search")
            put("gsrsearch", query)
            put("gsrnamespace", "6")
            put("gsrlimit", limit.toString())
            put("gsroffset", offset.toString())
            put("prop", "imageinfo")
            put("iiprop", "url|size|mime|extmetadata")
            put("iiurlwidth", PREVIEW_WIDTH)
            if (sort != null) put("gsrsort", sort)
        }

        val response = withRetry { api.query(params) }
        val pages = response.query?.pages.orEmpty()
        val mapped = pages.mapNotNull { CommonsMapper.toWallpaper(it, artistName) }
            .distinctBy { it.id }

        val relevant = mapped.filter { CommonsMapper.isRelevant(it, matchTokens) }
        // Keep the relevance filter only when it still leaves a usable gallery.
        val kept = if (relevant.size >= 3) relevant else mapped

        val next = response.continueInfo?.gsroffset
        return PagedWallpapers(
            items = kept.sortedWith(CommonsMapper.galleryOrder),
            nextOffset = next ?: (offset + limit),
            hasMore = next != null && next > offset,
            rawCount = pages.size,
            rejectedCount = pages.size - mapped.size
        )
    }

    private fun baseParams(): Map<String, String> = mapOf(
        "format" to "json",
        "formatversion" to "2",
        "maxlag" to "5"
    )

    private fun artistNameFor(title: String): String {
        val haystack = title.lowercase()
        return ArtistCatalog.allArtists.firstOrNull { artist ->
            artist.matchTokens.any { haystack.contains(it) }
        }?.name.orEmpty()
    }

    private suspend fun <T> withRetry(attempts: Int = 3, block: suspend () -> T): T {
        var last: Throwable? = null
        for (attempt in 0 until attempts) {
            try {
                return block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                last = error
                if (attempt < attempts - 1) delay(600L * (attempt + 1))
            }
        }
        throw CommonsUnavailableException(
            "Wikimedia Commons is not reachable right now. Check your connection and try again.",
            last
        )
    }

    companion object {
        private const val PREVIEW_WIDTH = "1200"
        private const val CACHE_TTL_MS = 5 * 60 * 1000L
    }
}
