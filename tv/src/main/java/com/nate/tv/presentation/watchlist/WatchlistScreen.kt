package com.nate.tv.presentation.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.nate.core.common.theme.ThemeTokens
import com.nate.core.domain.model.MediaItem
import com.nate.tv.ui.components.MovieCard
import com.nate.tv.ui.components.TvCircularProgressIndicator

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onMediaClick: (MediaItem) -> Unit,
    drawerFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeTokens.BackgroundBlack)
            .padding(
                start = ThemeTokens.TvSafeMarginHorizontal,
                end = ThemeTokens.TvSafeMarginHorizontal,
                top = ThemeTokens.TvSafeMarginVertical,
                bottom = ThemeTokens.TvSafeMarginVertical
            )
    ) {
        when (val state = uiState) {
            is WatchlistUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    TvCircularProgressIndicator(color = ThemeTokens.FocusGold)
                }
            }
            is WatchlistUiState.Success -> {
                if (state.favorites.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "★ Your Watchlist is Empty",
                                color = ThemeTokens.TextWhite,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add movies and TV series to your watchlist to watch them later.",
                                color = ThemeTokens.TextGrey,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "My Watchlist (${state.favorites.size})",
                            color = ThemeTokens.TextWhite,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.favorites, key = { it.tmdbId }) { fav ->
                                val mediaItem = MediaItem(
                                    tmdbId = fav.tmdbId,
                                    title = fav.title,
                                    overview = "",
                                    posterPath = fav.posterPath,
                                    backdropPath = null,
                                    releaseYear = "",
                                    genres = emptyList(),
                                    rating = 0.0,
                                    runtime = null,
                                    mediaType = fav.mediaType
                                )
                                MovieCard(
                                    mediaItem = mediaItem,
                                    onClick = { onMediaClick(mediaItem) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
