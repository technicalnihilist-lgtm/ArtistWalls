package com.artistwalls.app.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artistwalls.app.data.model.FxGroup
import com.artistwalls.app.data.model.FxSettings
import com.artistwalls.app.data.model.FxSlider
import com.artistwalls.app.data.model.FxSliders
import com.artistwalls.app.data.model.FxValues
import com.artistwalls.app.di.LocalAppContainer
import com.artistwalls.app.ui.components.glass
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.GoldLight
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary
import com.artistwalls.app.ui.theme.TextTertiary

private data class GroupInfo(val group: FxGroup, val title: String, val description: String)

private val GROUPS = listOf(
    GroupInfo(
        FxGroup.GLASS, "Liquid Glass",
        "Glossier panels with a diagonal sheen and glowing edges, plus a glass pill on the selected tab."
    ),
    GroupInfo(
        FxGroup.DEPTH, "4D Depth Effect",
        "Cards tilt towards your finger, rise slightly, drop a shadow and carry a moving glare."
    ),
    GroupInfo(
        FxGroup.BOUNCE, "Bounce Open / Close",
        "Screens spring in with a little overshoot and settle back when you go back."
    ),
    GroupInfo(
        FxGroup.MAC, "macOS Open / Close",
        "Screens grow out of the bottom edge and shrink back into it, like a window opening on macOS."
    )
)

/**
 * Visual effects: on/off per effect, an Adjust panel with the same sliders as the web
 * build, per-group reset, reset-everything and a Preview for the two animations.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val fx by container.fx.settings.collectAsStateWithLifecycle()

    var openGroup by remember { mutableStateOf<FxGroup?>(null) }
    var previewTick by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .glass(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Settings", style = MaterialTheme.typography.displayMedium, color = TextPrimary)
                Text(
                    "Turn effects on or off, then tap Adjust to fine-tune every detail. Saved on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        GROUPS.forEach { info ->
            FxGroupCard(
                info = info,
                fx = fx,
                open = openGroup == info.group,
                onToggle = { on -> container.fx.update { it.withGroup(info.group, on) } },
                onAdjust = { openGroup = if (openGroup == info.group) null else info.group },
                onReset = { container.fx.update { it.resetValues(info.group) } },
                onPreview = { previewTick++ },
                previewTick = previewTick,
                onValue = { slider, value ->
                    container.fx.update { current ->
                        current.copy(values = slider.set(current.values, value))
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(14.dp))
                .clickable { container.fx.update { it.resetAll() } }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "↺  Reset everything to default",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Text("Good to know", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                "Turn on both animation switches to get the macOS opening with a springy bounce. " +
                    "Animations respect your device's “remove animations” accessibility setting. " +
                    "Blur, saturation and refraction of the glass are browser-only effects — " +
                    "Compose cannot sample what is behind a panel, so those three sliders are " +
                    "not offered here.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun FxGroupCard(
    info: GroupInfo,
    fx: FxSettings,
    open: Boolean,
    onToggle: (Boolean) -> Unit,
    onAdjust: () -> Unit,
    onReset: () -> Unit,
    onPreview: () -> Unit,
    previewTick: Int,
    onValue: (FxSlider, Float) -> Unit
) {
    val enabled = fx.isOn(info.group)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(20.dp))
                .clickable { onToggle(!enabled) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    info.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(3.dp))
                Text(info.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Gold,
                    checkedBorderColor = GoldLight.copy(alpha = 0.6f),
                    uncheckedThumbColor = Color.White.copy(alpha = 0.9f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.16f),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallChip(label = "⚙  Adjust", active = open, onClick = onAdjust)
            if (info.group == FxGroup.BOUNCE || info.group == FxGroup.MAC) {
                SmallChip(label = "▶  Preview", active = false, onClick = onPreview)
            }
            SmallChip(label = "↺  Reset", active = false, onClick = onReset)
        }

        AnimatedVisibility(visible = open) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .glass(RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                FxSliders.forGroup(info.group).forEach { slider ->
                    SliderRow(
                        slider = slider,
                        value = slider.get(fx.values),
                        onValue = { onValue(slider, it) }
                    )
                }
                if (info.group == FxGroup.BOUNCE || info.group == FxGroup.MAC) {
                    AnimationPreview(fx = fx, tick = previewTick, group = info.group)
                }
            }
        }
    }
}

@Composable
private fun SmallChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) Gold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) GoldLight else TextSecondary
        )
    }
}

@Composable
private fun SliderRow(slider: FxSlider, value: Float, onValue: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                slider.label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                FxSliders.format(slider, value),
                style = MaterialTheme.typography.labelLarge,
                color = GoldLight
            )
        }
        if (slider.note != null) {
            Text(slider.note, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
        Slider(
            value = value,
            onValueChange = onValue,
            valueRange = slider.min..slider.max,
            steps = stepsFor(slider),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Gold,
                inactiveTrackColor = Color.White.copy(alpha = 0.18f)
            )
        )
    }
}

/** Discrete steps between the ends, so a slider snaps like the web one. */
private fun stepsFor(slider: FxSlider): Int {
    val span = slider.max - slider.min
    if (slider.step <= 0f) return 0
    val count = (span / slider.step).toInt() - 1
    return count.coerceIn(0, 40)
}

/** A small card that replays the current open animation, so a change is visible at once. */
@Composable
private fun AnimationPreview(fx: FxSettings, tick: Int, group: FxGroup) {
    val scale = remember { Animatable(1f) }
    val fade = remember { Animatable(1f) }
    LaunchedEffect(tick, group) {
        if (tick == 0) return@LaunchedEffect
        val v: FxValues = fx.values
        val from = if (group == FxGroup.MAC) v.macSize else 1f - 0.08f * v.bounceAmount
        scale.snapTo(from.coerceIn(0.05f, 1f))
        fade.snapTo(0f)
        fade.animateTo(1f, tween(if (group == FxGroup.MAC) (v.macDuration * 500).toInt() else 120))
        if (group == FxGroup.MAC) {
            scale.animateTo(1f, tween((v.macDuration * 1000).toInt(), easing = FastOutSlowInEasing))
        } else {
            scale.animateTo(
                1f,
                spring(
                    dampingRatio = (0.85f - 0.20f * v.bounceAmount).coerceIn(0.2f, 1f),
                    stiffness = (520f / v.bounceDuration.coerceAtLeast(0.2f)).coerceIn(150f, 1200f)
                )
            )
        }
    }
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    alpha = fade.value
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                }
                .glass(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Preview",
                style = MaterialTheme.typography.labelLarge,
                color = GoldLight,
                textAlign = TextAlign.Center
            )
        }
    }
}
