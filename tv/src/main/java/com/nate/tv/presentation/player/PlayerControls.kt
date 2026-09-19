package com.nate.tv.presentation.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.nate.core.common.theme.ThemeTokens

/**
 * Pixel-Perfect TV Player Controls Overlay:
 * - Top-Left: Poster Avatar + Title + Provider Badge
 * - Top-Right: Coral/Orange Circular Close Button
 * - Center: [↺ 10s] [Glowing Orange Play/Pause] [↻ 10s]
 * - Bottom: Orange Timeline + Scrubber + Remaining Time
 * - Bottom Row: [Settings] [Server] [Subtitles] [Audio] [Quality] [Episodes]
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerControls(
    title: String,
    subtitleInfo: String?,
    serverName: String,
    posterUrl: String? = null,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    playbackSpeed: Float,
    aspectRatioMode: String,
    hasNextEpisode: Boolean,
    hasEpisodes: Boolean,
    seekFeedback: String?,
    nextEpisodeCountdown: Int?,
    actions: PlaybackActions,
    controlsFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val displayRemaining = if (durationMs > 0) formatTime(durationMs - positionMs) else formatTime(positionMs)

    val closeButtonFocusRequester = remember { FocusRequester() }
    val rewFocusRequester = remember { FocusRequester() }
    val fwdFocusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.85f),
                        Color.Black.copy(alpha = 0.20f),
                        Color.Black.copy(alpha = 0.40f),
                        Color.Black.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        // ── 1. Top Header (Avatar + Title + Provider Badge + Close Button) ──────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Poster thumbnail + Titles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circular Poster Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ThemeTokens.SurfaceDarkBlue)
                        .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                ) {
                    if (!posterUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = posterUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Tv,
                            contentDescription = null,
                            tint = ThemeTokens.FocusGold,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                // Title + Provider Subtitle
                Column {
                    val fullTitle = if (!subtitleInfo.isNullOrEmpty()) {
                        "$title • $subtitleInfo"
                    } else {
                        title
                    }

                    Text(
                        text = fullTitle,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 3.dp)
                    ) {
                        // Red/Orange Provider Badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE53935), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "HQ",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Text(
                            text = "$serverName • Stream",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right: Orange/Coral Circular Close Button (X)
            var closeFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (closeFocused) Color(0xFFFF5722) else Color(0xFFEF5350).copy(alpha = 0.85f))
                    .border(
                        width = if (closeFocused) 2.5.dp else 0.dp,
                        color = if (closeFocused) Color.White else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { actions.onBack() }
                    .focusRequester(closeButtonFocusRequester)
                    .focusProperties { down = controlsFocusRequester }
                    .focusable()
                    .onFocusChanged { closeFocused = it.isFocused }
                    .graphicsLayer {
                        scaleX = if (closeFocused) 1.15f else 1.0f
                        scaleY = if (closeFocused) 1.15f else 1.0f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close Player",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ── 2. Center Playback Controls ([↺ 10s]  [▶ Play/Pause]  [↻ 10s]) ──────────
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rewind 10s Button
            var rewFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (rewFocused) Color(0x66FFA000) else Color.Black.copy(alpha = 0.55f))
                    .border(
                        width = if (rewFocused) 2.dp else 1.dp,
                        color = if (rewFocused) Color(0xFFFFA000) else Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clickable { actions.onSeekBackward() }
                    .focusRequester(rewFocusRequester)
                    .focusProperties {
                        right = controlsFocusRequester
                        up = closeButtonFocusRequester
                    }
                    .focusable()
                    .onFocusChanged { rewFocused = it.isFocused }
                    .graphicsLayer {
                        scaleX = if (rewFocused) 1.15f else 1.0f
                        scaleY = if (rewFocused) 1.15f else 1.0f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Replay10,
                    contentDescription = "Rewind 10s",
                    tint = if (rewFocused) Color(0xFFFFA000) else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Big Glowing Orange Play/Pause Button
            var playFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFB300),
                                Color(0xFFFF8F00)
                            )
                        )
                    )
                    .border(
                        width = if (playFocused) 3.dp else 1.dp,
                        color = if (playFocused) Color.White else Color(0xFFFFD54F),
                        shape = CircleShape
                    )
                    .clickable { actions.onPlayPause() }
                    .focusRequester(controlsFocusRequester)
                    .focusProperties {
                        left = rewFocusRequester
                        right = fwdFocusRequester
                        up = closeButtonFocusRequester
                    }
                    .focusable()
                    .onFocusChanged { playFocused = it.isFocused }
                    .graphicsLayer {
                        scaleX = if (playFocused) 1.18f else 1.0f
                        scaleY = if (playFocused) 1.18f else 1.0f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(38.dp)
                )
            }

            // Forward 10s Button
            var fwdFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (fwdFocused) Color(0x66FFA000) else Color.Black.copy(alpha = 0.55f))
                    .border(
                        width = if (fwdFocused) 2.dp else 1.dp,
                        color = if (fwdFocused) Color(0xFFFFA000) else Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clickable { actions.onSeekForward() }
                    .focusRequester(fwdFocusRequester)
                    .focusProperties {
                        left = controlsFocusRequester
                        up = closeButtonFocusRequester
                    }
                    .focusable()
                    .onFocusChanged { fwdFocused = it.isFocused }
                    .graphicsLayer {
                        scaleX = if (fwdFocused) 1.15f else 1.0f
                        scaleY = if (fwdFocused) 1.15f else 1.0f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Forward10,
                    contentDescription = "Forward 10s",
                    tint = if (fwdFocused) Color(0xFFFFA000) else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Seek Feedback Toast
        AnimatedVisibility(
            visible = seekFeedback != null,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            seekFeedback?.let { feedback ->
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFFFA000), RoundedCornerShape(10.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = feedback,
                        color = Color(0xFFFFA000),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── 3. Bottom Section: Timeline & Action Icon Bar ──────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 20.dp)
        ) {
            // Time text (Top Right above timeline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = displayRemaining,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Glowing Orange Progress Line with Scrubber Thumb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                )

                // Filled Orange Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFFF9800))
                )

                // Scrubber Dot
                Box(
                    modifier = Modifier
                        .padding(start = (progress * 1000).dp.coerceAtMost(0.dp)) // subtle placement
                        .fillMaxWidth(progress),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB300))
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 4. Bottom Action Icons Row (Settings, Server, Subtitles, Audio, Quality, Episodes) ─
            val actionModifier = Modifier.focusProperties { up = controlsFocusRequester }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomActionItem(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    onClick = actions.onOpenSettings,
                    modifier = actionModifier
                )

                BottomActionItem(
                    icon = Icons.Filled.Cloud,
                    label = "Server",
                    onClick = actions.onOpenServerSelector,
                    modifier = actionModifier
                )

                BottomActionItem(
                    icon = Icons.Filled.Subtitles,
                    label = "Subtitles",
                    onClick = actions.onOpenSubtitles,
                    modifier = actionModifier
                )

                BottomActionItem(
                    icon = Icons.Filled.GraphicEq,
                    label = "Audio",
                    onClick = actions.onOpenAudioTracks,
                    modifier = actionModifier
                )

                BottomActionItem(
                    icon = Icons.Filled.Tv,
                    label = "Quality",
                    onClick = actions.onOpenQuality,
                    modifier = actionModifier
                )

                if (hasEpisodes) {
                    BottomActionItem(
                        icon = Icons.Filled.FormatListBulleted,
                        label = "Episodes",
                        onClick = actions.onOpenEpisodesSheet,
                        modifier = actionModifier
                    )
                }
            }
        }
    }
}

/**
 * Clean TV Action Button with Icon on top and label below.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun BottomActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color(0x33FFA000) else Color.Transparent)
            .border(
                width = if (isFocused) 1.5.dp else 0.dp,
                color = if (isFocused) Color(0xFFFFA000) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = if (isFocused) 1.12f else 1.0f
                scaleY = if (isFocused) 1.12f else 1.0f
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isFocused) Color(0xFFFFA000) else Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = if (isFocused) Color(0xFFFFA000) else Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}
