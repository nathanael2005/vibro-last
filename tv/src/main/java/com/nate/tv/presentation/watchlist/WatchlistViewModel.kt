package com.nate.tv.presentation.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.core.domain.model.Favorite
import com.nate.core.domain.model.MediaItem
import com.nate.core.domain.model.WatchHistory
import com.nate.core.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WatchlistUiState {
    object Loading : WatchlistUiState
    data class Success(
        val favorites: List<Favorite>,
        val history: List<WatchHistory>
    ) : WatchlistUiState
}

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchlistUiState>(WatchlistUiState.Loading)
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getFavorites().collectLatest { favorites ->
                val history = mutableListOf<WatchHistory>()
                _uiState.value = WatchlistUiState.Success(
                    favorites = favorites,
                    history = history
                )
            }
        }
    }

    fun removeFavorite(tmdbId: String) {
        viewModelScope.launch {
            repository.deleteFavorite(tmdbId)
        }
    }
}
