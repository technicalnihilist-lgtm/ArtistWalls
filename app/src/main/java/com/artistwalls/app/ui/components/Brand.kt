package com.artistwalls.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.GoldLight
import com.artistwalls.app.ui.theme.TextPrimary

/**
 * The ArtistWalls mark: a thin metallic ring around a five-bar soundwave.
 * Drawn, not bitmapped, so it stays crisp at any size.
 */
@Composable
fun ArtistWallsMark(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 30.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = this.size.minDimension * 0.055f
        drawCircle(
            color = Gold,
            radius = this.size.minDimension / 2 - stroke,
            style = Stroke(width = stroke)
        )
        val barWidth = this.size.width * 0.075f
        val heights = listOf(0.30f, 0.52f, 0.66f, 0.44f, 0.24f)
        val gap = this.size.width * 0.075f
        val totalWidth = heights.size * barWidth + (heights.size - 1) * gap
        var x = (this.size.width - totalWidth) / 2
        heights.forEach { factor ->
            val barHeight = this.size.height * factor
            drawRoundRect(
                color = GoldLight,
                topLeft = Offset(x, (this.size.height - barHeight) / 2),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
            x += barWidth + gap
        }
    }
}

@Composable
fun ArtistWallsWordmark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ArtistWallsMark(size = if (compact) 24.dp else 30.dp)
        Spacer(Modifier.width(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
            Text(
                text = "Artist",
                style = if (compact) MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = "Walls",
                style = if (compact) MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.headlineMedium,
                color = GoldLight
            )
        }
    }
}
