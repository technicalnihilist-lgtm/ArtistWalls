package com.artistwalls.app.data.model

enum class ArtistGroup(val label: String) {
    INDIAN("Indian Singers"),
    INTERNATIONAL("International Artists")
}

/**
 * A curated artist entry. The catalogue only holds *who* to look for — every
 * photograph is discovered live from Wikimedia Commons at runtime, so new
 * uploads appear in the app without a release.
 *
 * [queryCandidates] are tried in order until one returns licensed photographs,
 * which keeps the app resilient when Commons categories are renamed.
 */
data class Artist(
    val slug: String,
    val name: String,
    val group: ArtistGroup,
    val queryCandidates: List<String>,
    val wikipediaUrl: String,
    val tagline: String = "",
    /** Lower-case tokens used to keep search hits on-topic for this artist. */
    val matchTokens: List<String> = emptyList()
) {
    val initial: String get() = name.trim().take(1).uppercase()
}

/**
 * One of the twelve "other" collections. Also resolved live.
 */
data class WallpaperCategory(
    val id: String,
    val name: String,
    val blurb: String,
    val queryCandidates: List<String>
)
