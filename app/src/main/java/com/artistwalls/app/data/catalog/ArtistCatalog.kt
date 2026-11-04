package com.artistwalls.app.data.catalog

import com.artistwalls.app.data.model.Artist
import com.artistwalls.app.data.model.ArtistGroup
import com.artistwalls.app.data.model.WallpaperCategory

/**
 * The curated spine of the app.
 *
 * Only names, categories and Wikipedia links live here. Every photograph is fetched
 * live from Wikimedia Commons at runtime — nothing is bundled and nothing is faked,
 * so newly uploaded (and newly licensed) photographs show up on their own.
 *
 * Each entry carries a list of Commons search candidates, tried in order, so a
 * renamed category or a differently spelled file name still resolves.
 */
object ArtistCatalog {

    /**
     * Plain text search first: on Commons it is far more reliable than `incategory:`
     * (some artist categories are not indexed for search), and the relevance filter
     * in the repository keeps the results on-topic. `incategory:` is kept as a
     * secondary candidate for artists whose category is well maintained.
     */
    private fun candidates(name: String, extra: List<String> = emptyList()): List<String> =
        listOf("$name filetype:bitmap") +
            extra +
            listOf("incategory:\"$name\" filetype:bitmap", "$name singer filetype:bitmap")

    private fun tokens(name: String): List<String> {
        val clean = name.lowercase()
            .replace("(", " ").replace(")", " ")
            .replace("é", "e").replace("ó", "o").replace("í", "i")
        return clean.split(" ", ",", ".", "-", "'")
            .filter { it.length > 2 && it !in STOP_WORDS }
            .distinct()
    }

    private val STOP_WORDS = setOf("the", "and", "singer", "artists", "artist")

    private fun artist(
        slug: String,
        name: String,
        group: ArtistGroup,
        wiki: String,
        tagline: String,
        extra: List<String> = emptyList(),
        matchTokens: List<String> = emptyList()
    ) = Artist(
        slug = slug,
        name = name,
        group = group,
        queryCandidates = candidates(name, extra),
        wikipediaUrl = "https://en.wikipedia.org/wiki/$wiki",
        tagline = tagline,
        matchTokens = (matchTokens + tokens(name)).distinct()
    )

    // ------------------------------------------------------------------ Indian

    val indianSingers: List<Artist> = listOf(
        artist("arijit-singh", "Arijit Singh", ArtistGroup.INDIAN, "Arijit_Singh",
            "Playback voice of a generation"),
        artist("shreya-ghoshal", "Shreya Ghoshal", ArtistGroup.INDIAN, "Shreya_Ghoshal",
            "National Award winning playback singer"),
        artist("sonu-nigam", "Sonu Nigam", ArtistGroup.INDIAN, "Sonu_Nigam",
            "The golden voice of the 2000s"),
        artist("armaan-malik", "Armaan Malik", ArtistGroup.INDIAN, "Armaan_Malik",
            "Contemporary pop and playback"),
        artist("jubin-nautiyal", "Jubin Nautiyal", ArtistGroup.INDIAN, "Jubin_Nautiyal",
            "Indie-pop crossover voice"),
        artist("atif-aslam", "Atif Aslam", ArtistGroup.INDIAN, "Atif_Aslam",
            "Subcontinental rock and playback"),
        artist("kk", "KK", ArtistGroup.INDIAN, "KK_(singer)",
            "Krishnakumar Kunnath · evergreen playback",
            extra = listOf("Krishnakumar Kunnath filetype:bitmap", "KK singer playback filetype:bitmap"),
            matchTokens = listOf("kk", "krishnakumar", "kunnath")),
        artist("mohit-chauhan", "Mohit Chauhan", ArtistGroup.INDIAN, "Mohit_Chauhan",
            "Silk Route frontman turned playback legend"),
        artist("vishal-mishra", "Vishal Mishra", ArtistGroup.INDIAN, "Vishal_Mishra_(composer)",
            "Composer and singer"),
        artist("neha-kakkar", "Neha Kakkar", ArtistGroup.INDIAN, "Neha_Kakkar",
            "Pop and playback powerhouse"),
        artist("sunidhi-chauhan", "Sunidhi Chauhan", ArtistGroup.INDIAN, "Sunidhi_Chauhan",
            "High-energy playback icon"),
        artist("kumar-sanu", "Kumar Sanu", ArtistGroup.INDIAN, "Kumar_Sanu",
            "The melody king of the 90s"),
        artist("udit-narayan", "Udit Narayan", ArtistGroup.INDIAN, "Udit_Narayan",
            "Voice of a million film songs"),
        artist("alka-yagnik", "Alka Yagnik", ArtistGroup.INDIAN, "Alka_Yagnik",
            "Record-breaking playback artist"),
        artist("asha-bhosle", "Asha Bhosle", ArtistGroup.INDIAN, "Asha_Bhosle",
            "Dadasaheb Phalke awardee"),
        artist("lata-mangeshkar", "Lata Mangeshkar", ArtistGroup.INDIAN, "Lata_Mangeshkar",
            "The Nightingale of India")
    )

    // ----------------------------------------------------------- International

    val internationalArtists: List<Artist> = listOf(
        artist("michael-jackson", "Michael Jackson", ArtistGroup.INTERNATIONAL, "Michael_Jackson",
            "King of Pop"),
        artist("taylor-swift", "Taylor Swift", ArtistGroup.INTERNATIONAL, "Taylor_Swift",
            "Songwriter and pop phenomenon"),
        artist("ed-sheeran", "Ed Sheeran", ArtistGroup.INTERNATIONAL, "Ed_Sheeran",
            "Acoustic pop and stadium tours"),
        artist("justin-bieber", "Justin Bieber", ArtistGroup.INTERNATIONAL, "Justin_Bieber",
            "Global pop"),
        artist("adele", "Adele", ArtistGroup.INTERNATIONAL, "Adele",
            "Soulful British vocalist"),
        artist("bruno-mars", "Bruno Mars", ArtistGroup.INTERNATIONAL, "Bruno_Mars",
            "Funk, soul and showmanship"),
        artist("the-weeknd", "The Weeknd", ArtistGroup.INTERNATIONAL, "The_Weeknd",
            "Alt-R&B and synth pop"),
        artist("billie-eilish", "Billie Eilish", ArtistGroup.INTERNATIONAL, "Billie_Eilish",
            "Whisper-pop auteur"),
        artist("eminem", "Eminem", ArtistGroup.INTERNATIONAL, "Eminem",
            "Rap icon"),
        artist("rihanna", "Rihanna", ArtistGroup.INTERNATIONAL, "Rihanna",
            "Pop and R&B superstar"),
        artist("beyonce", "Beyoncé", ArtistGroup.INTERNATIONAL, "Beyonc%C3%A9",
            "Vocalist, performer, producer",
            matchTokens = listOf("beyonce", "beyoncé", "knowles")),
        artist("freddie-mercury", "Freddie Mercury", ArtistGroup.INTERNATIONAL, "Freddie_Mercury",
            "Queen's frontman"),
        artist("elvis-presley", "Elvis Presley", ArtistGroup.INTERNATIONAL, "Elvis_Presley",
            "The King of Rock and Roll"),
        artist("john-lennon", "John Lennon", ArtistGroup.INTERNATIONAL, "John_Lennon",
            "Beatle, songwriter, activist"),
        artist("madonna", "Madonna", ArtistGroup.INTERNATIONAL, "Madonna",
            "Queen of Pop")
    )

    // --------------------------------------------------------------- Categories

    val categories: List<WallpaperCategory> = listOf(
        WallpaperCategory("bollywood-singers", "Bollywood Singers",
            "Playback voices of Hindi cinema",
            listOf(
                "Bollywood playback singer filetype:bitmap",
                "incategory:\"Bollywood singers\" filetype:bitmap",
                "Hindi film singer portrait filetype:bitmap"
            )),
        WallpaperCategory("classical-musicians", "Classical Musicians",
            "Hindustani, Carnatic and western classical",
            listOf(
                "incategory:\"Classical musicians\" filetype:bitmap",
                "classical musician concert filetype:bitmap"
            )),
        WallpaperCategory("music-composers", "Music Composers",
            "The people behind the score",
            listOf(
                "incategory:\"Composers\" filetype:bitmap",
                "film score composer filetype:bitmap",
                "music director portrait filetype:bitmap"
            )),
        WallpaperCategory("guitarists", "Guitarists",
            "Six strings, endless tone",
            listOf(
                "incategory:\"Guitarists\" filetype:bitmap",
                "guitarist performing live filetype:bitmap"
            )),
        WallpaperCategory("pianists", "Pianists",
            "Keys and quiet intensity",
            listOf(
                "incategory:\"Pianists\" filetype:bitmap",
                "pianist concert grand piano filetype:bitmap"
            )),
        WallpaperCategory("drummers", "Drummers",
            "The engine room of every band",
            listOf(
                "incategory:\"Drummers\" filetype:bitmap",
                "drummer performing live filetype:bitmap"
            )),
        WallpaperCategory("rock-bands", "Rock Bands",
            "Loud, live and legendary",
            listOf(
                "incategory:\"Rock music groups\" filetype:bitmap",
                "incategory:\"Rock musicians\" filetype:bitmap",
                "rock band on stage filetype:bitmap"
            )),
        WallpaperCategory("pop-artists", "Pop Artists",
            "Chart-topping performers",
            listOf(
                "incategory:\"Pop musicians\" filetype:bitmap",
                "pop singer stage filetype:bitmap"
            )),
        WallpaperCategory("legendary-artists", "Legendary Artists",
            "Careers that defined music",
            listOf(
                "legendary musician portrait filetype:bitmap",
                "incategory:\"Deceased singers\" filetype:bitmap",
                "vintage singer portrait filetype:bitmap"
            )),
        WallpaperCategory("female-artists", "Female Artists",
            "Voices that lead",
            listOf(
                "incategory:\"Female vocalists\" filetype:bitmap",
                "female singer portrait filetype:bitmap"
            )),
        WallpaperCategory("male-artists", "Male Artists",
            "Voices that carry",
            listOf(
                "incategory:\"Male vocalists\" filetype:bitmap",
                "male singer portrait filetype:bitmap"
            )),
        WallpaperCategory("live-performers", "Live Performers",
            "Stage lights and sweat",
            listOf(
                "incategory:\"Live performances\" filetype:bitmap",
                "live concert singer stage filetype:bitmap"
            ))
    )

    // ------------------------------------------------------------------ Lookups

    val allArtists: List<Artist> = indianSingers + internationalArtists

    /** Shown as the "Featured artists" rail on Home. */
    val featured: List<Artist> = listOf(
        "arijit-singh", "taylor-swift", "lata-mangeshkar", "the-weeknd",
        "shreya-ghoshal", "freddie-mercury", "billie-eilish", "atif-aslam"
    ).mapNotNull { bySlug(it) }

    fun bySlug(slug: String?): Artist? = allArtists.firstOrNull { it.slug == slug }

    fun byName(name: String?): Artist? =
        if (name.isNullOrBlank()) null else allArtists.firstOrNull { it.name.equals(name, ignoreCase = true) }

    fun categoryById(id: String?): WallpaperCategory? = categories.firstOrNull { it.id == id }

    /** Related artists = same group, nearest in the list, never the artist itself. */
    fun relatedTo(artist: Artist, limit: Int = 8): List<Artist> {
        val pool = if (artist.group == ArtistGroup.INDIAN) indianSingers else internationalArtists
        val index = pool.indexOfFirst { it.slug == artist.slug }.coerceAtLeast(0)
        val after = pool.drop(index + 1)
        val before = pool.take(index).reversed()
        return (after + before).take(limit)
    }

    /** Simple on-device suggestion matching used by the search bar. */
    fun suggest(query: String, limit: Int = 6): List<Artist> {
        val q = query.trim().lowercase()
        if (q.length < 2) return emptyList()
        return allArtists
            .mapNotNull { a ->
                val name = a.name.lowercase()
                val score = when {
                    name.startsWith(q) -> 0
                    name.split(" ").any { it.startsWith(q) } -> 1
                    name.contains(q) -> 2
                    a.matchTokens.any { it.startsWith(q) } -> 3
                    else -> return@mapNotNull null
                }
                score to a
            }
            .sortedBy { it.first }
            .take(limit)
            .map { it.second }
    }
}
