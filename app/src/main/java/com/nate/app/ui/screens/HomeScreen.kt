package com.nate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nate.app.data.local.PreferencesManager
import com.nate.app.data.model.Media
import com.nate.app.ui.components.ImmersiveBanner
import com.nate.app.ui.theme.DarkBackground
import com.nate.app.ui.theme.TextPrimary
import com.nate.app.ui.theme.TextSecondary
import com.nate.app.ui.viewmodel.HomeViewModel
import com.nate.app.ui.viewmodel.NateViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModelFactory: NateViewModelFactory,
    preferencesManager: PreferencesManager,
    onMediaClick: (Media) -> Unit,
    onWatchClick: (Media) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val apiKey by preferencesManager.apiKeyFlow.collectAsState(initial = "")
    val scope = rememberCoroutineScope()

    LaunchedEffect(apiKey) {
        viewModel.load(apiKey)
    }

    val featured = uiState.featuredMedia
    val featuredBookmarked by preferencesManager
        .isBookmarkedFlow(featured?.id ?: -1, featured?.mediaType ?: "movie")
        .collectAsState(initial = false)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        when {
            apiKey.isBlank() -> {
                ApiKeyPrompt(
                    title = "Welcome to Nate",
                    onNavigateToSettings = onNavigateToSettings,
                )
            }
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
                    if (featured != null) {
                        item {
                            ImmersiveBanner(
                                media = featured,
                                isBookmarked = featuredBookmarked,
                                onWatchClick = onWatchClick,
                                onBookmarkClick = { media ->
                                    scope.launch {
                                        if (featuredBookmarked) {
                                            preferencesManager.removeFromWatchlist(
                                                media.id,
                                                media.mediaType,
                                            )
                                        } else {
                                            preferencesManager.addToWatchlist(media)
                                        }
                                    }
                                },
                                onDetailsClick = { onMediaClick(featured) },
                            )
                        }
                    }
                    item {
                        CategoryRow(
                            title = "Trending Movies",
                            items = uiState.trendingMovies,
                            onMediaClick = onMediaClick,
                            onMediaFocused = { },
                        )
                    }
                    item {
                        CategoryRow(
                            title = "Trending TV Shows",
                            items = uiState.trendingTvShows,
                            onMediaClick = onMediaClick,
                            onMediaFocused = { },
                        )
                    }
                    item {
                        CategoryRow(
                            title = "Highly Rated Movies",
                            items = uiState.popularMovies,
                            onMediaClick = onMediaClick,
                            onMediaFocused = { },
                        )
                    }
                    item {
                        CategoryRow(
                            title = "Highly Rated Series",
                            items = uiState.popularTvShows,
                            onMediaClick = onMediaClick,
                            onMediaFocused = { },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApiKeyPrompt(
    title: String,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Configure your free TMDB API key in Settings to browse movies and TV shows.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigateToSettings,
            colors = ButtonDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                focusedContentColor = TextPrimary,
            ),
        ) {
            Text("Go to Settings", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ErrorPanel(
    message: String,
    onRetry: () -> Unit,
    onSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                focusedContentColor = TextPrimary,
            ),
        ) {
            Text("Retry", fontWeight = FontWeight.Bold)
        }
        if (onSettings != null && message.contains("API key", ignoreCase = true)) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onSettings) {
                Text("Open Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CategoryRow(
    title: String,
    items: List<com.nate.app.data.model.Media>,
    onMediaClick: (com.nate.app.data.model.Media) -> Unit,
    onMediaFocused: (com.nate.app.data.model.Media) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp, horizontal = 24.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        androidx.tv.foundation.lazy.list.TvLazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            items(items) { item ->
                com.nate.app.ui.components.MovieCard(
                    media = item,
                    onClick = { onMediaClick(item) },
                    onFocused = onMediaFocused,
                )
            }
        }
    }
}
