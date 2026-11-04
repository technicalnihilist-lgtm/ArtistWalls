package com.artistwalls.app.ui.screens.wallpaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artistwalls.app.data.local.toLibraryEntry
import com.artistwalls.app.data.local.toWallpaper
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.data.wallpaper.WallpaperSetter
import com.artistwalls.app.di.AppContainer
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WallpaperDetailUiState(
    val wallpaper: Wallpaper? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val progress: Int? = null,
    val isBusy: Boolean = false,
    val message: String? = null
)

class WallpaperDetailViewModel(
    private val wallpaperId: String,
    private val container: AppContainer
) : ViewModel() {

    private val repository = container.wallpaperRepository
    private val dao = container.libraryDao
    private val _state = MutableStateFlow(WallpaperDetailUiState())
    val state: StateFlow<WallpaperDetailUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            dao.favoriteIds().collect { ids ->
                _state.update { it.copy(isFavorite = ids.contains(wallpaperId)) }
            }
        }
        viewModelScope.launch {
            dao.downloadedIds().collect { ids ->
                _state.update { it.copy(isDownloaded = ids.contains(wallpaperId)) }
            }
        }
        viewModelScope.launch {
            container.downloadManager.progress.collect { progress ->
                _state.update { it.copy(progress = progress[wallpaperId]?.takeIf { value -> value in 0..99 }) }
            }
        }
    }

    fun load() {
        // 1. Instant: the tile the user just tapped.
        container.selection.find(wallpaperId)?.let { cached ->
            _state.update { it.copy(wallpaper = cached, isLoading = false, error = null) }
            recordView(cached)
            return
        }

        viewModelScope.launch {
            // 2. Offline: a favourite or download we already stored.
            val stored = runCatching { dao.find(wallpaperId) }.getOrNull()
            if (stored != null) {
                val wallpaper = stored.toWallpaper()
                _state.update { it.copy(wallpaper = wallpaper, isLoading = false) }
                recordView(wallpaper)
                return@launch
            }

            // 3. Live: refetch full metadata from Commons (deep links, fresh installs).
            runCatching { repository.wallpaperByTitle(wallpaperId) }
                .onSuccess { wallpaper ->
                    if (wallpaper == null) {
                        _state.update {
                            it.copy(isLoading = false, error = "This photograph is no longer available on Commons.")
                        }
                    } else {
                        _state.update { it.copy(wallpaper = wallpaper, isLoading = false, error = null) }
                        recordView(wallpaper)
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun toggleFavorite() {
        val wallpaper = _state.value.wallpaper ?: return
        val makeFavorite = !_state.value.isFavorite
        viewModelScope.launch {
            if (makeFavorite) {
                dao.upsert(wallpaper.toLibraryEntry().copy(isFavorite = true, savedAt = System.currentTimeMillis()))
                _state.update { it.copy(message = "Added to favourites") }
            } else {
                dao.setFavorite(wallpaper.id, false)
                _state.update { it.copy(message = "Removed from favourites") }
            }
        }
    }

    fun download() {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.isBusy) return
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            // Store the credit row first so attribution survives even if the transfer fails.
            dao.upsert(wallpaper.toLibraryEntry())
            val result = container.downloadManager.download(wallpaper)
            _state.update {
                it.copy(
                    isBusy = false,
                    message = result.fold(
                        onSuccess = { file -> "Saved offline · ${file.length() / 1024} KB" },
                        onFailure = { error -> "Download failed: ${error.message}" }
                    )
                )
            }
        }
    }

    fun setAs(target: WallpaperSetter.Target) {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.isBusy) return
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            dao.upsert(wallpaper.toLibraryEntry())
            val file = container.downloadManager.download(wallpaper).getOrNull()
            if (file == null) {
                _state.update { it.copy(isBusy = false, message = "Could not download the original image") }
                return@launch
            }
            val result = container.wallpaperSetter.setWallpaper(file, target)
            val label = when (target) {
                WallpaperSetter.Target.HOME -> "home screen"
                WallpaperSetter.Target.LOCK -> "lock screen"
                WallpaperSetter.Target.BOTH -> "home and lock screen"
            }
            _state.update {
                it.copy(
                    isBusy = false,
                    message = result.fold(
                        onSuccess = { "Wallpaper set on $label" },
                        onFailure = { error -> "Could not set wallpaper: ${error.message}" }
                    )
                )
            }
        }
    }

    fun share() {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.isBusy) return
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val file: File? = container.downloadManager.download(wallpaper).getOrNull()
            if (file == null) {
                _state.update { it.copy(isBusy = false, message = "Could not prepare the image for sharing") }
                return@launch
            }
            val result = container.shareHelper.shareWallpaper(wallpaper, file)
            _state.update {
                it.copy(
                    isBusy = false,
                    message = result.exceptionOrNull()?.let { error -> "Share failed: ${error.message}" }
                )
            }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    private fun recordView(wallpaper: Wallpaper) {
        viewModelScope.launch {
            val existing = runCatching { dao.find(wallpaper.id) }.getOrNull()
            val entry = existing ?: wallpaper.toLibraryEntry()
            dao.upsert(entry.copy(lastViewedAt = System.currentTimeMillis()))
        }
    }
}
