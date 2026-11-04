package com.artistwalls.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Editorial pairing: a serif display face for artist names (gallery-catalogue feel)
 * and a clean sans for UI text, with generous letter spacing on labels.
 */
private val Display = FontFamily.Serif
private val Ui = FontFamily.SansSerif

val ArtistWallsTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Display, fontWeight = FontWeight.Normal,
        fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Display, fontWeight = FontWeight.Normal,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.3).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Display, fontWeight = FontWeight.Medium,
        fontSize = 26.sp, lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Display, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Display, fontWeight = FontWeight.Medium,
        fontSize = 19.sp, lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Ui, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Ui, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Ui, fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp, lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Ui, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Ui, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, letterSpacing = 0.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Ui, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, letterSpacing = 1.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Ui, fontWeight = FontWeight.Medium,
        fontSize = 10.sp, letterSpacing = 1.6.sp
    )
)
