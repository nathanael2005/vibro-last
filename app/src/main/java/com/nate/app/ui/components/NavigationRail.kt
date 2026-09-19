package com.nate.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.nate.app.ui.theme.BrandBlue
import com.nate.app.ui.theme.BrandMaroon
import com.nate.app.ui.theme.TextPrimary
import com.nate.app.ui.theme.TextSecondary

val AccentYellow = Color(0xFFFFC107)

enum class Screen(val title: String) {
    Home("Home"),
    Movies("Movies"),
    TvShows("TV Shows"),
    Search("Search"),
    Bookmarks("Watchlist"),
    Settings("Settings")
}

fun Screen.getIcon(): ImageVector = when (this) {
    Screen.Home -> Icons.Default.Home
    Screen.Movies -> Icons.Default.PlayArrow
    Screen.TvShows -> Icons.Default.Star
    Screen.Search -> Icons.Default.Search
    Screen.Bookmarks -> Icons.Default.Favorite
    Screen.Settings -> Icons.Default.Settings
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun NavigationRail(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only expand when the rail column itself has focus — prevents content D-pad navigation
    // from accidentally triggering sidebar expansion
    var railHasFocus by remember { mutableStateOf(false) }

    val targetWidth = if (railHasFocus) 200.dp else 66.dp
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(durationMillis = 200),
        label = "nav_width"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(animatedWidth)
            .onFocusChanged { state ->
                // hasFocus = true only when a child inside this column is focused
                railHasFocus = state.hasFocus
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandBlue.copy(alpha = 0.98f),
                        BrandMaroon.copy(alpha = 0.98f)
                    )
                )
            )
            .padding(vertical = 20.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Logo mark
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentAlignment = Alignment.Center
        ) {
            if (railHasFocus) {
                Text(
                    text = "NATE",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentYellow,
                    letterSpacing = 4.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF082351)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Nav items
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Screen.values().forEach { screen ->
                val isSelected = currentScreen == screen

                Surface(
                    onClick = { onScreenSelected(screen) },
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSelected) Color.White.copy(alpha = 0.18f) else Color.Transparent,
                        focusedContainerColor = Color.White.copy(alpha = 0.28f),
                        pressedContainerColor = Color.White.copy(alpha = 0.35f)
                    ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInteropFilter { event ->
                            when (event.actionMasked) {
                                android.view.MotionEvent.ACTION_HOVER_ENTER -> {
                                    railHasFocus = true
                                    false
                                }
                                else -> false
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 13.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Active indicator dot
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(AccentYellow)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Icon(
                            imageVector = screen.getIcon(),
                            contentDescription = screen.title,
                            tint = if (isSelected) AccentYellow else TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )

                        if (railHasFocus) {
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = screen.title,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AccentYellow else TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
