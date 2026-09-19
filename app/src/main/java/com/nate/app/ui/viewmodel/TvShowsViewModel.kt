package com.nate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.app.data.api.ApiResult
import com.nate.app.data.repository.MediaRepository
import com.nate.app.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TvShowsViewModel(
    private val repository: MediaRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TvShowsUiState())
    val uiState: StateFlow<TvShowsUiState> = _uiState.asStateFlow()

    fun load(apiKey: String?) {
        if (apiKey.isNullOrBlank()) {
            _uiState.update {
                it.copy(catalog = CatalogUiState(errorMessage = "Add your TMDB API key in Settings."))
            }
            return
        }
        if (!networkMonitor.isOnline()) {
            _uiState.update {
                it.copy(
                    catalog = CatalogUiState(
                        errorMessage = networkMonitor.offlineMessage(),
                        isOffline = true,
                    ),
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(catalog = CatalogUiState(isLoading = true)) }
            when (val result = repository.getTvCatalog()) {
                is ApiResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            catalog = CatalogUiState(isLoading = false),
                            trendingTvShows = data.trendingTvShows,
                            popularTvShows = data.popularTvShows,
                            topRatedTvShows = data.topRatedTvShows,
                            airingTodayTvShows = data.airingTodayTvShows,
                            onTheAirTvShows = data.onTheAirTvShows,
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            catalog = CatalogUiState(
                                isLoading = false,
                                errorMessage = result.message,
                            ),
                        )
                    }
                }
            }
        }
    }
}
