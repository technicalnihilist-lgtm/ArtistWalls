package com.artistwalls.app.ui.screens.credits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artistwalls.app.data.local.toWallpaper
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreditsUiState(
    val credits: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = true
)

class CreditsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(CreditsUiState())
    val state: StateFlow<CreditsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val rows = runCatching { container.libraryDao.creditRows() }.getOrDefault(emptyList())
            _state.value = CreditsUiState(
                credits = rows.map { it.toWallpaper() }.distinctBy { it.id },
                isLoading = false
            )
        }
    }
}
