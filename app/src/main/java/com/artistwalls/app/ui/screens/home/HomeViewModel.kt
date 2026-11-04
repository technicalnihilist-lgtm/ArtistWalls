package com.artistwalls.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artistwalls.app.data.catalog.ArtistCatalog
import com.artistwalls.app.data.model.Artist
import com.artistwalls.app.data.model.PagedWallpapers
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.di.AppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val daily: Wallpaper? = null,
    val featured: List<Artist> = ArtistCatalog.featured,
    val trending: List<Wallpaper> = emptyList(),
    val legendary: List<Wallpaper> = emptyList(),
    val recentlyAdded: List<Wallpaper> = emptyList(),
    val favoriteIds: Set<String> = emptySet()
)

/**
 * Home pulls six independent live feeds in parallel. A failure in one section never
 * blanks the screen — whatever loaded stays visible.
 */
class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val repository = container.wallpaperRepository
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        observeFavourites()
        load()
    }

    private fun observeFavourites() {
        viewModelScope.launch {
            container.libraryDao.favoriteIds().collect { ids ->
                _state.update { it.copy(favoriteIds = ids.toSet()) }
            }
        }
    }

    fun load(refresh: Boolean = false) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = it.daily == null && it.trending.isEmpty(),
                    isRefreshing = refresh,
                    error = null
                )
            }

            var failure: String? = null

            coroutineScope {
                val dailyJob = async { runCatching { repository.dailyWallpaper() } }
                val trendingJob = async { runCatching { repository.trending(limit = 12) } }
                val recentJob = async {
                    runCatching { repository.recentlyAdded(limit = 12, refresh = refresh) }
                }
                val legendaryJob = async {
                    val category = ArtistCatalog.categoryById("legendary-artists")
                    if (category == null) Result.success(PagedWallpapers.Empty)
                    else runCatching { repository.categoryFeed(category, limit = 12) }
                }

                val daily = dailyJob.await()
                val trending = trendingJob.await()
                val recent = recentJob.await()
                val legendary = legendaryJob.await()

                if (trending.isFailure) {
                    failure = trending.exceptionOrNull()?.message
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        daily = daily.getOrNull() ?: it.daily,
                        trending = trending.getOrNull()?.items ?: it.trending,
                        recentlyAdded = recent.getOrNull()?.items ?: it.recentlyAdded,
                        legendary = legendary.getOrNull()?.items ?: it.legendary,
                        error = if (it.trending.isEmpty() && it.daily == null) failure else null
                    )
                }
            }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }
}
