package com.nate.tv.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tv
import androidx.tv.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.nate.core.common.theme.ThemeTokens

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    data object Home : NavItem("home", "Home", Icons.Outlined.Home)
    data object Movies : NavItem("movies", "Movies", Icons.Outlined.Movie)
    data object TvShows : NavItem("tv_shows", "TV Shows", Icons.Outlined.Tv)
    data object Search : NavItem("search", "Search", Icons.Outlined.Search)
    data object Watchlist : NavItem("watchlist", "Watchlist", Icons.Outlined.Bookmarks)
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvNavigationShell(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    drawerFocusRequester: FocusRequester,
    contentFocusRequester: FocusRequester,
    content: @Composable () -> Unit
) {
    val navItems = remember {
        listOf(
            NavItem.Home,
            NavItem.Movies,
            NavItem.TvShows,
            NavItem.Search,
            NavItem.Watchlist
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeTokens.BackgroundBlack)
    ) {
        // Fixed static 74dp Navigation Rail with balanced vertical spacing
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(74.dp)
                .background(ThemeTokens.SurfaceDarkBlue.copy(alpha = 0.95f))
                .padding(vertical = 20.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Brand Logo
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ThemeTokens.FocusGold)
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Centered & Fully Spaced Navigation Icons
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = currentRoute == item.route
                    var isItemFocused by remember { mutableStateOf(false) }

                    val itemModifier = if (index == 0) {
                        Modifier.focusRequester(drawerFocusRequester)
                    } else {
                        Modifier
                    }

                    Box(
                        modifier = itemModifier
                            .size(48.dp)
                            .graphicsLayer {
                                val scale = if (isItemFocused) 1.12f else 1.0f
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isItemFocused) ThemeTokens.FocusGold
                                else if (isSelected) ThemeTokens.PrimaryBlue.copy(alpha = 0.8f)
                                else Color.White.copy(alpha = 0.04f)
                            )
                            .border(
                                width = if (isItemFocused) 2.dp else if (isSelected) 1.5.dp else 0.dp,
                                color = if (isItemFocused) ThemeTokens.FocusGold else if (isSelected) ThemeTokens.FocusGold else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onNavigate(item.route) }
                            .focusable()
                            .onFocusChanged { isItemFocused = it.isFocused }
                            .focusProperties {
                                right = contentFocusRequester
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp),
                            tint = if (isItemFocused) Color.Black else if (isSelected) ThemeTokens.FocusGold else ThemeTokens.TextWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Bottom subtle spacer/accent dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }

        // Primary Content Area (Static bounds)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            content()
        }
    }
}
