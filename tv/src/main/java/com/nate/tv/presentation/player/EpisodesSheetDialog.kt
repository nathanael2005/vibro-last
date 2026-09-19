package com.nate.tv.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.nate.core.common.theme.ThemeTokens
import com.nate.core.domain.model.EpisodeItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EpisodesSheetDialog(
    episodes: List<EpisodeItem>,
    currentEpisodeNumber: Int?,
    onSelectEpisode: (EpisodeItem) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(520.dp)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(ThemeTokens.SurfaceDarkBlue)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📺 Episodes",
                        color = ThemeTokens.TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${episodes.size} Episodes",
                        color = ThemeTokens.TextGrey,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(episodes, key = { it.id }) { ep ->
                        val isCurrent = ep.episodeNumber == currentEpisodeNumber
                        var isFocused by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isFocused) ThemeTokens.FocusGold
                                    else if (isCurrent) ThemeTokens.PrimaryBlue.copy(alpha = 0.6f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = if (isFocused) 2.dp else if (isCurrent) 1.dp else 0.dp,
                                    color = if (isFocused) ThemeTokens.FocusGold else if (isCurrent) ThemeTokens.FocusGold else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSelectEpisode(ep)
                                    onDismiss()
                                }
                                .focusable()
                                .onFocusChanged { isFocused = it.isFocused }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = ep.stillPath,
                                    contentDescription = ep.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "EP ${ep.episodeNumber}: ${ep.name}",
                                    color = if (isFocused) Color.Black else ThemeTokens.TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (ep.overview.isNotEmpty()) {
                                    Text(
                                        text = ep.overview,
                                        color = if (isFocused) Color.Black.copy(alpha = 0.7f) else ThemeTokens.TextGrey,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isFocused) Color.Black else ThemeTokens.FocusGold,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PLAYING",
                                        color = if (isFocused) ThemeTokens.FocusGold else Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
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
