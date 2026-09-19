package com.nate.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nate.core.common.theme.ThemeTokens
import com.nate.core.domain.model.MediaItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieCard(
    mediaItem: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFocused: ((MediaItem) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(140.dp)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onFocused?.invoke(mediaItem)
                }
            }
            .graphicsLayer {
                scaleX = if (isFocused) 1.08f else 1.0f
                scaleY = if (isFocused) 1.08f else 1.0f
            }
            .clickable(onClick = onClick)
            .focusable()
    ) {
        val posterUrl = remember(mediaItem.posterPath) {
            val raw = mediaItem.posterPath
            if (raw != null && raw.contains("/w500/")) {
                raw.replace("/w500/", "/w342/")
            } else if (raw != null && raw.contains("/original/")) {
                raw.replace("/original/", "/w342/")
            } else {
                raw
            }
        }

        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(posterUrl)
            .size(342, 513)
            .crossfade(false)
            .build()

        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .background(ThemeTokens.SurfaceDarkBlue)
                .drawWithContent {
                    drawContent()
                    if (isFocused) {
                        drawRoundRect(
                            color = Color(0xFFFFC107),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                        )
                    }
                }
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = mediaItem.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Rating Badge (Top-Right)
            if (mediaItem.rating > 0.0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .background(
                            Color.Black.copy(alpha = 0.8f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⭐ ${String.format("%.1f", mediaItem.rating)}",
                        color = ThemeTokens.FocusGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Media Type Tag (Top-Left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp)
                    .background(
                        if (mediaItem.mediaType == "tv") ThemeTokens.AccentBurgundy.copy(alpha = 0.85f)
                        else ThemeTokens.PrimaryBlue.copy(alpha = 0.85f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (mediaItem.mediaType == "tv") "TV" else "MOVIE",
                    color = ThemeTokens.TextWhite,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = mediaItem.title,
            color = if (isFocused) ThemeTokens.FocusGold else ThemeTokens.TextWhite,
            fontSize = 13.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        if (mediaItem.releaseYear.isNotEmpty()) {
            Text(
                text = mediaItem.releaseYear,
                color = ThemeTokens.TextGrey,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}
