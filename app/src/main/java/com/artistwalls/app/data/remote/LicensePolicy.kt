package com.artistwalls.app.data.remote

/**
 * The single gatekeeper for copyright. A photograph is only ever shown if it
 * passes here.
 *
 * Accepted (all allow free commercial reuse with attribution):
 *  - Public domain / PD-* / Public domain mark
 *  - CC0 / CC-0
 *  - CC BY (any version, any jurisdiction port)
 *  - CC BY-SA (any version) — ShareAlike honoured by shipping the file unmodified
 *
 * Rejected: anything non-free, "fair use", all rights reserved, unknown/empty,
 * NonCommercial (-NC), NoDerivatives (-ND) and GFDL-only files (not a free
 * content licence for images on Commons).
 */
object LicensePolicy {

    private val REJECT_MARKERS = listOf(
        "non-free", "nonfree", "fair use", "fairuse", "all rights reserved",
        "copyright", "no license", "unknown", "gfdl", "permission"
    )

    private val NC_MARKERS = listOf("-nc", " nc ", "nc-", "noncommercial", "non-commercial")
    private val ND_MARKERS = listOf("-nd", " nd ", "nd-", "noderiv", "no derivative")

    /** Returns true only for licences we may display and distribute. */
    fun isAccepted(shortName: String?, licenseUrl: String?): Boolean {
        val n = shortName?.trim()?.lowercase().orEmpty()
        if (n.isEmpty()) return false

        if (REJECT_MARKERS.any { n.contains(it) }) return false
        if (NC_MARKERS.any { n.contains(it) }) return false
        if (ND_MARKERS.any { n.contains(it) }) return false

        if (n.startsWith("public domain") || n == "pd" || n.startsWith("pd-") ||
            n == "public domain mark 1.0" || n == "no restrictions"
        ) return true

        if (n.startsWith("cc0") || n.startsWith("cc-0") || n.startsWith("cc zero")) return true

        if (n.startsWith("cc by") || n.startsWith("cc-by") || n.startsWith("attribution")) {
            // CC BY / CC BY-SA in any version or port.
            return true
        }
        return false
    }

    /** True when the licence obliges us to license derivatives under the same terms. */
    fun isShareAlike(shortName: String?): Boolean {
        val n = shortName?.lowercase().orEmpty()
        return n.contains("sa") && (n.startsWith("cc by-sa") || n.contains("share"))
    }

    fun requiresAttribution(shortName: String?): Boolean {
        val n = shortName?.trim()?.lowercase().orEmpty()
        if (n.startsWith("public domain") || n == "pd" || n.startsWith("pd-")) return false
        if (n.startsWith("cc0") || n.startsWith("cc-0")) return false
        return true
    }

    /**
     * We never alter the photograph itself: the app displays the original file and
     * Wikimedia's own server-side thumbnail. This sentence is shown to the user so
     * the "modification" position is explicit rather than assumed.
     */
    fun modificationNote(shareAlike: Boolean): String = if (shareAlike) {
        "Unmodified. Original file shown as published on Wikimedia Commons; the smaller " +
            "preview is scaled by Wikimedia's thumbnail servers, not by ArtistWalls. " +
            "Shared under the same licence (ShareAlike)."
    } else {
        "Unmodified. Original file shown as published on Wikimedia Commons; the smaller " +
            "preview is scaled by Wikimedia's thumbnail servers, not by ArtistWalls."
    }
}
