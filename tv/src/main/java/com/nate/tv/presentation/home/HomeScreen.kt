package com.nate.tv.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.nate.core.common.theme.ThemeTokens
import com.nate.core.domain.model.MediaItem
import com.nate.tv.ui.components.HeroBanner
import com.nate.tv.ui.components.MovieCard
import com.nate.tv.ui.components.TvCircularProgressIndicator

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onPlayClick: (MediaItem) -> Unit,
    contentFocusRequester: FocusRequester,
    drawerFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeTokens.BackgroundBlack)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    TvCircularProgressIndicator(color = ThemeTokens.FocusGold)
                }
            }
            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "An error occurred: ${state.message}",
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadHomeContent() }) {
                        Text(text = "Retry")
                    }
                }
            }
            is HomeUiState.Success -> {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                val firstCardFocusRequester = remember { FocusRequester() }
                val featuredHero = state.trending.firstOrNull()

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 28.dp,
                        end = 28.dp,
                        top = 16.dp,
                        bottom = 48.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    // 1. Featured Spotlight
                    item {
                        HeroBanner(
                            mediaItem = featuredHero,
                            onPlayClick = onPlayClick,
                            onDetailsClick = onMediaClick
                        )
                    }

                    // 2. Trending Now row
                    item {
                        Column {
                            Text(
                                text = "🔥 Trending Now",
                                color = ThemeTokens.TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                itemsIndexed(state.trending, key = { _, item -> item.tmdbId }) { index, item ->
                                    val itemModifier = if (index == 0) {
                                        Modifier
                                            .focusRequester(firstCardFocusRequester)
                                            .focusRequester(contentFocusRequester)
                                            .focusProperties {
                                                left = drawerFocusRequester
                                            }
                                    } else {
                                        Modifier
                                    }
                                    MovieCard(
                                        mediaItem = item,
                                        onClick = { onMediaClick(item) },
                                        modifier = itemModifier
                                    )
                                }
                            }
                        }
                    }

                    // 3. Dynamic Categories rows
                    itemsIndexed(state.categories, key = { _, cat -> cat.id }) { _, category ->
                        Column {
                            Text(
                                text = category.name,
                                color = ThemeTokens.TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                            )
                            val rowState = androidx.compose.runtime.saveable.rememberSaveable(category.id, saver = androidx.compose.foundation.lazy.LazyListState.Saver) {
                                androidx.compose.foundation.lazy.LazyListState()
                            }
                            LazyRow(
                                state = rowState,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                itemsIndexed(category.items, key = { _, item -> item.tmdbId }) { index, item ->
                                    val itemModifier = if (index == 0) {
                                        Modifier.focusProperties {
                                            left = drawerFocusRequester
                                        }
                                    } else {
                                        Modifier
                                    }
                                    MovieCard(
                                        mediaItem = item,
                                        onClick = { onMediaClick(item) },
                                        modifier = itemModifier
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
