package com.nate.tv.presentation.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.core.domain.model.Category
import com.nate.core.domain.model.MediaItem
import com.nate.core.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TvShowsUiState {
    object Loading : TvShowsUiState
    data class Error(val message: String) : TvShowsUiState
    data class Success(
        val categories: List<Category>,
        val featured: MediaItem?
    ) : TvShowsUiState
}

@HiltViewModel
class TvShowsViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TvShowsUiState>(TvShowsUiState.Loading)
    val uiState: StateFlow<TvShowsUiState> = _uiState.asStateFlow()

    init {
        loadTvShows()
    }

    fun loadTvShows() {
        viewModelScope.launch {
            _uiState.value = TvShowsUiState.Loading
            try {
                val categories = repository.getTvCategories()
                val featured = categories.firstOrNull()?.items?.firstOrNull()
                _uiState.value = TvShowsUiState.Success(
                    categories = categories,
                    featured = featured
                )
            } catch (e: Exception) {
                _uiState.value = TvShowsUiState.Error(
                    message = e.localizedMessage ?: "Failed to load TV shows"
                )
            }
        }
    }
}
