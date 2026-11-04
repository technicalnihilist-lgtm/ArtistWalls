package com.artistwalls.app.ui.screens.wallpaper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.artistwalls.app.data.model.ImageQuality
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.data.wallpaper.WallpaperSetter
import com.artistwalls.app.di.LocalAppContainer
import com.artistwalls.app.ui.components.ErrorState
import com.artistwalls.app.ui.components.GlassButton
import com.artistwalls.app.ui.components.GoldButton
import com.artistwalls.app.ui.components.LicenseChip
import com.artistwalls.app.ui.components.LowResolutionNotice
import com.artistwalls.app.ui.components.QualityBadge
import com.artistwalls.app.ui.components.RemoteImage
import com.artistwalls.app.ui.components.glass
import com.artistwalls.app.ui.components.photoScrim
import com.artistwalls.app.ui.components.topScrim
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.GoldLight
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary
import com.artistwalls.app.ui.theme.TextTertiary

@Composable
fun WallpaperDetailScreen(
    wallpaperId: String,
    onBack: () -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenCredits: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: WallpaperDetailViewModel = viewModel(
        key = wallpaperId,
        factory = viewModelFactory { initializer { WallpaperDetailViewModel(wallpaperId, container) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    var showDetails by remember { mutableStateOf(true) }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val wallpaper = state.wallpaper
        val density = LocalDensity.current

        // The info panel is measured so the photograph can be given exactly the space
        // that is left above it — that is what keeps a portrait shot portrait and a
        // landscape shot landscape, whole and uncropped, instead of hiding it behind
        // the panel.
        var panelHeightPx by remember(wallpaperId) { mutableStateOf(0) }
        var panelOpen by remember(wallpaperId) { mutableStateOf(true) }
        val panelPadding = if (panelOpen) with(density) { panelHeightPx.toDp() } else 0.dp

        if (wallpaper != null) {
            // 1. a blurred copy fills the letterbox, so nothing looks empty
            RemoteImage(
                url = wallpaper.previewUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(32.dp),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.66f)))
            Box(modifier = Modifier.fillMaxSize().topScrim(0.55f))

            // 2. the photograph itself, never cropped: Fit keeps its own aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = panelPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { panelOpen = !panelOpen }
            ) {
                RemoteImage(
                    url = wallpaper.previewUrl,
                    contentDescription = wallpaper.displayTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (wallpaper != null && panelOpen) {
            Text(
                text = "TAP THE PHOTO TO SEE IT FULL SCREEN",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = with(density) { panelHeightPx.toDp() } + 10.dp)
            )
        }

        // ------------------------------------------------------------ top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(icon = Icons.Rounded.ArrowBack, label = "Back", onClick = onBack)
            Spacer(Modifier.weight(1f))
            if (wallpaper != null) {
                RoundIconButton(
                    icon = Icons.Rounded.Share,
                    label = "Share",
                    onClick = { viewModel.share() }
                )
                Spacer(Modifier.width(8.dp))
                RoundIconButton(
                    icon = if (state.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    label = "Favourite",
                    tint = if (state.isFavorite) Gold else Color.White,
                    onClick = { viewModel.toggleFavorite() }
                )
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Gold, strokeWidth = 2.dp)
            }
        }

        state.error?.let { message ->
            if (wallpaper == null) {
                Box(modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
                    ErrorState(message = message, onRetry = { viewModel.load() })
                }
            }
        }

        // --------------------------------------------------------- info panel
        if (wallpaper != null && panelOpen) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .heightIn(max = 430.dp)
                    .onSizeChanged { panelHeightPx = it.height }
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .glass(RoundedCornerShape(26.dp))
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = wallpaper.artistName.ifBlank { wallpaper.displayTitle },
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = wallpaper.displayTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    QualityBadge(quality = wallpaper.quality)
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${wallpaper.resolution} · ${wallpaper.megapixels}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    LicenseChip(
                        licenseName = wallpaper.credit.license.name,
                        onClick = { uriHandler.openUri(wallpaper.credit.license.url) }
                    )
                }

                if (wallpaper.quality == ImageQuality.PREVIEW_ONLY) {
                    Spacer(Modifier.height(10.dp))
                    LowResolutionNotice()
                }

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoldButton(
                        label = if (state.isBusy) "Working…" else "Download",
                        icon = Icons.Rounded.Download,
                        enabled = !state.isBusy,
                        onClick = { viewModel.download() },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        label = if (state.isFavorite) "Saved" else "Favourite",
                        icon = if (state.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        onClick = { viewModel.toggleFavorite() }
                    )
                }

                state.progress?.let { progress ->
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        color = Gold,
                        trackColor = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassButton(
                        label = "Home screen",
                        onClick = { viewModel.setAs(WallpaperSetter.Target.HOME) },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        label = "Lock screen",
                        onClick = { viewModel.setAs(WallpaperSetter.Target.LOCK) },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        label = "Both",
                        onClick = { viewModel.setAs(WallpaperSetter.Target.BOTH) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDetails = !showDetails }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CREDITS & LICENCE",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldLight,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (showDetails) "HIDE" else "SHOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }

                AnimatedVisibility(visible = showDetails) {
                    Column {
                        CreditLine(label = "Photograph by", value = wallpaper.credit.author)
                        CreditLine(label = "Image title", value = wallpaper.title)
                        CreditLine(label = "Licence", value = wallpaper.credit.license.name)
                        if (wallpaper.credit.license.usageTerms.isNotBlank()) {
                            CreditLine(
                                label = "Usage terms",
                                value = wallpaper.credit.license.usageTerms
                            )
                        }
                        CreditLine(label = "Modification", value = wallpaper.credit.license.modificationNote)
                        if (wallpaper.description.isNotBlank()) {
                            CreditLine(label = "Description", value = wallpaper.description)
                        }
                        if (wallpaper.dateOriginal.isNotBlank()) {
                            CreditLine(label = "Date", value = wallpaper.dateOriginal)
                        }

                        Spacer(Modifier.height(10.dp))
                        LinkRow(
                            label = "Original file on Wikimedia Commons",
                            onClick = { uriHandler.openUri(wallpaper.credit.sourcePageUrl) }
                        )
                        LinkRow(
                            label = "Licence deed",
                            onClick = { uriHandler.openUri(wallpaper.credit.license.url) }
                        )
                        val artistSlug = slugFor(wallpaper.artistName)
                        if (artistSlug.isNotBlank()) {
                            LinkRow(
                                label = "More wallpapers of ${wallpaper.artistName}",
                                onClick = { onOpenArtist(artistSlug) }
                            )
                        }
                        LinkRow(label = "All credits in ArtistWalls", onClick = onOpenCredits)

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "ArtistWalls does not own this photograph. It is shown here " +
                                "under the licence above and remains the property of its creator.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
        )
    }
}

private fun slugFor(artistName: String): String =
    com.artistwalls.app.data.catalog.ArtistCatalog.byName(artistName)?.slug.orEmpty()

@Composable
private fun RoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun CreditLine(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Rounded.OpenInNew,
            contentDescription = null,
            tint = Gold.copy(alpha = 0.8f),
            modifier = Modifier.size(15.dp)
        )
    }
}
