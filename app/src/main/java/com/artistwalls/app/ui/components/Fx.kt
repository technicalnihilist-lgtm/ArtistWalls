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
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.unit.dp
import com.artistwalls.app.data.model.FxSettings

/**
 * Shared tilt reading for one card: the pointer position (-1..1 on both axes) and
 * whether a finger is down. The card itself and the photo inside it both read this,
 * which is what makes the parallax possible.
 */
@Stable
class TiltState {
    var normalized by mutableStateOf(Offset.Zero)
    var pressed by mutableStateOf(false)
}

@Composable
fun rememberTiltState(): TiltState = remember { TiltState() }

/**
 * 4D depth: the card tilts towards your finger, rises a little, drops a shadow and
 * carries a glare that follows the touch.
 *
 * Pointer events are read on the Initial pass and never consumed, so the card stays
 * fully clickable — a tap still opens the wallpaper, a drag just tilts.
 */
@Composable
fun Modifier.depthTilt(
    enabled: Boolean,
    state: TiltState,
    maxDegrees: Float = 9f,
    glareAmount: Float = 1f,
    lift: Float = 22f,
    shadow: Float = 1f
): Modifier {
    if (!enabled) return this

    val tiltSpec: FiniteAnimationSpec<Float> =
        spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
    val rx by animateFloatAsState(-state.normalized.y * maxDegrees, tiltSpec, label = "tiltX")
    val ry by animateFloatAsState(state.normalized.x * maxDegrees, tiltSpec, label = "tiltY")
    val press by animateFloatAsState(if (state.pressed) 1f else 0f, tween(220), label = "press")
    val glare by animateFloatAsState(
        if (state.pressed) glareAmount.coerceIn(0f, 2f) else 0f, tween(220), label = "glare"
    )

    return this
        .graphicsLayer {
            rotationX = rx
            rotationY = ry
            cameraDistance = 14f * density
            transformOrigin = TransformOrigin(0.5f, 0.5f)
            val grow = 1f + (lift.coerceIn(0f, 60f) / 1000f) * press
            scaleX = grow
            scaleY = grow
            shadowElevation = (shadow.coerceIn(0f, 2f) * 16f * press) * density
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
                    state.pressed = change.pressed
                    state.normalized = if (change.pressed) Offset(nx, ny) else Offset.Zero
                }
            }
        }
        .drawWithContent {
            drawContent()
            if (glare > 0.01f) {
                val center = Offset(
                    size.width * (0.5f + state.normalized.x * 0.35f),
                    size.height * (0.5f + state.normalized.y * 0.35f)
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

/**
 * Photo parallax: the photograph inside a tilted card drifts against the tilt, which
 * reads as depth behind the glass.
 */
@Composable
fun Modifier.parallax(state: TiltState, amount: Float): Modifier {
    if (amount <= 0.01f) return this
    return this.graphicsLayer {
        val dx = -state.normalized.x * 12.dp.toPx() * amount.coerceIn(0f, 2f)
        val dy = -state.normalized.y * 12.dp.toPx() * amount.coerceIn(0f, 2f)
        scaleX = 1.06f
        scaleY = 1.06f
        translationX = dx
        translationY = dy
    }
}

/** Screen-entrance used by the navigator: macOS genie, spring bounce, or a plain fade. */
fun fxEnterTransition(fx: FxSettings): EnterTransition {
    val v = fx.values
    val macMs = (v.macDuration * 1000f).toInt().coerceIn(120, 1400)
    val bounceSpec = spring<Float>(
        dampingRatio = (0.85f - 0.20f * v.bounceAmount).coerceIn(0.20f, 1f),
        stiffness = (520f / v.bounceDuration.coerceAtLeast(0.2f)).coerceIn(150f, 1200f)
    )
    return when {
        fx.mac && fx.bounce -> scaleIn(
            animationSpec = bounceSpec,
            initialScale = v.macSize.coerceIn(0.05f, 0.9f),
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + fadeIn(tween((macMs * 0.6f).toInt().coerceAtLeast(80)))
        fx.mac -> scaleIn(
            animationSpec = tween(macMs, easing = FastOutSlowInEasing),
            initialScale = v.macSize.coerceIn(0.05f, 0.9f),
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + fadeIn(tween((macMs * 0.7f).toInt().coerceAtLeast(80)))
        fx.bounce -> scaleIn(
            animationSpec = bounceSpec,
            initialScale = (1f - 0.06f * v.bounceAmount).coerceIn(0.7f, 1f),
            transformOrigin = TransformOrigin.Center
        ) + fadeIn(tween(160))
        else -> fadeIn(tween(180))
    }
}

fun fxExitTransition(fx: FxSettings): ExitTransition {
    val v = fx.values
    val macMs = (v.macDuration * 1000f).toInt().coerceIn(120, 1400)
    return fadeOut(tween(if (fx.mac) (macMs * 0.65f).toInt().coerceAtLeast(80) else 140)) +
        scaleOut(
            targetScale = if (fx.bounce || fx.mac) 0.97f else 1f,
            animationSpec = tween((if (fx.mac) macMs * 0.65f else 180f).toInt().coerceAtLeast(80))
        )
}
