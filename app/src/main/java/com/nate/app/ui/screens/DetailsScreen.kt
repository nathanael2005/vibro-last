package com.nate.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.nate.app.data.local.PreferencesManager
import com.nate.app.data.model.Episode
import com.nate.app.data.model.Media
import com.nate.app.data.model.Season
import com.nate.app.ui.components.mouseClickable
import com.nate.app.ui.theme.DarkBackground
import com.nate.app.ui.theme.TextPrimary
import com.nate.app.ui.theme.TextSecondary
import com.nate.app.ui.viewmodel.DetailsViewModel
import com.nate.app.ui.viewmodel.NateViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun DetailsScreen(
    media: Media,
    viewModelFactory: NateViewModelFactory,
    preferencesManager: PreferencesManager,
    onWatchMovie: (Media) -> Unit,
    onWatchEpisode: (Media, Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DetailsViewModel = viewModel(
        key = "details-${media.mediaType}-${media.id}",
        factory = viewModelFactory,
    )
    val uiState by viewModel.uiState.collectAsState()
    val displayMedia = uiState.media ?: media
    val isBookmarked by preferencesManager
        .isBookmarkedFlow(displayMedia.id, displayMedia.mediaType ?: "movie")
        .collectAsState(initial = false)

    val watchBtnFocus = remember { FocusRequester() }

    LaunchedEffect(media.id, media.mediaType) {
        viewModel.load(media)
    }

    LaunchedEffect(uiState.isLoading, uiState.isEpisodeLoading) {
        if (!uiState.isLoading && !uiState.isEpisodeLoading) {
            delay(200)
            try {
                watchBtnFocus.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

  Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        AsyncImage(
            model = displayMedia.backdropUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkBackground.copy(alpha = 0.5f),
                            DarkBackground.copy(alpha = 0.9f),
                            DarkBackground,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DarkBackground,
                            DarkBackground.copy(alpha = 0.9f),
                            DarkBackground.copy(alpha = 0.5f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFFAB91),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp),
            )
        }

        TvLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = displayMedia.displayTitle,
                            style = MaterialTheme.typography.displayLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEAB308))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = String.format("★ %.1f", displayMedia.voteAverage ?: 0.0),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = displayMedia.displayDate,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (displayMedia.mediaType == "tv") "TV SHOW" else "MOVIE",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                            if (displayMedia.runtimeText.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = displayMedia.runtimeText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary,
                                )
                            }
                        }

                        if (displayMedia.genreText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = displayMedia.genreText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        displayMedia.tagline?.takeIf { it.isNotBlank() }?.let { tagline ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"$tagline\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                fontWeight = FontWeight.Light,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = displayMedia.overview?.takeIf { it.isNotBlank() }
                                ?: "No overview available.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            lineHeight = 24.sp,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (displayMedia.mediaType != "tv") {
                                Button(
                                    onClick = { onWatchMovie(displayMedia) },
                                    enabled = !uiState.isLoading,
                                    scale = ButtonDefaults.scale(focusedScale = 1.08f),
                                    colors = ButtonDefaults.colors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF0F172A),
                                        focusedContainerColor = Color(0xFFFFC107),
                                        focusedContentColor = Color(0xFF0F172A),
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .focusRequester(watchBtnFocus),
                                ) {
                                    PlayLabel("Watch Now")
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            } else {
                                val firstEpisode = uiState.episodes.firstOrNull()
                                val season = uiState.selectedSeason
                                val canPlay = season != null &&
                                    !uiState.isEpisodeLoading &&
                                    firstEpisode != null
                                Button(
                                    onClick = {
                                        if (canPlay && season != null && firstEpisode != null) {
                                            onWatchEpisode(
                                                displayMedia,
                                                season.seasonNumber,
                                                firstEpisode.episodeNumber,
                                                uiState.episodeCount,
                                            )
                                        }
                                    },
                                    enabled = canPlay,
                                    scale = ButtonDefaults.scale(focusedScale = 1.08f),
                                    colors = ButtonDefaults.colors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF0F172A),
                                        focusedContainerColor = Color(0xFFFFC107),
                                        focusedContentColor = Color(0xFF0F172A),
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .focusRequester(watchBtnFocus),
                                ) {
                                    PlayLabel(
                                        when {
                                            uiState.isEpisodeLoading -> "Loading episodes…"
                                            firstEpisode == null -> "No episodes"
                                            else -> "Play Episode ${firstEpisode.episodeNumber}"
                                        },
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.toggleBookmark(displayMedia, isBookmarked)
                                },
                                scale = ButtonDefaults.scale(focusedScale = 1.08f),
                                colors = ButtonDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                    focusedContentColor = TextPrimary,
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp)),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isBookmarked) {
                                            Icons.Default.Favorite
                                        } else {
                                            Icons.Default.FavoriteBorder
                                        },
                                        contentDescription = if (isBookmarked) {
                                            "Remove from watchlist"
                                        } else {
                                            "Add to watchlist"
                                        },
                                        tint = if (isBookmarked) Color.Red else TextPrimary,
                                        modifier = Modifier.padding(end = 6.dp),
                                    )
                                    Text(
                                        text = if (isBookmarked) "Watchlisted" else "Add Watchlist",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .height(300.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Card(
                            onClick = {},
                            modifier = Modifier
                                .width(200.dp)
                                .height(300.dp),
                            shape = CardDefaults.shape(RoundedCornerShape(12.dp)),
                            border = CardDefaults.border(
                                border = Border(
                                    BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(12.dp),
                                ),
                            ),
                        ) {
                            AsyncImage(
                                model = displayMedia.posterUrl,
                                contentDescription = displayMedia.displayTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }

            if (displayMedia.mediaType == "tv" && !uiState.isLoading) {
                if (uiState.seasons.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                        ) {
                            Text(
                                text = "Seasons",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                            TvLazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.seasons) { season ->
                                    val isCurrent = uiState.selectedSeason?.id == season.id
                                    Surface(
                                        onClick = { viewModel.selectSeason(displayMedia.id, season) },
                                        colors = ClickableSurfaceDefaults.colors(
                                            containerColor = if (isCurrent) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.White.copy(alpha = 0.05f)
                                            },
                                            focusedContainerColor = MaterialTheme.colorScheme.secondary,
                                            pressedContainerColor = MaterialTheme.colorScheme.secondary,
                                        ),
                                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp),
                                    ) {
                                        Text(
                                            text = season.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                    ) {
                        Text(
                            text = "Episodes",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        if (uiState.isEpisodeLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                            }
                        } else if (uiState.episodes.isEmpty()) {
                            Text(
                                text = "No episodes available for this season.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                            )
                        } else {
                            TvLazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(uiState.episodes) { episode ->
                                    EpisodeCard(
                                        episode = episode,
                                        onClick = {
                                            uiState.selectedSeason?.let { season ->
                                                onWatchEpisode(
                                                    displayMedia,
                                                    season.seasonNumber,
                                                    episode.episodeNumber,
                                                    uiState.episodeCount,
                                                )
                                            }
                                        },
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

@Composable
private fun PlayLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            modifier = Modifier.padding(end = 6.dp),
        )
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun EpisodeCard(
    episode: Episode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(220.dp)
            .height(160.dp),
        scale = CardDefaults.scale(focusedScale = 1.08f),
        border = CardDefaults.border(
            focusedBorder = Border(
                BorderStroke(3.dp, Color.White),
                shape = RoundedCornerShape(8.dp),
            ),
            border = Border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
            ),
        ),
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                AsyncImage(
                    model = episode.stillUrl,
                    contentDescription = episode.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play episode",
                        tint = Color.White,
                        modifier = Modifier
                            .width(36.dp)
                            .height(36.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
            ) {
                Text(
                    text = "E${episode.episodeNumber}: ${episode.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
