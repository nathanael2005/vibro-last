package com.nate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.nate.app.data.local.PreferencesManager
import com.nate.app.data.model.Media
import com.nate.app.data.repository.MediaRepository
import com.nate.app.ui.components.NavigationRail
import com.nate.app.ui.components.Screen
import com.nate.app.ui.screens.BookmarksScreen
import com.nate.app.ui.screens.DetailsScreen
import com.nate.app.ui.screens.HomeScreen
import com.nate.app.ui.screens.MoviesScreen
import com.nate.app.ui.screens.SearchScreen
import com.nate.app.ui.screens.SettingsScreen
import com.nate.app.ui.screens.TvShowsScreen
import com.nate.app.ui.theme.DarkBackground
import com.nate.app.ui.theme.NateTheme
import com.nate.app.ui.viewmodel.NateViewModelFactory
import com.nate.app.util.NetworkMonitor
import com.nate.app.util.PlayerNavigator

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var repository: MediaRepository
    private lateinit var viewModelFactory: NateViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(applicationContext)
        repository = MediaRepository(preferencesManager)
        val networkMonitor = NetworkMonitor(applicationContext)
        viewModelFactory = NateViewModelFactory(repository, preferencesManager, networkMonitor)

        setContent {
            NateTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    val isOnline by remember { networkMonitor.isOnlineFlow }
                        .collectAsState(initial = networkMonitor.isOnline())

                    var currentScreen by androidx.compose.runtime.saveable.rememberSaveable {
                        mutableStateOf(Screen.Home)
                    }
                    var activeDetailsMedia by androidx.compose.runtime.saveable.rememberSaveable {
                        mutableStateOf<Media?>(null)
                    }
                    val contentFocusRequester = remember { FocusRequester() }

                    LaunchedEffect(activeDetailsMedia, currentScreen) {
                        kotlinx.coroutines.delay(150)
                        try {
                            contentFocusRequester.requestFocus()
                        } catch (_: Exception) {
                        }
                    }

                    BackHandler(enabled = activeDetailsMedia != null) {
                        activeDetailsMedia = null
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground),
                    ) {
                        NavigationRail(
                            currentScreen = currentScreen,
                            onScreenSelected = { screen ->
                                activeDetailsMedia = null
                                currentScreen = screen
                            },
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .focusRequester(contentFocusRequester),
                        ) {
                            if (!isOnline) {
                                com.nate.app.ui.screens.ErrorPanel(
                                    message = "No internet connection. Please check your network settings.",
                                    onRetry = {}
                                )
                            } else {
                                val details = activeDetailsMedia
                                if (details != null) {
                                    DetailsScreen(
                                        media = details,
                                        viewModelFactory = viewModelFactory,
                                        preferencesManager = preferencesManager,
                                        onWatchMovie = { media ->
                                            PlayerNavigator.launchMovie(this@MainActivity, media)
                                        },
                                        onWatchEpisode = { media, season, episode, episodeCount ->
                                            PlayerNavigator.launchEpisode(
                                                context = this@MainActivity,
                                                media = media,
                                                season = season,
                                                episode = episode,
                                                episodeCount = episodeCount,
                                            )
                                        },
                                    )
                                } else {
                                    when (currentScreen) {
                                        Screen.Home -> HomeScreen(
                                            viewModelFactory = viewModelFactory,
                                            preferencesManager = preferencesManager,
                                            onMediaClick = { activeDetailsMedia = it },
                                            onWatchClick = { media ->
                                                if (media.mediaType == "tv") {
                                                    PlayerNavigator.launchEpisode(
                                                        context = this@MainActivity,
                                                        media = media,
                                                        season = 1,
                                                        episode = 1,
                                                        episodeCount = 0,
                                                    )
                                                } else {
                                                    PlayerNavigator.launchMovie(this@MainActivity, media)
                                                }
                                            },
                                            onNavigateToSettings = { currentScreen = Screen.Settings },
                                        )
                                        Screen.Movies -> MoviesScreen(
                                            viewModelFactory = viewModelFactory,
                                            preferencesManager = preferencesManager,
                                            onMediaClick = { activeDetailsMedia = it },
                                            onNavigateToSettings = { currentScreen = Screen.Settings },
                                        )
                                        Screen.TvShows -> TvShowsScreen(
                                            viewModelFactory = viewModelFactory,
                                            preferencesManager = preferencesManager,
                                            onMediaClick = { activeDetailsMedia = it },
                                            onNavigateToSettings = { currentScreen = Screen.Settings },
                                        )
                                        Screen.Search -> SearchScreen(
                                            viewModelFactory = viewModelFactory,
                                            onMediaClick = { activeDetailsMedia = it },
                                        )
                                        Screen.Bookmarks -> BookmarksScreen(
                                            preferencesManager = preferencesManager,
                                            onMediaClick = { activeDetailsMedia = it },
                                        )
                                        Screen.Settings -> SettingsScreen(
                                            preferencesManager = preferencesManager,
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
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var scale by remember { mutableStateOf(0.7f) }
    var alpha by remember { mutableStateOf(0f) }

    val scaleAnim by androidx.compose.animation.core.animateFloatAsState(
        targetValue = scale,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 1000,
            easing = androidx.compose.animation.core.FastOutSlowInEasing,
        ),
        label = "scale_anim",
    )

    val alphaAnim by androidx.compose.animation.core.animateFloatAsState(
        targetValue = alpha,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 800),
        label = "alpha_anim",
    )

    LaunchedEffect(Unit) {
        scale = 1.0f
        alpha = 1.0f
        kotlinx.coroutines.delay(1000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060913)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scaleAnim,
                        scaleY = scaleAnim,
                        alpha = alphaAnim,
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF082351), Color(0xFF5D0034)),
                        ),
                    )
                    .padding(horizontal = 48.dp, vertical = 24.dp),
            ) {
                Text(
                    text = "NATE",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFC107),
                    letterSpacing = 6.sp,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "YOUR PREMIUM STREAMING HUB",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 3.sp,
                modifier = Modifier.graphicsLayer(alpha = alphaAnim),
            )

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                color = Color(0xFFFFC107),
                modifier = Modifier.graphicsLayer(alpha = alphaAnim),
            )
        }
    }
}