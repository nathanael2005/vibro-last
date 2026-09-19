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
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.nate.core.domain.model.EpisodeItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EpisodeCard(
    episode: EpisodeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(episode.stillPath)
        .size(300, 169)
        .crossfade(false)
        .build()

    Column(
        modifier = modifier
            .width(220.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                scaleX = if (isFocused) 1.06f else 1.0f
                scaleY = if (isFocused) 1.06f else 1.0f
            }
            .clickable(onClick = onClick)
            .focusable()
    ) {
        Box(
            modifier = Modifier
                .width(220.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(ThemeTokens.SurfaceDarkBlue)
                .drawWithContent {
                    drawContent()
                    if (isFocused) {
                        drawRoundRect(
                            color = Color(0xFFFFC107),
                            cornerRadius = CornerRadius(10.dp.toPx()),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = episode.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Episode Number Pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(
                        Color.Black.copy(alpha = 0.75f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "EP ${episode.episodeNumber}",
                    color = ThemeTokens.FocusGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Duration (Bottom-Right)
            val epRuntime = episode.runtime
            if (!epRuntime.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(
                            Color.Black.copy(alpha = 0.75f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = epRuntime,
                        color = ThemeTokens.TextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = episode.name,
            color = if (isFocused) ThemeTokens.FocusGold else ThemeTokens.TextWhite,
            fontSize = 13.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (episode.overview.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = episode.overview,
                color = ThemeTokens.TextGrey,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}
