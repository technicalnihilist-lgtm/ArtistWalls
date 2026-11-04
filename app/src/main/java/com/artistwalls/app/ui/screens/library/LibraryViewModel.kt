package com.artistwalls.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artistwalls.app.data.local.toWallpaper
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LibraryTab(val label: String) {
    FAVORITES("Favourites"),
    DOWNLOADS("Offline"),
    RECENT("Recently viewed")
}

data class LibraryUiState(
    val tab: LibraryTab = LibraryTab.FAVORITES,
    val favorites: List<Wallpaper> = emptyList(),
    val downloads: List<Wallpaper> = emptyList(),
    val recent: List<Wallpaper> = emptyList(),
    val message: String? = null
)

class LibraryViewModel(private val container: AppContainer) : ViewModel() {

    private val dao = container.libraryDao
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(dao.favorites(), dao.downloads(), dao.recentlyViewed()) { favorites, downloads, recent ->
                Triple(
                    favorites.map { it.toWallpaper() },
                    downloads.map { it.toWallpaper() },
                    recent.map { it.toWallpaper() }
                )
            }.collect { (favorites, downloads, recent) ->
                _state.update {
                    it.copy(favorites = favorites, downloads = downloads, recent = recent)
                }
            }
        }
    }

    fun selectTab(tab: LibraryTab) = _state.update { it.copy(tab = tab) }

    fun removeDownload(wallpaper: Wallpaper) {
        viewModelScope.launch {
            container.downloadManager.deleteDownload(wallpaper)
            _state.update { it.copy(message = "Offline copy removed") }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
}
