package com.nate.tv.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.core.domain.model.MediaItem
import com.nate.core.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val query: String, val results: List<MediaItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // Debounce typing
            _uiState.value = SearchUiState.Loading
            try {
                val results = repository.searchMedia(newQuery, 1)
                _uiState.value = SearchUiState.Success(newQuery, results)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.localizedMessage ?: "Search failed")
            }
        }
    }

    fun appendCharacter(char: String) {
        onQueryChange(_query.value + char)
    }

    fun deleteLastCharacter() {
        if (_query.value.isNotEmpty()) {
            onQueryChange(_query.value.dropLast(1))
        }
    }

    fun clearQuery() {
        onQueryChange("")
    }
}
