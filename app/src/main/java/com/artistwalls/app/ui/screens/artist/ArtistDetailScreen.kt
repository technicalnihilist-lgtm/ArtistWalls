package com.artistwalls.app.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.artistwalls.app.data.model.Artist
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.di.LocalAppContainer
import com.artistwalls.app.ui.components.ArtistRailCard
import com.artistwalls.app.ui.components.EmptyState
import com.artistwalls.app.ui.components.ErrorState
import com.artistwalls.app.ui.components.GlassButton
import com.artistwalls.app.ui.components.RemoteImage
import com.artistwalls.app.ui.components.SectionHeader
import com.artistwalls.app.ui.components.ShimmerTile
import com.artistwalls.app.ui.components.WallpaperTile
import com.artistwalls.app.ui.components.glass
import com.artistwalls.app.ui.components.photoScrim
import com.artistwalls.app.ui.components.topScrim
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.GoldLight
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary
import com.artistwalls.app.ui.theme.TextTertiary

@Composable
fun ArtistDetailScreen(
    slug: String,
    onBack: () -> Unit,
    onOpenWallpaper: (String) -> Unit,
    onOpenArtist: (String) -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: ArtistDetailViewModel = viewModel(
        key = slug,
        factory = viewModelFactory { initializer { ArtistDetailViewModel(slug, container) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val uriHandler = LocalUriHandler.current

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 4
        }
    }
    LaunchedEffect(shouldLoadMore, state.hasMore) {
        if (shouldLoadMore && state.hasMore && !state.isLoading) viewModel.loadMore()
    }

    fun open(wallpaper: Wallpaper) {
        container.selection.select(wallpaper)
        onOpenWallpaper(wallpaper.id)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ArtistHeader(
                artist = state.artist,
                hero = state.hero,
                isLoading = state.isLoading,
                onBack = onBack,
                onOpenWikipedia = { url -> uriHandler.openUri(url) }
            )
        }

        if (state.isLoading) {
            items(6) { ShimmerTile(modifier = Modifier.height(230.dp)) }
        }

        state.error?.let { message ->
            if (state.items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        ErrorState(message = message, onRetry = { viewModel.load() })
                    }
                }
            }
        }

        if (!state.isLoading && state.items.isEmpty() && state.error == null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    EmptyState(
                        title = "No licensed photographs yet",
                        message = "Wikimedia Commons currently holds no freely licensed " +
                            "photograph of ${state.artist?.name ?: "this artist"}. Rather than " +
                            "use a press image without permission, this gallery stays empty " +
                            "until a licensed photograph is published.",
                        icon = Icons.Rounded.Search
                    )
                }
            }
        }

        if (state.items.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    title = "Wallpaper collection",
                    subtitle = "${state.items.size} photographs · live from Commons",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
            items(state.items, key = { it.id }) { wallpaper ->
                WallpaperTile(
                    wallpaper = wallpaper,
                    onClick = { open(wallpaper) },
                    isFavorite = state.favoriteIds.contains(wallpaper.id)
                )
            }
        }

        if (state.related.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    SectionHeader(
                        title = "Related artists",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.related, key = { it.slug }) { artist ->
                            ArtistRailCard(artist = artist, onClick = { onOpenArtist(artist.slug) })
                        }
                    }
                }
            }
        }

        if (state.items.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CreditsBlock(
                    items = state.items,
                    onOpenSource = { url -> uriHandler.openUri(url) }
                )
            }
        }

        if (state.isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Gold, strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun ArtistHeader(
    artist: Artist?,
    hero: Wallpaper?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onOpenWikipedia: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        if (hero != null) {
            RemoteImage(
                url = hero.previewUrl,
                contentDescription = artist?.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (isLoading) {
            ShimmerTile(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(0.dp))
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF14141A), Color.Black)))
            )
        }
        Box(modifier = Modifier.fillMaxSize().topScrim(1f))
        Box(modifier = Modifier.fillMaxSize().photoScrim(1f))

        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Text(
                text = artist?.group?.label.orEmpty().uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = GoldLight
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = artist?.name ?: "",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!artist?.tagline.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = artist?.tagline.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                artist?.let {
                    GlassButton(
                        label = "Biography on Wikipedia",
                        icon = Icons.Rounded.OpenInNew,
                        onClick = { onOpenWikipedia(it.wikipediaUrl) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditsBlock(items: List<Wallpaper>, onOpenSource: (String) -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .fillMaxWidth()
            .glass(RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "Image credits",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Photographs are the work of their creators on Wikimedia Commons. " +
                "ArtistWalls claims no ownership. Tap any credit to open the original file page.",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
        Spacer(Modifier.height(10.dp))
        items.take(20).forEach { wallpaper ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSource(wallpaper.credit.sourcePageUrl) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wallpaper.displayTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${wallpaper.credit.author} · ${wallpaper.credit.license.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.width(16.dp)
                )
            }
        }
    }
}
