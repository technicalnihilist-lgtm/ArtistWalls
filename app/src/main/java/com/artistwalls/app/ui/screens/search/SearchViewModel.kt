package com.artistwalls.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artistwalls.app.data.catalog.ArtistCatalog
import com.artistwalls.app.data.model.Artist
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.data.model.WallpaperCategory
import com.artistwalls.app.di.AppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SearchFilter(val label: String) {
    ALL("All images"),
    ARTISTS("Artists"),
    COLLECTIONS("Collections")
}

data class SearchUiState(
    val query: String = "",
    val activeQuery: String = "",
    val suggestions: List<String> = emptyList(),
    val artistMatches: List<Artist> = emptyList(),
    val categoryMatches: List<WallpaperCategory> = emptyList(),
    val filter: SearchFilter = SearchFilter.ALL,
    val results: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val nextOffset: Int = 0,
    val error: String? = null,
    val recentSearches: List<String> = emptyList(),
    val hasSearched: Boolean = false
)

class SearchViewModel(private val container: AppContainer) : ViewModel() {

    private val repository = container.wallpaperRepository
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var suggestionJob: Job? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            container.preferences.recentSearches.collect { history ->
                _state.update { it.copy(recentSearches = history) }
            }
        }
    }

    fun onQueryChange(value: String) {
        _state.update {
            it.copy(
                query = value,
                artistMatches = ArtistCatalog.suggest(value, 6),
                categoryMatches = matchCategories(value)
            )
        }
        suggestionJob?.cancel()
        if (value.trim().length < 2) {
            _state.update { it.copy(suggestions = emptyList()) }
            return
        }
        suggestionJob = viewModelScope.launch {
            delay(280)
            val live = runCatching { container.suggestionService.suggest(value) }.getOrDefault(emptyList())
            _state.update { it.copy(suggestions = live) }
        }
    }

    fun submit(rawQuery: String = _state.value.query) {
        val query = rawQuery.trim()
        if (query.isEmpty()) return
        searchJob?.cancel()
        _state.update {
            it.copy(
                query = query,
                activeQuery = query,
                isLoading = true,
                error = null,
                results = emptyList(),
                suggestions = emptyList(),
                hasSearched = true,
                filter = SearchFilter.ALL
            )
        }
        viewModelScope.launch { container.preferences.addSearch(query) }

        searchJob = viewModelScope.launch {
            val outcome = runCatching { repository.searchWallpapers(query, offset = 0) }
            outcome.fold(
                onSuccess = { page ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            results = page.items,
                            hasMore = page.hasMore,
                            nextOffset = page.nextOffset,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message, results = emptyList())
                    }
                }
            )
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return
        val query = current.activeQuery
        if (query.isEmpty()) return

        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            val outcome = runCatching {
                repository.searchWallpapers(query, offset = current.nextOffset)
            }
            outcome.fold(
                onSuccess = { page ->
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            results = (it.results + page.items).distinctBy { item -> item.id },
                            hasMore = page.hasMore,
                            nextOffset = page.nextOffset
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoadingMore = false, error = error.message) }
                }
            )
        }
    }

    fun setFilter(filter: SearchFilter) = _state.update { it.copy(filter = filter) }

    fun clearQuery() = _state.update {
        it.copy(query = "", activeQuery = "", results = emptyList(), suggestions = emptyList(), hasSearched = false)
    }

    fun clearHistory() {
        viewModelScope.launch { container.preferences.clearSearches() }
    }

    fun retry() {
        if (_state.value.activeQuery.isNotEmpty()) submit(_state.value.activeQuery)
    }

    private fun matchCategories(query: String): List<WallpaperCategory> {
        val q = query.trim().lowercase()
        if (q.length < 2) return emptyList()
        return ArtistCatalog.categories.filter { it.name.lowercase().contains(q) }.take(4)
    }
}
