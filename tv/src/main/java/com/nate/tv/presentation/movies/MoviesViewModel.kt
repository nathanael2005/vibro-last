package com.nate.tv.presentation.movies

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

sealed interface MoviesUiState {
    object Loading : MoviesUiState
    data class Error(val message: String) : MoviesUiState
    data class Success(
        val categories: List<Category>,
        val featured: MediaItem?
    ) : MoviesUiState
}

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            try {
                val categories = repository.getMoviesCategories()
                val featured = categories.firstOrNull()?.items?.firstOrNull()
                _uiState.value = MoviesUiState.Success(
                    categories = categories,
                    featured = featured
                )
            } catch (e: Exception) {
                _uiState.value = MoviesUiState.Error(
                    message = e.localizedMessage ?: "Failed to load movies"
                )
            }
        }
    }
}
