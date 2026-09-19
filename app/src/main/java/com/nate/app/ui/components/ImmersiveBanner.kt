package com.nate.app.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.nate.app.data.model.Media
import com.nate.app.ui.theme.DarkBackground
import com.nate.app.ui.theme.TextPrimary
import com.nate.app.ui.theme.TextSecondary

@Composable
fun ImmersiveBanner(
    media: Media?,
    isBookmarked: Boolean,
    onWatchClick: (Media) -> Unit,
    onBookmarkClick: (Media) -> Unit,
    onDetailsClick: (Media) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        // 1. Animated background image crossfading
        Crossfade(
            targetState = media?.backdropUrl,
            animationSpec = tween(600),
            label = "backdrop_crossfade"
        ) { url ->
            if (!url.isNullOrEmpty()) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                )
            }
        }

        // 2. Linear gradient shroud filters (Left-to-Right for title readability, Top-to-Bottom to merge with grid background)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DarkBackground,
                            DarkBackground.copy(alpha = 0.95f),
                            DarkBackground.copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1400f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DarkBackground.copy(alpha = 0.5f),
                            DarkBackground
                        )
                    )
                )
        )

        // 3. Foreground details (Title, Meta-info, Synopsis, and Focused Action Controls)
        media?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(550.dp)
                    .padding(start = 24.dp, top = 32.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Title
                Text(
                    text = item.displayTitle,
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Meta Indicators (Year, Rating, Type)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rating Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEAB308))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format("★ %.1f", item.voteAverage ?: 0.0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Year
                    val year = item.displayDate.split("-").firstOrNull() ?: "N/A"
                    Text(
                        text = year,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Type Badge
                    val typeLabel = if (item.mediaType == "tv") "TV SHOW" else "MOVIE"
                    Text(
                        text = typeLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Synopsis description
                Text(
                    text = item.overview ?: "No overview available.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Remote D-pad focused action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { onWatchClick(item) },
                        scale = ButtonDefaults.scale(focusedScale = 1.08f),
                        colors = ButtonDefaults.colors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F172A),
                            focusedContainerColor = Color(0xFFFFC107),
                            focusedContentColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Now",
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = "Watch Now",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedButton(
                        onClick = { onDetailsClick(item) },
                        scale = ButtonDefaults.scale(focusedScale = 1.08f),
                        colors = ButtonDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            focusedContentColor = TextPrimary
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "More Info",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedButton(
                        onClick = { onBookmarkClick(item) },
                        scale = ButtonDefaults.scale(focusedScale = 1.08f),
                        colors = ButtonDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            focusedContentColor = TextPrimary
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isBookmarked) "Remove from watchlist" else "Add to watchlist",
                                tint = if (isBookmarked) Color.Red else TextPrimary,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = if (isBookmarked) "Watchlisted" else "Add Watchlist",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
