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
import com.nate.app.ui.viewmodel.MoviesViewModel
import com.nate.app.ui.viewmodel.NateViewModelFactory

@Composable
fun MoviesScreen(
    viewModelFactory: NateViewModelFactory,
    preferencesManager: PreferencesManager,
    onMediaClick: (Media) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MoviesViewModel = viewModel(factory = viewModelFactory)
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
            apiKey.isBlank() -> ApiKeyPrompt(title = "Movies", onNavigateToSettings = onNavigateToSettings)
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
                        CategoryRow("Trending Movies Today", uiState.trendingMovies, onMediaClick, onMediaFocused = {})
                    }
                    item {
                        CategoryRow("Popular Choices", uiState.popularMovies, onMediaClick, onMediaFocused = {})
                    }
                    item {
                        CategoryRow("Top Rated Movies", uiState.topRatedMovies, onMediaClick, onMediaFocused = {})
                    }
                    item {
                        CategoryRow("Now Playing in Theatres", uiState.nowPlayingMovies, onMediaClick, onMediaFocused = {})
                    }
                    item {
                        CategoryRow("Upcoming Blockbusters", uiState.upcomingMovies, onMediaClick, onMediaFocused = {})
                    }
                }
            }
        }
    }
}
