package com.nate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.app.data.api.ApiResult
import com.nate.app.data.repository.MediaRepository
import com.nate.app.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: MediaRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var requestToken = 0

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, isGenreBrowse = false) }
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            searchJob?.cancel()
            _uiState.update {
                it.copy(isLoading = false, results = emptyList(), errorMessage = null)
            }
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(results = emptyList(), errorMessage = null) }
            delay(500)
            runSearch(trimmed, isGenre = false)
        }
    }

    fun searchGenre(genreLabel: String) {
        _uiState.update { it.copy(query = genreLabel, isGenreBrowse = true) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isOffline = false) }
            val genreId = com.nate.app.data.api.TmdbGenres.movieGenreId(genreLabel)
            val result = if (genreId != null) {
                repository.discoverByGenreId(genreId)
            } else {
                repository.discoverByGenre(genreLabel)
            }
            _uiState.update {
                when (result) {
                    is ApiResult.Success -> it.copy(
                        isLoading = false,
                        results = result.data,
                        errorMessage = if (result.data.isEmpty()) "No results found for '$genreLabel'." else null
                    )
                    is ApiResult.Error -> it.copy(
                        isLoading = false,
                        results = emptyList(),
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun searchSuggestion(keyword: String) {
        onQueryChange(keyword)
    }

    private suspend fun runSearch(term: String, isGenre: Boolean) {
        if (!networkMonitor.isOnline()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = networkMonitor.offlineMessage(),
                    isOffline = true,
                    results = emptyList(),
                )
            }
            return
        }
        val token = ++requestToken
        _uiState.update {
            it.copy(isLoading = true, errorMessage = null, isOffline = false)
        }
        val result = if (isGenre) {
            repository.discoverByGenre(term)
        } else {
            repository.search(term)
        }
        if (token != requestToken) return
        when (result) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = result.data,
                        errorMessage = if (result.data.isEmpty()) "No results found for '$term'." else null,
                    )
                }
            }
            is ApiResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = emptyList(),
                        errorMessage = result.message,
                    )
                }
            }
        }
    }
}
