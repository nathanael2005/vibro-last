package com.nate.tv.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nate.core.domain.model.Favorite
import com.nate.core.domain.model.MediaDetail
import com.nate.core.domain.model.SeasonDetail
import com.nate.core.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MediaDetailUiState {
    object Loading : MediaDetailUiState
    data class Error(val message: String) : MediaDetailUiState
    data class Success(
        val detail: MediaDetail,
        val selectedSeason: Int,
        val currentSeasonDetail: SeasonDetail?,
        val isFavorite: Boolean
    ) : MediaDetailUiState
}

@HiltViewModel
class MediaDetailViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Loading)
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    private var currentTmdbId: String = ""
    private var currentMediaType: String = ""
    private var currentDetail: MediaDetail? = null
    private var currentSeason: Int = 1
    private var isFavoriteState: Boolean = false

    fun loadDetail(tmdbId: String, mediaType: String) {
        currentTmdbId = tmdbId
        currentMediaType = mediaType
        viewModelScope.launch {
            _uiState.value = MediaDetailUiState.Loading
            try {
                val detail = repository.getFullMediaDetail(mediaType, tmdbId)
                currentDetail = detail
                
                var seasonDetail: SeasonDetail? = null
                if (mediaType == "tv") {
                    val validSeason = detail.seasons.firstOrNull { it.seasonNumber > 0 }?.seasonNumber ?: 1
                    currentSeason = validSeason
                    seasonDetail = runCatching { repository.getTvSeasonDetail(tmdbId, currentSeason) }.getOrNull()
                }

                _uiState.value = MediaDetailUiState.Success(
                    detail = detail,
                    selectedSeason = currentSeason,
                    currentSeasonDetail = seasonDetail,
                    isFavorite = isFavoriteState
                )

                // Observe favorite state
                launch {
                    repository.isFavorite(tmdbId).collectLatest { isFav ->
                        isFavoriteState = isFav
                        val state = _uiState.value
                        if (state is MediaDetailUiState.Success) {
                            _uiState.value = state.copy(isFavorite = isFav)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MediaDetailUiState.Error(
                    message = e.localizedMessage ?: "Failed to load media details"
                )
            }
        }
    }

    fun selectSeason(seasonNumber: Int) {
        currentSeason = seasonNumber
        viewModelScope.launch {
            try {
                val seasonDetail = repository.getTvSeasonDetail(currentTmdbId, seasonNumber)
                val state = _uiState.value
                if (state is MediaDetailUiState.Success) {
                    _uiState.value = state.copy(
                        selectedSeason = seasonNumber,
                        currentSeasonDetail = seasonDetail
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun toggleFavorite() {
        val detail = currentDetail ?: return
        viewModelScope.launch {
            if (isFavoriteState) {
                repository.deleteFavorite(currentTmdbId)
            } else {
                repository.saveFavorite(
                    Favorite(
                        tmdbId = currentTmdbId,
                        title = detail.mediaItem.title,
                        posterPath = detail.mediaItem.posterPath,
                        mediaType = currentMediaType,
                        addedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
