package com.artistwalls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.artistwalls.app.data.model.Artist
import com.artistwalls.app.data.model.ImageQuality
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.data.model.WallpaperCategory
import com.artistwalls.app.di.LocalAppContainer
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.GoldLight
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary
import com.artistwalls.app.ui.theme.TextTertiary

/**
 * An artist's lead photograph, fetched live the first time the card scrolls into
 * view and then cached. Falls back to an elegant monogram, never to a fake image.
 */
@Composable
fun ArtistPhoto(
    artist: Artist,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape
) {
    val cache = LocalAppContainer.current.artistImageCache
    var url by remember(artist.slug) { mutableStateOf<String?>(cache.cached(artist)) }

    LaunchedEffect(artist.slug) {
        if (url.isNullOrBlank()) url = cache.thumbnail(artist)
    }

    if (url.isNullOrBlank()) {
        Monogram(initial = artist.initial, modifier = modifier, shape = shape)
    } else {
        RemoteImage(
            url = url,
            contentDescription = artist.name,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            shape = shape
        )
    }
}

/** Round artist card used in the "Popular Indian singers" style rails. */
@Composable
fun ArtistRailCard(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(112.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArtistPhoto(
            artist = artist,
            modifier = Modifier
                .size(88.dp)
                .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = artist.group.label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Large editorial card for the featured rail on Home. */
@Composable
fun FeaturedArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(168.dp)
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        ArtistPhoto(
            artist = artist,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(22.dp)
        )
        Box(modifier = Modifier.fillMaxSize().photoScrim())
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = artist.tagline.ifBlank { artist.group.label },
                style = MaterialTheme.typography.labelSmall,
                color = GoldLight.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Grid tile for a single photograph. */
@Composable
fun WallpaperTile(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false
) {
    Box(
        modifier = modifier
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        RemoteImage(
            url = wallpaper.thumbUrl,
            contentDescription = wallpaper.displayTitle,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(18.dp)
        )
        Box(modifier = Modifier.fillMaxSize().photoScrim(0.7f))

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isFavorite) {
                Icon(
                    Icons.Rounded.Favorite,
                    contentDescription = "Favourite",
                    tint = Gold,
                    modifier = Modifier.size(14.dp)
                )
            }
            QualityBadge(quality = wallpaper.quality)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = wallpaper.artistName.ifBlank { wallpaper.displayTitle },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = wallpaper.resolution,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

/** Wide hero used for the daily artist wallpaper. */
@Composable
fun DailyHeroCard(
    wallpaper: Wallpaper,
    onOpen: () -> Unit,
    onSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.32f)
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onOpen)
    ) {
        RemoteImage(
            url = wallpaper.thumbUrl,
            contentDescription = wallpaper.displayTitle,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            shape = RoundedCornerShape(26.dp)
        )
        Box(modifier = Modifier.fillMaxSize().photoScrim(1f))

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Gold)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "DAILY ARTIST WALLPAPER",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldLight
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = wallpaper.artistName.ifBlank { wallpaper.displayTitle },
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${wallpaper.resolution} · ${wallpaper.credit.license.name}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldButton(label = "Set wallpaper", onClick = onSet)
                GlassButton(label = "View", onClick = onOpen)
            }
        }
    }
}

/** Category tile for the "Other categories" grid. */
@Composable
fun CategoryCard(
    category: WallpaperCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    countLabel: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = countLabel ?: category.blurb,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

/** Compact row used in the Credits screen. */
@Composable
fun CreditRow(
    title: String,
    author: String,
    license: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$author · $license",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

/** Small inline licence chip; tapping opens the licence deed. */
@Composable
fun LicenseChip(licenseName: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Gold.copy(alpha = 0.12f))
            .border(1.dp, Gold.copy(alpha = 0.45f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Lock,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = licenseName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = GoldLight
        )
    }
}

/** Marks a photograph that only exists as a small original on Commons. */
@Composable
fun LowResolutionNotice(modifier: Modifier = Modifier) {
    Row(
            modifier = modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QualityBadge(quality = ImageQuality.PREVIEW_ONLY)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Only a small original exists on Commons — it may not fill a modern screen.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
