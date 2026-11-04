package com.artistwalls.app.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.artistwalls.app.data.model.FxSettings

/**
 * 4D depth: the card tilts towards your finger and carries a soft glare that follows it.
 *
 * Pointer events are read on the Initial pass and never consumed, so the card stays
 * fully clickable — a tap still opens the wallpaper, a drag just tilts.
 */
@Composable
fun Modifier.depthTilt(enabled: Boolean, maxDegrees: Float = 8f): Modifier {
    if (!enabled) return this

    var target by remember { mutableStateOf(Offset.Zero) }
    var pointer by remember { mutableStateOf(Offset.Zero) }
    var pressed by remember { mutableStateOf(false) }

    val tiltSpec: FiniteAnimationSpec<Float> =
        spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
    val rx by animateFloatAsState(-target.y * maxDegrees, tiltSpec, label = "tiltX")
    val ry by animateFloatAsState(target.x * maxDegrees, tiltSpec, label = "tiltY")
    val glare by animateFloatAsState(if (pressed) 1f else 0f, tween(220), label = "glare")

    return this
        .graphicsLayer {
            rotationX = rx
            rotationY = ry
            cameraDistance = 14f * density
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull() ?: continue
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    if (w <= 0f || h <= 0f) continue
                    val nx = (change.position.x / w) * 2f - 1f
                    val ny = (change.position.y / h) * 2f - 1f
                    pointer = Offset(nx, ny)
                    pressed = change.pressed
                    target = if (change.pressed) Offset(nx, ny) else Offset.Zero
                }
            }
        }
        .drawWithContent {
            drawContent()
            if (glare > 0.01f) {
                val center = Offset(
                    size.width * (0.5f + pointer.x * 0.35f),
                    size.height * (0.5f + pointer.y * 0.35f)
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.10f * glare), Color.Transparent),
                        center = center,
                        radius = size.maxDimension * 0.85f
                    )
                )
            }
        }
}

/** Screen-entrance used by the navigator: macOS genie, spring bounce, or a plain fade. */
fun fxEnterTransition(fx: FxSettings): EnterTransition = when {
    fx.mac && fx.bounce -> scaleIn(
        animationSpec = spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessMediumLow),
        initialScale = 0.86f,
        transformOrigin = TransformOrigin(0.5f, 1f)
    ) + fadeIn(tween(160))
    fx.mac -> scaleIn(
        animationSpec = tween(340, easing = FastOutSlowInEasing),
        initialScale = 0.90f,
        transformOrigin = TransformOrigin(0.5f, 1f)
    ) + fadeIn(tween(220))
    fx.bounce -> scaleIn(
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
        initialScale = 0.94f,
        transformOrigin = TransformOrigin.Center
    ) + fadeIn(tween(160))
    else -> fadeIn(tween(180))
}

fun fxExitTransition(fx: FxSettings): ExitTransition =
    fadeOut(tween(if (fx.mac) 200 else 140)) +
        scaleOut(
            targetScale = if (fx.bounce || fx.mac) 0.97f else 1f,
            animationSpec = tween(180)
        )
