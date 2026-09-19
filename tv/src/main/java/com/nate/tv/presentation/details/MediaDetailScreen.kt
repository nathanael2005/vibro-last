package com.nate.tv.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.nate.core.common.theme.ThemeTokens
import com.nate.core.domain.model.EpisodeItem
import com.nate.core.domain.model.MediaItem
import com.nate.tv.ui.components.EpisodeCard
import com.nate.tv.ui.components.MovieCard
import com.nate.tv.ui.components.TvCircularProgressIndicator

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    viewModel: MediaDetailViewModel,
    tmdbId: String,
    mediaType: String,
    onPlayClick: (mediaId: String, type: String, season: Int?, episode: Int?) -> Unit,
    onSimilarClick: (MediaItem) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val playButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(tmdbId, mediaType) {
        viewModel.loadDetail(tmdbId, mediaType)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeTokens.BackgroundBlack)
    ) {
        when (val state = uiState) {
            is MediaDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    TvCircularProgressIndicator(color = ThemeTokens.FocusGold)
                }
            }
            is MediaDetailUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadDetail(tmdbId, mediaType) }) {
                        Text("Retry")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onBack) {
                        Text("Back")
                    }
                }
            }
            is MediaDetailUiState.Success -> {
                val detail = state.detail
                val item = detail.mediaItem

                LaunchedEffect(state) {
                    try {
                        playButtonFocusRequester.requestFocus()
                    } catch (_: Exception) {}
                }

                // Background Backdrop Image
                AsyncImage(
                    model = item.backdropPath ?: item.posterPath,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Cinematic dark vignette overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ThemeTokens.BackgroundBlack.copy(alpha = 0.5f),
                                    ThemeTokens.BackgroundBlack.copy(alpha = 0.88f),
                                    ThemeTokens.BackgroundBlack
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ThemeTokens.BackgroundBlack.copy(alpha = 0.95f),
                                    ThemeTokens.BackgroundBlack.copy(alpha = 0.7f),
                                    Color.Transparent
                                ),
                                endX = 1400f
                            )
                        )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = ThemeTokens.TvSafeMarginHorizontal,
                            vertical = ThemeTokens.TvSafeMarginVertical
                        ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Back button header
                    item {
                        var backFocused by remember { mutableStateOf(false) }
                        Button(
                            onClick = onBack,
                            modifier = Modifier.onFocusChanged { backFocused = it.isFocused },
                            colors = ButtonDefaults.colors(
                                containerColor = if (backFocused) ThemeTokens.FocusGold else Color.White.copy(alpha = 0.1f),
                                contentColor = if (backFocused) Color.Black else ThemeTokens.TextWhite
                            ),
                            shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                        ) {
                            Text("← Back", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 2. Hero Detail Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Poster Card
                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .aspectRatio(2f / 3f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                    .background(ThemeTokens.SurfaceDarkBlue)
                            ) {
                                AsyncImage(
                                    model = item.posterPath,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Info Column
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.title,
                                    color = ThemeTokens.TextWhite,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                if (!detail.tagline.isNullOrEmpty()) {
                                    Text(
                                        text = "\"${detail.tagline}\"",
                                        color = ThemeTokens.FocusGold,
                                        fontSize = 14.sp,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Metadata Badges
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (item.rating > 0.0) {
                                        Box(
                                            modifier = Modifier
                                                .background(ThemeTokens.FocusGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .border(1.dp, ThemeTokens.FocusGold, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "⭐ ${String.format("%.1f", item.rating)} TMDB",
                                                color = ThemeTokens.FocusGold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (item.releaseYear.isNotEmpty()) {
                                        Text(
                                            text = item.releaseYear,
                                            color = ThemeTokens.TextWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (detail.runtime != null) {
                                        Text(
                                            text = detail.runtime ?: "",
                                            color = ThemeTokens.TextGrey,
                                            fontSize = 13.sp
                                        )
                                    } else if (detail.numberOfSeasons > 0) {
                                        Text(
                                            text = "${detail.numberOfSeasons} Seasons",
                                            color = ThemeTokens.TextGrey,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(ThemeTokens.SurfaceDarkBlue, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (mediaType == "tv") "TV SERIES" else "MOVIE",
                                            color = ThemeTokens.TextGrey,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Genres
                                if (detail.genres.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        detail.genres.take(4).forEach { genre ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = genre,
                                                    color = ThemeTokens.TextGrey,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Overview
                                Text(
                                    text = item.overview.ifEmpty { "No description available." },
                                    color = ThemeTokens.TextGrey,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Action Buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    var playFocused by remember { mutableStateOf(false) }
                                    Button(
                                        onClick = {
                                            if (mediaType == "tv") {
                                                val firstEp = state.currentSeasonDetail?.episodes?.firstOrNull()
                                                val epNum = firstEp?.episodeNumber ?: 1
                                                onPlayClick(tmdbId, mediaType, state.selectedSeason, epNum)
                                            } else {
                                                onPlayClick(tmdbId, mediaType, null, null)
                                            }
                                        },
                                        modifier = Modifier
                                            .clickable {
                                                if (mediaType == "tv") {
                                                    val firstEp = state.currentSeasonDetail?.episodes?.firstOrNull()
                                                    val epNum = firstEp?.episodeNumber ?: 1
                                                    onPlayClick(tmdbId, mediaType, state.selectedSeason, epNum)
                                                } else {
                                                    onPlayClick(tmdbId, mediaType, null, null)
                                                }
                                            }
                                            .focusRequester(playButtonFocusRequester)
                                            .onFocusChanged { playFocused = it.isFocused },
                                        colors = ButtonDefaults.colors(
                                            containerColor = if (playFocused) ThemeTokens.FocusGold else Color.White,
                                            contentColor = Color.Black
                                        ),
                                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                                    ) {
                                        Text(
                                            text = if (mediaType == "tv") "▶  Play Season ${state.selectedSeason} Ep 1" else "▶  Play Movie",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    var favFocused by remember { mutableStateOf(false) }
                                    Button(
                                        onClick = { viewModel.toggleFavorite() },
                                        modifier = Modifier
                                            .clickable { viewModel.toggleFavorite() }
                                            .onFocusChanged { favFocused = it.isFocused },
                                        colors = ButtonDefaults.colors(
                                            containerColor = if (favFocused) ThemeTokens.FocusGold else ThemeTokens.SurfaceDarkBlue,
                                            contentColor = if (favFocused) Color.Black else ThemeTokens.TextWhite
                                        ),
                                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                                    ) {
                                        Text(
                                            text = if (state.isFavorite) "✓  In Watchlist" else "+  Add to Watchlist",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. If TV Series: Seasons Tabs & Episodes Row
                    if (mediaType == "tv" && detail.seasons.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Seasons",
                                    color = ThemeTokens.TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(detail.seasons, key = { it.seasonNumber }) { season ->
                                        val isSelected = season.seasonNumber == state.selectedSeason
                                        var isFocused by remember { mutableStateOf(false) }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) ThemeTokens.FocusGold.copy(alpha = 0.2f)
                                                    else ThemeTokens.SurfaceDarkBlue
                                                )
                                                .border(
                                                    width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                                                    color = if (isFocused) ThemeTokens.FocusGold else if (isSelected) ThemeTokens.FocusGold else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.selectSeason(season.seasonNumber) }
                                                .focusable()
                                                .onFocusChanged { isFocused = it.isFocused }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = season.name,
                                                color = if (isFocused || isSelected) ThemeTokens.FocusGold else ThemeTokens.TextWhite,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Episodes Row
                        val episodes = state.currentSeasonDetail?.episodes ?: emptyList()
                        if (episodes.isNotEmpty()) {
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Episodes (Season ${state.selectedSeason})",
                                        color = ThemeTokens.TextWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        items(episodes, key = { it.id }) { ep ->
                                            EpisodeCard(
                                                episode = ep,
                                                onClick = {
                                                    onPlayClick(tmdbId, mediaType, state.selectedSeason, ep.episodeNumber)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. More Like This / Similar Titles
                    if (detail.similar.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "More Like This",
                                    color = ThemeTokens.TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(detail.similar, key = { it.tmdbId }) { similarItem ->
                                        MovieCard(
                                            mediaItem = similarItem,
                                            onClick = { onSimilarClick(similarItem) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
