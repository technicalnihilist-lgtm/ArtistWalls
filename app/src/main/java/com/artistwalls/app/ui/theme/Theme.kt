package com.artistwalls.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ArtistWallsColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1A1400),
    primaryContainer = GoldDeep,
    onPrimaryContainer = GoldLight,
    secondary = Silver,
    onSecondary = Color(0xFF101014),
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = SurfaceDeep,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassFill,
    error = ErrorRed,
    onError = Color(0xFF1A0503)
)

/**
 * The app is intentionally dark-only: an AMOLED black canvas is the whole point of
 * a wallpaper gallery, and it keeps photographs the brightest thing on screen.
 */
@Composable
fun ArtistWallsTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ArtistWallsColors,
        typography = ArtistWallsTypography,
        content = content
    )
}
