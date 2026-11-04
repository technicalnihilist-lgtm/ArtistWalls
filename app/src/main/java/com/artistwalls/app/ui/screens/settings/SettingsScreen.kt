package com.artistwalls.app.ui.screens.settings

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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artistwalls.app.di.LocalAppContainer
import com.artistwalls.app.ui.components.glass
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.GoldLight
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary
import com.artistwalls.app.ui.theme.TextTertiary

/**
 * Visual effects, switchable and remembered across launches.
 * Everything is applied live: the screen behind you restyles the moment you flip a switch.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val fx by container.fx.settings.collectAsStateWithLifecycle()

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
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Visual effects and animations. Saved on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        FxSwitchRow(
            title = "Liquid Glass",
            description = "Glossier panels with a diagonal sheen and glowing edges, " +
                "plus a glass pill on the selected tab.",
            checked = fx.glass,
            onCheckedChange = { on -> container.fx.update { it.copy(glass = on) } }
        )
        Spacer(Modifier.height(10.dp))
        FxSwitchRow(
            title = "4D Depth Effect",
            description = "Cards tilt towards your finger with a moving glare and " +
                "a deeper sense of depth.",
            checked = fx.depth,
            onCheckedChange = { on -> container.fx.update { it.copy(depth = on) } }
        )
        Spacer(Modifier.height(10.dp))
        FxSwitchRow(
            title = "Bounce Open / Close",
            description = "Screens spring in with a little overshoot and settle back " +
                "when you go back.",
            checked = fx.bounce,
            onCheckedChange = { on -> container.fx.update { it.copy(bounce = on) } }
        )
        Spacer(Modifier.height(10.dp))
        FxSwitchRow(
            title = "macOS Open / Close",
            description = "Screens grow out of the bottom edge and shrink back into it, " +
                "like a window opening on macOS.",
            checked = fx.mac,
            onCheckedChange = { on -> container.fx.update { it.copy(mac = on) } }
        )

        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "Good to know",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Turn on both animation switches to get the macOS opening with a " +
                    "springy bounce. Animations respect your device's “remove animations” " +
                    "accessibility setting, and every effect is drawn on top of the same " +
                    "photographs — nothing about the images or credits changes.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun FxSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(20.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
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
}
