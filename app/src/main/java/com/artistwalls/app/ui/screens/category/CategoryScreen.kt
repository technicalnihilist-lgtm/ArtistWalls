package com.artistwalls.app.ui.screens.category

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.di.LocalAppContainer
import com.artistwalls.app.ui.components.EmptyState
import com.artistwalls.app.ui.components.ErrorState
import com.artistwalls.app.ui.components.ShimmerTile
import com.artistwalls.app.ui.components.WallpaperTile
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary

@Composable
fun CategoryScreen(
    categoryId: String,
    onBack: () -> Unit,
    onOpenWallpaper: (String) -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: CategoryViewModel = viewModel(
        key = categoryId,
        factory = viewModelFactory { initializer { CategoryViewModel(categoryId, container) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

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

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp)
                    .clickableNoRipple(onBack)
            )
            Spacer(Modifier.size(6.dp))
            Column {
                Text(
                    text = state.category?.name ?: "Collection",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = state.category?.blurb.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.isLoading) {
                items(6) {
                    ShimmerTile(modifier = Modifier.height(230.dp))
                }
            }

            state.error?.let { message ->
                if (state.items.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorState(message = message, onRetry = { viewModel.load() })
                    }
                }
            }

            if (!state.isLoading && state.items.isEmpty() && state.error == null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = "Nothing licensed yet",
                        message = "This collection currently has no freely licensed photograph on " +
                            "Wikimedia Commons. It will fill in on its own as new images are " +
                            "published — ArtistWalls never shows unlicensed press images.",
                        icon = Icons.Rounded.Search
                    )
                }
            }

            items(state.items, key = { it.id }) { wallpaper ->
                WallpaperTile(
                    wallpaper = wallpaper,
                    onClick = { open(wallpaper) },
                    isFavorite = state.favoriteIds.contains(wallpaper.id)
                )
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
}

internal fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
