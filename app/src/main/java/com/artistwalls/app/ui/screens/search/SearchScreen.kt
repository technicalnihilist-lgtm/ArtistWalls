package com.artistwalls.app.ui.screens.search

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
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
import com.artistwalls.app.ui.components.ArtistRailCard
import com.artistwalls.app.ui.components.CategoryCard
import com.artistwalls.app.ui.components.EmptyState
import com.artistwalls.app.ui.components.ErrorState
import com.artistwalls.app.ui.components.FilterChip
import com.artistwalls.app.ui.components.SearchInput
import com.artistwalls.app.ui.components.SectionHeader
import com.artistwalls.app.ui.components.WallpaperTile
import com.artistwalls.app.ui.components.glass
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.TextPrimary
import com.artistwalls.app.ui.theme.TextSecondary
import com.artistwalls.app.ui.theme.TextTertiary

@Composable
fun SearchScreen(
    onOpenArtist: (String) -> Unit,
    onOpenWallpaper: (String) -> Unit,
    onOpenCategory: (String) -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: SearchViewModel = viewModel(
        factory = viewModelFactory { initializer { SearchViewModel(container) } }
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
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(10.dp))
            SearchInput(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                onSearch = { viewModel.submit() }
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(SearchFilter.values().toList()) { filter ->
                FilterChip(
                    label = filter.label,
                    selected = state.filter == filter,
                    onClick = { viewModel.setFilter(filter) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 130.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---------------------------------------------------------- suggestions
            if (state.query.trim().length >= 2 && !state.hasSearched) {
                if (state.artistMatches.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(title = "Artists", modifier = Modifier.padding(top = 8.dp))
                    }
                    items(state.artistMatches, key = { "artist-" + it.slug }, span = { GridItemSpan(1) }) { artist ->
                        Box(modifier = Modifier.glass(RoundedCornerShape(16.dp)).clickable { onOpenArtist(artist.slug) }) {
                            ArtistRailCard(
                                artist = artist,
                                onClick = { onOpenArtist(artist.slug) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                            )
                        }
                    }
                }
                if (state.categoryMatches.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(title = "Collections", modifier = Modifier.padding(top = 8.dp))
                    }
                    items(state.categoryMatches, key = { "cat-" + it.id }, span = { GridItemSpan(maxLineSpan) }) { category ->
                        CategoryCard(category = category, onClick = { onOpenCategory(category.id) })
                    }
                }
                if (state.suggestions.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(title = "Suggestions", modifier = Modifier.padding(top = 8.dp))
                    }
                    items(state.suggestions, key = { "sug-" + it }, span = { GridItemSpan(maxLineSpan) }) { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glass(RoundedCornerShape(14.dp))
                                .clickable { viewModel.submit(suggestion) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.width(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(suggestion, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }
                if (state.recentSearches.isNotEmpty() && state.query.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(
                            title = "Recent searches",
                            actionLabel = "Clear",
                            onAction = { viewModel.clearHistory() },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // ------------------------------------------------------- empty / idle
            if (!state.hasSearched && state.query.isBlank()) {
                if (state.recentSearches.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.recentSearches) { term ->
                                FilterChip(label = term, selected = false, onClick = { viewModel.submit(term) })
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = "Find any artist",
                        message = "Type a singer, composer or performer. Results are searched live " +
                            "across Wikimedia Commons and filtered to freely licensed photographs only.",
                        icon = Icons.Rounded.Search,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            // ------------------------------------------------------------ results
            if (state.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Gold, strokeWidth = 2.dp)
                    }
                }
            }

            state.error?.let { message ->
                if (state.results.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorState(message = message, onRetry = { viewModel.retry() })
                    }
                }
            }

            if (state.hasSearched && !state.isLoading && state.results.isEmpty() && state.error == null &&
                state.filter == SearchFilter.ALL
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = "No licensed photographs",
                        message = "Wikimedia Commons has no freely licensed photograph for " +
                            "\"${state.activeQuery}\". ArtistWalls will not substitute an " +
                            "unauthorised image, so this stays empty until one is published.",
                        icon = Icons.Rounded.Search
                    )
                }
            }

            if (state.filter == SearchFilter.ALL) {
                items(state.results, key = { it.id }) { wallpaper ->
                    WallpaperTile(wallpaper = wallpaper, onClick = { open(wallpaper) })
                }
            }

            if (state.isLoadingMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Gold, strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}
