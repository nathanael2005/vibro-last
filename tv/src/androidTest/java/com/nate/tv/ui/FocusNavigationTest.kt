package com.nate.tv.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import com.nate.core.domain.model.MediaItem
import com.nate.tv.ui.components.MovieCard
import com.nate.tv.ui.theme.OnStreamTvTheme
import org.junit.Rule
import org.junit.Test

class FocusNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInitialFocusAndDpadNavigation() {
        val testItems = listOf(
            MediaItem(
                tmdbId = "1",
                title = "Movie One",
                overview = "Overview One",
                posterPath = null,
                backdropPath = null,
                releaseYear = "2024",
                genres = emptyList(),
                rating = 8.0,
                runtime = null,
                mediaType = "movie"
            ),
            MediaItem(
                tmdbId = "2",
                title = "Movie Two",
                overview = "Overview Two",
                posterPath = null,
                backdropPath = null,
                releaseYear = "2024",
                genres = emptyList(),
                rating = 7.5,
                runtime = null,
                mediaType = "movie"
            )
        )

        composeTestRule.setContent {
            OnStreamTvTheme {
                val firstFocusRequester = remember { FocusRequester() }
                
                TvLazyRow(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(testItems) { item ->
                        val modifier = if (item.tmdbId == "1") {
                            Modifier.focusRequester(firstFocusRequester)
                        } else {
                            Modifier
                        }
                        MovieCard(
                            mediaItem = item,
                            onClick = {},
                            modifier = modifier
                        )
                    }
                }
                
                LaunchedEffect(Unit) {
                    firstFocusRequester.requestFocus()
                }
            }
        }

        // 1. Verify Movie One gets initial focus
        composeTestRule.onNodeWithText("Movie One").assertIsFocused()

        // 2. Perform D-pad right keypress to shift focus
        composeTestRule.onNodeWithText("Movie One").performKeyInput {
            pressKey(Key.DirectionRight)
        }

        // 3. Verify focus has shifted to Movie Two
        composeTestRule.onNodeWithText("Movie Two").assertIsFocused()
    }
}
