package com.nate.tv.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.nate.core.common.theme.ThemeTokens
import com.nate.core.domain.model.MediaItem
import com.nate.tv.ui.components.MovieCard
import com.nate.tv.ui.components.TvCircularProgressIndicator

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onMediaClick: (MediaItem) -> Unit,
    drawerFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val keyboardRows = listOf(
        listOf("A", "B", "C", "D", "E", "F"),
        listOf("G", "H", "I", "J", "K", "L"),
        listOf("M", "N", "O", "P", "Q", "R"),
        listOf("S", "T", "U", "V", "W", "X"),
        listOf("Y", "Z", "1", "2", "3", "4"),
        listOf("5", "6", "7", "8", "9", "0"),
        listOf("SPACE", "BACKSPACE", "CLEAR")
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeTokens.BackgroundBlack)
            .padding(
                start = ThemeTokens.TvSafeMarginHorizontal,
                end = ThemeTokens.TvSafeMarginHorizontal,
                top = ThemeTokens.TvSafeMarginVertical,
                bottom = ThemeTokens.TvSafeMarginVertical
            ),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Left Column: Search Bar & TV Keyboard
        Column(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
        ) {
            // Search Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ThemeTokens.SurfaceDarkBlue)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (query.isEmpty()) "🔍 Type movie or series name..." else query,
                    color = if (query.isEmpty()) ThemeTokens.TextGrey else ThemeTokens.TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TV Keyboard Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                keyboardRows.forEachIndexed { rowIndex, rowKeys ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowKeys.forEachIndexed { keyIndex, keyText ->
                            var isKeyFocused by remember { mutableStateOf(false) }
                            val isActionKey = keyText.length > 1
                            val keyWidth = if (isActionKey) Modifier.weight(1f) else Modifier.size(42.dp)

                            val keyModifier = if (keyIndex == 0) {
                                keyWidth.focusProperties {
                                    left = drawerFocusRequester
                                }
                            } else {
                                keyWidth
                            }

                            Box(
                                modifier = keyModifier
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isKeyFocused) ThemeTokens.FocusGold
                                        else ThemeTokens.SurfaceDarkBlue
                                    )
                                    .clickable {
                                        when (keyText) {
                                            "SPACE" -> viewModel.appendCharacter(" ")
                                            "BACKSPACE" -> viewModel.deleteLastCharacter()
                                            "CLEAR" -> viewModel.clearQuery()
                                            else -> viewModel.appendCharacter(keyText.lowercase())
                                        }
                                    }
                                    .focusable()
                                    .onFocusChanged { isKeyFocused = it.isFocused },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (keyText) {
                                        "SPACE" -> "␣ Space"
                                        "BACKSPACE" -> "⌫ Del"
                                        "CLEAR" -> "✕ Clear"
                                        else -> keyText
                                    },
                                    color = if (isKeyFocused) Color.Black else ThemeTokens.TextWhite,
                                    fontSize = if (isActionKey) 11.sp else 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Search Results Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Search across thousands of movies and TV series",
                            color = ThemeTokens.TextGrey,
                            fontSize = 16.sp
                        )
                    }
                }
                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TvCircularProgressIndicator(color = ThemeTokens.FocusGold)
                    }
                }
                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                    }
                }
                is SearchUiState.Success -> {
                    if (state.results.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found for \"${state.query}\"",
                                color = ThemeTokens.TextGrey,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.results, key = { it.tmdbId }) { item ->
                                MovieCard(
                                    mediaItem = item,
                                    onClick = { onMediaClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
