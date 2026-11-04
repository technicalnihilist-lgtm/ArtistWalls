package com.artistwalls.app.data.model

/**
 * Visual effects the user can switch on or off in Settings.
 * All four default to on, matching the web build.
 */
data class FxSettings(
    /** Liquid glass: glossier panels, glowing edges, glass pill on the selected tab. */
    val glass: Boolean = true,
    /** 4D depth: cards tilt with your finger, with a moving glare. */
    val depth: Boolean = true,
    /** Bounce: screens spring in and out with overshoot. */
    val bounce: Boolean = true,
    /** macOS: screens grow out of the bottom edge instead of sliding. */
    val mac: Boolean = true
)
