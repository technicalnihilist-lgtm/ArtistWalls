package com.artistwalls.app.ui.screens.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.artistwalls.app.data.catalog.ArtistCatalog
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.di.LocalAppContainer
import com.artistwalls.app.ui.components.ArtistRailCard
import com.artistwalls.app.ui.components.ArtistWallsWordmark
import com.artistwalls.app.ui.components.CategoryCard
import com.artistwalls.app.ui.components.DailyHeroCard
import com.artistwalls.app.ui.components.ErrorState
import com.artistwalls.app.ui.components.FeaturedArtistCard
import com.artistwalls.app.ui.components.GlassSearchBar
import com.artistwalls.app.ui.components.SectionHeader
import com.artistwalls.app.ui.components.ShimmerTile
import com.artistwalls.app.ui.components.WallpaperTile
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary
import com.artistwalls.app.ui.theme.TextTertiary
import com.artistwalls.app.ui.components.glass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenArtist: (String) -> Unit,
    onOpenWallpaper: (String) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCredits: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(container) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    fun open(wallpaper: Wallpaper) {
        container.selection.select(wallpaper)
        onOpenWallpaper(wallpaper.id)
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.load(refresh = true) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 118.dp)
        ) {
            item {
                HomeHeader(
                    onOpenCredits = onOpenCredits,
                    onOpenSearch = onOpenSearch
                )
            }

            if (state.error != null && state.trending.isEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.load() })
                    }
                }
            }

            if (state.isLoading) {
                item { LoadingHeader() }
            }

            state.daily?.let { daily ->
                item {
                    DailyHeroCard(
                        wallpaper = daily,
                        onOpen = { open(daily) },
                        onSet = { open(daily) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            if (state.featured.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Featured artists",
                        subtitle = "Hand-picked, photographed live on Commons",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.featured, key = { it.slug }) { artist ->
                            FeaturedArtistCard(artist = artist, onClick = { onOpenArtist(artist.slug) })
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Popular Indian singers",
                    subtitle = "16 artists · live gallery",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ArtistCatalog.indianSingers, key = { it.slug }) { artist ->
                        ArtistRailCard(artist = artist, onClick = { onOpenArtist(artist.slug) })
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Trending wallpapers",
                    subtitle = "Most-linked music photography on Commons",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
            wallpaperGrid(
                wallpapers = state.trending,
                favoriteIds = state.favoriteIds,
                onOpen = { open(it) },
                loading = state.isLoading
            )

            item {
                SectionHeader(
                    title = "International artists",
                    subtitle = "15 artists · live gallery",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ArtistCatalog.internationalArtists, key = { it.slug }) { artist ->
                        ArtistRailCard(artist = artist, onClick = { onOpenArtist(artist.slug) })
                    }
                }
            }

            if (state.legendary.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Legendary musicians",
                        subtitle = "Careers that defined music",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.legendary, key = { it.id }) { wallpaper ->
                            WallpaperTile(
                                wallpaper = wallpaper,
                                onClick = { open(wallpaper) },
                                modifier = Modifier.width(148.dp),
                                isFavorite = state.favoriteIds.contains(wallpaper.id)
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Browse categories",
                    subtitle = "Twelve live collections",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
            items(ArtistCatalog.categories.chunked(2)) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onOpenCategory(category.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            item {
                SectionHeader(
                    title = "Recently added",
                    subtitle = "Newest uploads, refreshed every visit",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
            wallpaperGrid(
                wallpapers = state.recentlyAdded,
                favoriteIds = state.favoriteIds,
                onOpen = { open(it) },
                loading = state.isLoading
            )

            item { LicenceFooter(onOpenCredits) }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.wallpaperGrid(
    wallpapers: List<Wallpaper>,
    favoriteIds: Set<String>,
    onOpen: (Wallpaper) -> Unit,
    loading: Boolean
) {
    if (loading && wallpapers.isEmpty()) {
        items(2) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(2) {
                    ShimmerTile(
                        modifier = Modifier
                            .weight(1f)
                            .height(230.dp)
                    )
                }
            }
        }
        return
    }

    items(wallpapers.chunked(2)) { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            row.forEach { wallpaper ->
                WallpaperTile(
                    wallpaper = wallpaper,
                    onClick = { onOpen(wallpaper) },
                    modifier = Modifier.weight(1f),
                    isFavorite = favoriteIds.contains(wallpaper.id)
                )
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeHeader(onOpenCredits: () -> Unit, onOpenSearch: () -> Unit) {
    Column(modifier = Modifier.statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtistWallsWordmark()
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onOpenCredits)
                    .padding(10.dp)
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = "Credits and licensing",
                    tint = TextSecondary
                )
            }
        }
        Text(
            text = "Live photography from Wikimedia Commons — every image credited.",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
        )
        GlassSearchBar(
            hint = "Search artists, singers, composers",
            onClick = onOpenSearch,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun LoadingHeader() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        ShimmerTile(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}

@Composable
private fun LicenceFooter(onOpenCredits: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .fillMaxWidth()
            .glass(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "About the photography",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "ArtistWalls does not own or claim any artist photograph. Every wallpaper " +
                "is discovered live from Wikimedia Commons and shown only when its licence " +
                "allows free reuse: public domain, CC0, CC BY or CC BY-SA. Creator, licence " +
                "and the original file page travel with each image.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                .clickable(onClick = onOpenCredits)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SEE ALL CREDITS",
                style = MaterialTheme.typography.labelMedium,
                color = com.artistwalls.app.ui.theme.Gold
            )
        }
    }
}
