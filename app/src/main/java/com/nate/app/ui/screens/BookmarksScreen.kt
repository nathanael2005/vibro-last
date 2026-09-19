package com.nate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nate.app.data.local.PreferencesManager
import com.nate.app.data.model.Media
import com.nate.app.ui.components.MovieCard
import com.nate.app.ui.theme.DarkBackground
import com.nate.app.ui.theme.TextPrimary
import com.nate.app.ui.theme.TextSecondary

@Composable
fun BookmarksScreen(
    preferencesManager: PreferencesManager,
    onMediaClick: (Media) -> Unit,
    modifier: Modifier = Modifier
) {
    val watchlist by preferencesManager.watchlistFlow.collectAsState(initial = emptyList())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header
            Text(
                text = "My Watchlist",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your bookmarked Movies and TV Shows are listed here.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (watchlist.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your watchlist is currently empty.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            } else {
                // Chunk watchlist into columns of 5 items for stable TV grid rows
                val chunkedWatchlist = remember(watchlist) { watchlist.chunked(5) }

                TvLazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(chunkedWatchlist) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { item ->
                                MovieCard(
                                    media = item,
                                    onClick = { onMediaClick(item) },
                                    onFocused = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
