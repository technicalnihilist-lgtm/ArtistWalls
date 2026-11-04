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
import androidx.compose.ui.unit.dp
import com.artistwalls.app.ui.theme.GlassBorder
import com.artistwalls.app.ui.theme.GlassFill

/** Dark glassmorphism: a very low-alpha fill, a hairline border, no heavy blur. */
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(20.dp),
    fill: Color = GlassFill,
    border: Color = GlassBorder,
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            listOf(fill.copy(alpha = fill.alpha * 1.4f), fill.copy(alpha = fill.alpha * 0.7f))
        ),
        shape = shape
    )
    .border(borderWidth, border, shape)

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
