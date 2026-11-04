package com.artistwalls.app.ui.screens.credits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.artistwalls.app.di.LocalAppContainer
import com.artistwalls.app.ui.components.CreditRow
import com.artistwalls.app.ui.components.SectionHeader
import com.artistwalls.app.ui.components.glass
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary
import com.artistwalls.app.ui.theme.TextTertiary

@Composable
fun CreditsScreen(onOpenWallpaper: (String) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: CreditsViewModel = viewModel(
        factory = viewModelFactory { initializer { CreditsViewModel(container) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 130.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Text(
                    text = "Credits & licensing",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Every photograph in ArtistWalls belongs to its creator.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }

        item { PolicyCard() }

        item {
            SectionHeader(
                title = "Photographs in your library",
                subtitle = "Tap a credit to open the original file page on Wikimedia Commons",
                modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
            )
        }

        if (state.credits.isEmpty() && !state.isLoading) {
            item {
                Text(
                    text = "No photographs saved yet. Favourite, download or simply open a " +
                        "wallpaper and its full credit will be listed here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(RoundedCornerShape(18.dp))
                        .padding(18.dp)
                )
            }
        }

        items(state.credits, key = { it.id }) { wallpaper ->
            Column {
                CreditRow(
                    title = wallpaper.displayTitle,
                    author = wallpaper.credit.author,
                    license = wallpaper.credit.license.name,
                    onClick = { uriHandler.openUri(wallpaper.credit.sourcePageUrl) }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            }
        }

        item {
            SectionHeader(
                title = "Where the images come from",
                modifier = Modifier.padding(top = 24.dp, bottom = 6.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                PolicyLink("Wikimedia Commons", "https://commons.wikimedia.org/", uriHandler)
                PolicyLink(
                    "Wikimedia Commons API",
                    "https://commons.wikimedia.org/w/api.php",
                    uriHandler
                )
                PolicyLink(
                    "Wikimedia Foundation — reuse of content",
                    "https://foundation.wikimedia.org/wiki/Policy:Terms_of_Use",
                    uriHandler
                )
                PolicyLink("Creative Commons licences", "https://creativecommons.org/licenses/", uriHandler)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "ArtistWalls queries the Commons API live for artist photographs. " +
                        "Only files whose own licence permits free reuse are displayed; if a " +
                        "suitable licensed photograph does not exist, the gallery shows an " +
                        "empty state instead of an unauthorised image. Removing an image from " +
                        "the app never removes it from Commons, and crediting here never " +
                        "transfers ownership.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "ArtistWalls 1.0.0 · Kotlin, Jetpack Compose, Material 3",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun PolicyCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "What this app will show",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        PolicyLine("Accepted", "Public domain · CC0 · CC BY · CC BY-SA")
        PolicyLine("Refused", "Non-free press images · album covers · fair use · unknown licences · NC/ND terms")
        PolicyLine("Credits shown", "Artist · image title · photographer or creator · licence + licence link · Commons source link")
        PolicyLine("Modification", "None. The original file is displayed; thumbnails are scaled by Wikimedia's servers")
        PolicyLine("ShareAlike", "Honoured — files are shared unmodified under the same licence")
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ArtistWalls never claims ownership of artist photographs.",
                style = MaterialTheme.typography.bodySmall,
                color = Gold
            )
        }
    }
}

@Composable
private fun PolicyLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PolicyLink(
    label: String,
    url: String,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = Gold,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(url) }
            .padding(vertical = 8.dp)
    )
}
