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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsDialog(
    playbackSpeed: Float,
    aspectRatioMode: String,
    onToggleSpeed: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onOpenWebPlayer: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ThemeTokens.SurfaceDarkBlue)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "⚙️ Playback Settings",
                    color = ThemeTokens.TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Playback Speed
                var speedFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (speedFocused) ThemeTokens.FocusGold else Color.White.copy(alpha = 0.05f))
                        .clickable { onToggleSpeed() }
                        .focusable()
                        .onFocusChanged { speedFocused = it.isFocused }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Playback Speed",
                        color = if (speedFocused) Color.Black else ThemeTokens.TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${playbackSpeed}x",
                        color = if (speedFocused) Color.Black else ThemeTokens.FocusGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Aspect Ratio
                var aspectFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (aspectFocused) ThemeTokens.FocusGold else Color.White.copy(alpha = 0.05f))
                        .clickable { onToggleAspectRatio() }
                        .focusable()
                        .onFocusChanged { aspectFocused = it.isFocused }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aspect Ratio",
                        color = if (aspectFocused) Color.Black else ThemeTokens.TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = aspectRatioMode,
                        color = if (aspectFocused) Color.Black else ThemeTokens.FocusGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Switch to Web Player
                var webFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (webFocused) ThemeTokens.FocusGold else Color.White.copy(alpha = 0.05f))
                        .clickable {
                            onOpenWebPlayer()
                            onDismiss()
                        }
                        .focusable()
                        .onFocusChanged { webFocused = it.isFocused }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Switch to Web Player",
                        color = if (webFocused) Color.Black else ThemeTokens.TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "🌐 Open",
                        color = if (webFocused) Color.Black else ThemeTokens.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
