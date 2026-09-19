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
fun QualityDialog(
    currentQuality: String = "1080p (Auto)",
    onSelectQuality: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val qualities = listOf("Auto (Best)", "1080p FHD", "720p HD", "480p SD")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ThemeTokens.SurfaceDarkBlue)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🖥️ Stream Quality",
                    color = ThemeTokens.TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                qualities.forEach { quality ->
                    val isSelected = quality.startsWith(currentQuality.take(4)) || (quality.startsWith("Auto") && currentQuality.contains("Auto"))
                    var isFocused by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isFocused) ThemeTokens.FocusGold
                                else if (isSelected) ThemeTokens.PrimaryBlue.copy(alpha = 0.6f)
                                else Color.White.copy(alpha = 0.05f)
                            )
                            .clickable {
                                onSelectQuality(quality)
                                onDismiss()
                            }
                            .focusable()
                            .onFocusChanged { isFocused = it.isFocused }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = quality,
                            color = if (isFocused) Color.Black else ThemeTokens.TextWhite,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = if (isFocused) Color.Black else ThemeTokens.FocusGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
