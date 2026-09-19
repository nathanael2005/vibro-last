package com.nate.app.ui.viewmodel

import com.nate.app.data.model.Media

data class CatalogUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
)

data class HomeUiState(
    val catalog: CatalogUiState = CatalogUiState(),
    val trendingMovies: List<Media> = emptyList(),
    val trendingTvShows: List<Media> = emptyList(),
    val popularMovies: List<Media> = emptyList(),
    val popularTvShows: List<Media> = emptyList(),
    val featuredMedia: Media? = null,
)

data class MoviesUiState(
    val catalog: CatalogUiState = CatalogUiState(),
    val trendingMovies: List<Media> = emptyList(),
    val popularMovies: List<Media> = emptyList(),
    val topRatedMovies: List<Media> = emptyList(),
    val nowPlayingMovies: List<Media> = emptyList(),
    val upcomingMovies: List<Media> = emptyList(),
)

data class TvShowsUiState(
    val catalog: CatalogUiState = CatalogUiState(),
    val trendingTvShows: List<Media> = emptyList(),
    val popularTvShows: List<Media> = emptyList(),
    val topRatedTvShows: List<Media> = emptyList(),
    val airingTodayTvShows: List<Media> = emptyList(),
    val onTheAirTvShows: List<Media> = emptyList(),
)

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val results: List<Media> = emptyList(),
    val isGenreBrowse: Boolean = false,
)
