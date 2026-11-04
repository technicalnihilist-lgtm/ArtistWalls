package com.artistwalls.app.ui.screens.library

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.artistwalls.app.ui.components.FilterChip
import com.artistwalls.app.ui.components.WallpaperTile
import com.artistwalls.app.ui.components.glass
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary
import com.artistwalls.app.ui.theme.TextTertiary

@Composable
fun LibraryScreen(
    onOpenWallpaper: (String) -> Unit,
    onOpenSearch: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: LibraryViewModel = viewModel(
        factory = viewModelFactory { initializer { LibraryViewModel(container) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    fun open(wallpaper: Wallpaper) {
        container.selection.select(wallpaper)
        onOpenWallpaper(wallpaper.id)
    }

    val visible = when (state.tab) {
        LibraryTab.FAVORITES -> state.favorites
        LibraryTab.DOWNLOADS -> state.downloads
        LibraryTab.RECENT -> state.recent
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Your favourites, offline copies and recently viewed photographs",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                items(LibraryTab.values().toList()) { tab ->
                    FilterChip(
                        label = tab.label,
                        selected = state.tab == tab,
                        onClick = { viewModel.selectTab(tab) }
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 130.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (visible.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyLibraryState(tab = state.tab, onOpenSearch = onOpenSearch)
                    }
                }

                items(visible, key = { it.id }) { wallpaper ->
                    Column {
                        WallpaperTile(
                            wallpaper = wallpaper,
                            onClick = { open(wallpaper) },
                            isFavorite = state.tab == LibraryTab.FAVORITES ||
                                state.favorites.any { it.id == wallpaper.id }
                        )
                        if (state.tab == LibraryTab.DOWNLOADS) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .glass(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.removeDownload(wallpaper) }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Remove offline copy",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        )
    }
}

@Composable
private fun EmptyLibraryState(tab: LibraryTab, onOpenSearch: () -> Unit) {
    val (title, message, icon) = when (tab) {
        LibraryTab.FAVORITES -> Triple(
            "No favourites yet",
            "Tap the heart on any wallpaper and it is kept here — with its photographer " +
                "and licence — even when you are offline.",
            Icons.Rounded.Favorite
        )
        LibraryTab.DOWNLOADS -> Triple(
            "Nothing saved offline",
            "Download a wallpaper and the original file is stored on this device, ready to " +
                "apply with no network.",
            Icons.Rounded.Download
        )
        LibraryTab.RECENT -> Triple(
            "No history yet",
            "Photographs you open will appear here so you can find them again quickly.",
            Icons.Rounded.Search
        )
    }
    EmptyState(
        title = title,
        message = message,
        icon = icon,
        actionLabel = "Browse artists",
        onAction = onOpenSearch,
        modifier = Modifier.padding(top = 20.dp)
    )
}
