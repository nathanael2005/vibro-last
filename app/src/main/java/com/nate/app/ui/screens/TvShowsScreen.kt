package com.nate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.MaterialTheme
import com.nate.app.data.local.PreferencesManager
import com.nate.app.data.model.Media
import com.nate.app.ui.theme.DarkBackground
import com.nate.app.ui.viewmodel.NateViewModelFactory
import com.nate.app.ui.viewmodel.TvShowsViewModel

@Composable
fun TvShowsScreen(
    viewModelFactory: NateViewModelFactory,
    preferencesManager: PreferencesManager,
    onMediaClick: (Media) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: TvShowsViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val apiKey by preferencesManager.apiKeyFlow.collectAsState(initial = "")

    LaunchedEffect(apiKey) {
        viewModel.load(apiKey)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        when {
            apiKey.isBlank() -> ApiKeyPrompt(title = "TV Series", onNavigateToSettings = onNavigateToSettings)
            uiState.catalog.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            uiState.catalog.errorMessage != null -> {
                ErrorPanel(
                    message = uiState.catalog.errorMessage!!,
                    onRetry = { viewModel.load(apiKey) },
                    onSettings = onNavigateToSettings,
                )
            }
            else -> {
                TvLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    item {
                        CategoryRow("Trending Series Today", uiState.trendingTvShows, onMediaClick, onMediaFocused = {})
                    }
                    item {
                        CategoryRow("Popular Series", uiState.popularTvShows, onMediaClick, onMediaFocused = {})
                    }
                    item {
                        CategoryRow("Highly Rated Masterpieces", uiState.topRatedTvShows, onMediaClick, onMediaFocused = {})
                    }
                    item {
                        CategoryRow("Airing Today", uiState.airingTodayTvShows, onMediaClick, onMediaFocused = {})
                    }
                    item {
                        CategoryRow("On The Air Now", uiState.onTheAirTvShows, onMediaClick, onMediaFocused = {})
                    }
                }
            }
        }
    }
}
