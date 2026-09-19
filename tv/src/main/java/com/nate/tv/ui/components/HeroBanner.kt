package com.nate.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nate.core.common.theme.ThemeTokens
import com.nate.core.domain.model.MediaItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroBanner(
    mediaItem: MediaItem?,
    onPlayClick: (MediaItem) -> Unit,
    onDetailsClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (mediaItem == null) return

    // Downsample backdrop for instantaneous decode and zero TV GPU overhead
    val backdropUrl = remember(mediaItem.backdropPath, mediaItem.posterPath) {
        val raw = mediaItem.backdropPath ?: mediaItem.posterPath
        if (raw != null && raw.contains("/original/")) {
            raw.replace("/original/", "/w780/")
        } else {
            raw
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ThemeTokens.SurfaceDarkBlue)
    ) {
        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(backdropUrl)
            .size(780, 439)
            .crossfade(false)
            .build()

        // Fast, hardware-accelerated backdrop image
        AsyncImage(
            model = imageRequest,
            contentDescription = mediaItem.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Single optimized gradient scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f),
                            ThemeTokens.BackgroundBlack.copy(alpha = 0.95f)
                        ),
                        startY = 40f
                    )
                )
        )

        // Hero Info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 18.dp, end = 120.dp)
        ) {
            Text(
                text = mediaItem.title,
                color = ThemeTokens.TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (mediaItem.rating > 0.0) {
                    Box(
                        modifier = Modifier
                            .background(ThemeTokens.FocusGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, ThemeTokens.FocusGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⭐ ${String.format("%.1f", mediaItem.rating)}",
                            color = ThemeTokens.FocusGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (mediaItem.releaseYear.isNotEmpty()) {
                    Text(
                        text = mediaItem.releaseYear,
                        color = ThemeTokens.TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(ThemeTokens.PrimaryBlue.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (mediaItem.mediaType == "tv") "TV SERIES" else "MOVIE",
                        color = ThemeTokens.TextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (mediaItem.overview.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = mediaItem.overview,
                    color = ThemeTokens.TextGrey,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var playFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = { onPlayClick(mediaItem) },
                    modifier = Modifier
                        .clickable { onPlayClick(mediaItem) }
                        .onFocusChanged { playFocused = it.isFocused },
                    colors = ButtonDefaults.colors(
                        containerColor = if (playFocused) ThemeTokens.FocusGold else Color.White,
                        contentColor = Color.Black
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "▶  Play",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                var detailsFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = { onDetailsClick(mediaItem) },
                    modifier = Modifier
                        .clickable { onDetailsClick(mediaItem) }
                        .onFocusChanged { detailsFocused = it.isFocused },
                    colors = ButtonDefaults.colors(
                        containerColor = if (detailsFocused) ThemeTokens.FocusGold else ThemeTokens.SurfaceDarkBlue,
                        contentColor = if (detailsFocused) Color.Black else ThemeTokens.TextWhite
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "ℹ  Details",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
