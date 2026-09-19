package com.nate.tv.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.core.domain.model.Category
import com.nate.core.domain.model.MediaItem
import com.nate.core.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Success(
        val categories: List<Category>,
        val trending: List<MediaItem>
    ) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Fetch categories and trending lists concurrently
                val categoriesDeferred = async { repository.getCategories() }
                val trendingDeferred = async { repository.getTrending(1) }

                val categories = categoriesDeferred.await()
                val trending = trendingDeferred.await()

                _uiState.value = HomeUiState.Success(
                    categories = categories,
                    trending = trending
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    message = e.localizedMessage ?: "Unknown network failure"
                )
            }
        }
    }
}
