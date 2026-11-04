package com.artistwalls.app.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artistwalls.app.data.catalog.ArtistCatalog
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.data.model.WallpaperCategory
import com.artistwalls.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
    val category: WallpaperCategory? = null,
    val items: List<Wallpaper> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val nextOffset: Int = 0,
    val error: String? = null
)

class CategoryViewModel(
    private val categoryId: String,
    private val container: AppContainer
) : ViewModel() {

    private val repository = container.wallpaperRepository
    private val category = ArtistCatalog.categoryById(categoryId)

    private val _state = MutableStateFlow(CategoryUiState(category = category))
    val state: StateFlow<CategoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.libraryDao.favoriteIds().collect { ids ->
                _state.update { it.copy(favoriteIds = ids.toSet()) }
            }
        }
        load()
    }

    fun load(refresh: Boolean = false) {
        val target = category ?: run {
            _state.update { it.copy(isLoading = false, error = "Unknown collection") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.categoryFeed(target, offset = 0, refresh = refresh) }
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = page.items,
                            hasMore = page.hasMore,
                            nextOffset = page.nextOffset,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun loadMore() {
        val current = _state.value
        val target = category ?: return
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return

        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            runCatching { repository.categoryFeed(target, offset = current.nextOffset) }
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            items = (it.items + page.items).distinctBy { item -> item.id },
                            hasMore = page.hasMore,
                            nextOffset = page.nextOffset
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoadingMore = false, error = error.message) }
                }
        }
    }
}
