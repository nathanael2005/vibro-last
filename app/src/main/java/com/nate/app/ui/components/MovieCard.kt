package com.nate.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.nate.app.R
import com.nate.app.data.model.Media
import com.nate.app.ui.theme.TextPrimary

@Composable
fun MovieCard(
    media: Media,
    onClick: () -> Unit,
    onFocused: (Media) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(130.dp)
            .height(195.dp)
            .onFocusChanged { state ->
                if (state.isFocused) onFocused(media)
            },
        scale = CardDefaults.scale(focusedScale = 1.12f),
        border = CardDefaults.border(
            focusedBorder = Border(
                BorderStroke(3.dp, Color.White),
                shape = RoundedCornerShape(8.dp),
            ),
            border = Border(
                BorderStroke(1.5.dp, Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
            ),
        ),
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = media.posterUrl,
                contentDescription = "Poster for ${media.displayTitle}",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_back_arrow),
                error = painterResource(R.drawable.ic_back_arrow),
                modifier = Modifier.fillMaxSize(),
            )

            if (media.posterPath.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = media.displayTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        maxLines = 3,
                    )
                }
            }
        }
    }
}
