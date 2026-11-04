package com.artistwalls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.artistwalls.app.di.LocalFxSettings
import com.artistwalls.app.ui.theme.GlassBorder
import com.artistwalls.app.ui.theme.GlassFill

/** The original dark glass: a low-alpha fill with a hairline border. */
fun Modifier.plainGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    fill: Color = GlassFill,
    border: Color = GlassBorder,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            listOf(fill.copy(alpha = fill.alpha * 1.4f), fill.copy(alpha = fill.alpha * 0.7f))
        ),
        shape = shape
    )
    .border(borderWidth, border, shape)

/**
 * "Liquid glass": a diagonal gloss across the surface plus a bright, glowing edge —
 * the closest Compose can get to iOS-style glass without sampling the backdrop.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    tint: Float = 1f,
    rim: Float = 1f
): Modifier = this
    .clip(shape)
    .background(
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = (0.185f * tint).coerceIn(0f, 1f)),
                Color.White.copy(alpha = (0.055f * tint).coerceIn(0f, 1f)),
                Color.White.copy(alpha = (0.105f * tint).coerceIn(0f, 1f))
            )
        ),
        shape = shape
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = (0.58f * rim).coerceIn(0f, 1f)),
                Color.White.copy(alpha = (0.13f * rim).coerceIn(0f, 1f)),
                Color.White.copy(alpha = (0.34f * rim).coerceIn(0f, 1f))
            )
        ),
        shape = shape
    )

/**
 * Single entry point used by every surface in the app. It reads the Liquid Glass
 * switch, so flipping it in Settings restyles panels, chips, bars and the tab bar
 * at once with no other code changes.
 */
@Composable
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(20.dp),
    fill: Color = GlassFill,
    border: Color = GlassBorder,
    borderWidth: Dp = 1.dp
): Modifier {
    val fx = LocalFxSettings.current
    return if (fx.glass) {
        liquidGlass(shape, tint = fx.values.glassTint, rim = fx.values.glassRim)
    } else {
        plainGlass(shape, fill, border, borderWidth)
    }
}

/** A floating glass panel used for headers, credit blocks and action bars. */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val base = modifier.glass(shape)
    Box(
        modifier = if (onClick != null) base.clickable(onClick = onClick) else base,
        content = content
    )
}

/** Bottom-up scrim so white text always sits on a readable base over photography. */
fun Modifier.photoScrim(intensity: Float = 0.85f): Modifier = this.background(
    Brush.verticalGradient(
        0f to Color.Transparent,
        0.45f to Color.Black.copy(alpha = 0.25f * intensity),
        0.78f to Color.Black.copy(alpha = 0.72f * intensity),
        1f to Color.Black.copy(alpha = 0.94f * intensity)
    )
)

/** Top scrim, for the bar over a full-screen photograph. */
fun Modifier.topScrim(intensity: Float = 0.8f): Modifier = this.background(
    Brush.verticalGradient(
        0f to Color.Black.copy(alpha = 0.75f * intensity),
        0.5f to Color.Black.copy(alpha = 0.25f * intensity),
        1f to Color.Transparent
    )
)
