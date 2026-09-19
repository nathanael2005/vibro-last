package com.nate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.app.data.api.ApiResult
import com.nate.app.data.local.PreferencesManager
import com.nate.app.data.model.Episode
import com.nate.app.data.model.Media
import com.nate.app.data.model.Season
import com.nate.app.data.repository.MediaRepository
import com.nate.app.data.repository.regularSeasons
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val media: Media? = null,
    val seasons: List<Season> = emptyList(),
    val selectedSeason: Season? = null,
    val episodes: List<Episode> = emptyList(),
    val isEpisodeLoading: Boolean = false,
    val episodeCount: Int = 0,
)

class DetailsViewModel(
    private val repository: MediaRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    fun load(initial: Media) {
        viewModelScope.launch {
            _uiState.update { DetailsUiState(isLoading = true, media = initial) }
            if (initial.mediaType == "tv") {
                when (val result = repository.getTvShowDetails(initial.id)) {
                    is ApiResult.Success -> {
                        val seasons = result.data.seasons.orEmpty().regularSeasons()
                        val firstSeason = seasons.firstOrNull()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                media = initial.copy(
                                    overview = result.data.overview?.takeIf { o -> o.isNotBlank() }
                                        ?: initial.overview,
                                    name = result.data.name,
                                    mediaType = "tv",
                                    genres = result.data.genres,
                                    tagline = result.data.tagline,
                                    status = result.data.status,
                                    voteAverage = result.data.voteAverage ?: initial.voteAverage,
                                    backdropPath = result.data.backdropPath ?: initial.backdropPath,
                                    posterPath = result.data.posterPath ?: initial.posterPath,
                                ),
                                seasons = seasons,
                                selectedSeason = firstSeason,
                                episodeCount = firstSeason?.episodeCount ?: 0,
                            )
                        }
                        firstSeason?.let { loadEpisodes(initial.id, it.seasonNumber) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = result.message, media = initial)
                        }
                    }
                }
            } else {
                when (val result = repository.getMovieDetails(initial.id)) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, media = result.data, errorMessage = null)
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, media = initial, errorMessage = result.message)
                        }
                    }
                }
            }
        }
    }

    fun selectSeason(tvId: Int, season: Season) {
        _uiState.update {
            it.copy(
                selectedSeason = season,
                episodeCount = season.episodeCount,
                episodes = emptyList(),
            )
        }
        loadEpisodes(tvId, season.seasonNumber)
    }

    private fun loadEpisodes(tvId: Int, seasonNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEpisodeLoading = true) }
            when (val result = repository.getEpisodesForSeason(tvId, seasonNumber)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isEpisodeLoading = false,
                            episodes = result.data,
                            episodeCount = result.data.size,
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isEpisodeLoading = false,
                            episodes = emptyList(),
                            episodeCount = 0,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun toggleBookmark(media: Media, isBookmarked: Boolean) {
        viewModelScope.launch {
            if (isBookmarked) {
                preferencesManager.removeFromWatchlist(media.id, media.mediaType)
            } else {
                preferencesManager.addToWatchlist(media)
            }
        }
    }
}
