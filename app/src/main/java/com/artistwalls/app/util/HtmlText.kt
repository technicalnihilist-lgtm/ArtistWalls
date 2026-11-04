package com.artistwalls.app.util

/**
 * Wikimedia's extmetadata values are HTML fragments (links, <bdi>, <span> …).
 * The app needs clean, readable attribution text, so tags are stripped and the
 * common entities decoded. Pure Kotlin: no Android dependency, unit-testable.
 */
object HtmlText {

    private val TAG = Regex("<[^>]*>")
    private val SPACES = Regex("[\\s\\u00A0]+")

    fun clean(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var s = TAG.replace(raw, " ")
        s = s.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&ndash;", "–")
            .replace("&mdash;", "—")
        return SPACES.replace(s, " ").trim()
    }

    fun truncate(raw: String?, max: Int): String {
        val s = clean(raw)
        if (s.length <= max) return s
        val cut = s.take(max)
        val lastSpace = cut.lastIndexOf(' ')
        return (if (lastSpace > max * 0.6) cut.take(lastSpace) else cut).trimEnd(' ', ',', ';', '.') + "…"
    }
}
