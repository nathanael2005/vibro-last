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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.nate.core.common.theme.ThemeTokens
import com.nate.core.domain.model.StreamLink

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ServerSelectorDialog(
    streams: List<StreamLink>,
    activeStream: StreamLink?,
    onSelect: (StreamLink) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(440.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ThemeTokens.SurfaceDarkBlue)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Select Streaming Server",
                        color = ThemeTokens.TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${streams.size} Available",
                        color = ThemeTokens.TextGrey,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(streams) { stream ->
                        val isCurrent = stream.serverId == activeStream?.serverId
                        var isFocused by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isFocused) ThemeTokens.FocusGold
                                    else if (isCurrent) ThemeTokens.PrimaryBlue.copy(alpha = 0.6f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = if (isFocused) 2.dp else if (isCurrent) 1.dp else 0.dp,
                                    color = if (isFocused) ThemeTokens.FocusGold else if (isCurrent) ThemeTokens.FocusGold else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSelect(stream)
                                    onDismiss()
                                }
                                .focusable()
                                .onFocusChanged { isFocused = it.isFocused }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stream.serverName,
                                    color = if (isFocused) Color.Black else ThemeTokens.TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Format: ${stream.format} • Fast CDN",
                                    color = if (isFocused) Color.Black.copy(alpha = 0.7f) else ThemeTokens.TextGrey,
                                    fontSize = 11.sp
                                )
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
                                        text = "ACTIVE",
                                        color = if (isFocused) ThemeTokens.FocusGold else Color.Black,
                                        fontSize = 10.sp,
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
