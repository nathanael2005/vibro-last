package com.nate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.nate.app.data.model.Media
import com.nate.app.ui.components.MovieCard
import com.nate.app.ui.theme.DarkBackground
import com.nate.app.ui.theme.DarkSurfaceVariant
import com.nate.app.ui.theme.TextPrimary
import com.nate.app.ui.theme.TextSecondary
import com.nate.app.ui.viewmodel.NateViewModelFactory
import com.nate.app.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModelFactory: NateViewModelFactory,
    onMediaClick: (Media) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SearchViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Search Content",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("Search movies, TV shows...", color = TextSecondary) },
                leadingIcon = {
                    androidx.compose.material3.Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = TextSecondary,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Handled by debounce */ }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                ),
            )

            if (uiState.isOffline) {
                Text(
                    text = "Working Offline - Showing cached results if available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.isOffline) Color(0xFFFFAB91) else TextSecondary,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }
                uiState.query.trim().isEmpty() -> {
                    SearchBrowsePanel(
                        onSuggestionClick = { viewModel.searchSuggestion(it) },
                        onGenreClick = { viewModel.searchGenre(it) },
                    )
                }
                uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = uiState.errorMessage
                                ?: "No results found for '${uiState.query.trim()}'.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                        )
                    }
                }
                else -> {
                    val chunkedResults = remember(uiState.results) { uiState.results.chunked(5) }
                    TvLazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        items(chunkedResults) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                rowItems.forEach { item ->
                                    MovieCard(
                                        media = item,
                                        onClick = { onMediaClick(item) },
                                        onFocused = {},
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

@Composable
private fun ColumnScope.SearchBrowsePanel(
    onSuggestionClick: (String) -> Unit,
    onGenreClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
    ) {
        Text(
            text = "Quick Search Suggestions",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        val suggestions = listOf("Marvel", "Avengers", "Batman", "Harry Potter", "Star Wars", "Matrix")
        TvLazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(suggestions) { keyword ->
                Surface(
                    onClick = { onSuggestionClick(keyword) },
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = DarkSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.secondary,
                        pressedContainerColor = MaterialTheme.colorScheme.secondary,
                    ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                    modifier = Modifier
                        .padding(horizontal = 2.dp),
                ) {
                    Text(
                        text = keyword,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Browse by Genre",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        val genres = listOf("Action", "Comedy", "Sci-Fi", "Drama", "Horror", "Anime", "Thriller", "Adventure")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            genres.chunked(4).forEach { rowGenres ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowGenres.forEach { genre ->
                        Surface(
                            onClick = { onGenreClick(genre) },
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = DarkSurfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.primary,
                                pressedContainerColor = MaterialTheme.colorScheme.primary,
                            ),
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                            modifier = with(this@Row) { Modifier.weight(1f) },
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}