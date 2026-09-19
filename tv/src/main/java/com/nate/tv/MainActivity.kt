package com.nate.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.nate.tv.presentation.details.MediaDetailScreen
import com.nate.tv.presentation.details.MediaDetailViewModel
import com.nate.tv.presentation.home.HomeScreen
import com.nate.tv.presentation.home.HomeViewModel
import com.nate.tv.presentation.movies.MoviesScreen
import com.nate.tv.presentation.movies.MoviesViewModel
import com.nate.tv.presentation.player.PlayerScreen
import com.nate.tv.presentation.player.PlayerViewModel
import com.nate.tv.presentation.search.SearchScreen
import com.nate.tv.presentation.search.SearchViewModel
import com.nate.tv.presentation.tvshows.TvShowsScreen
import com.nate.tv.presentation.tvshows.TvShowsViewModel
import com.nate.tv.presentation.watchlist.WatchlistScreen
import com.nate.tv.presentation.watchlist.WatchlistViewModel
import com.nate.tv.presentation.splash.SplashScreen
import com.nate.tv.ui.navigation.TvNavigationShell
import com.nate.tv.ui.theme.OnStreamTvTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class ScreenNav {
    data object Splash : ScreenNav()
    data class Main(val tabRoute: String = "home") : ScreenNav()
    data class Details(val tmdbId: String, val mediaType: String) : ScreenNav()
    data class Player(
        val tmdbId: String,
        val mediaType: String,
        val season: Int?,
        val episode: Int?
    ) : ScreenNav()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val moviesViewModel: MoviesViewModel by viewModels()
    private val tvShowsViewModel: TvShowsViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val watchlistViewModel: WatchlistViewModel by viewModels()
    private val detailViewModel: MediaDetailViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnStreamTvTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var isSplashDone by remember { mutableStateOf(false) }

                    if (!isSplashDone) {
                        SplashScreen(
                            onSplashFinished = {
                                isSplashDone = true
                            }
                        )
                    } else {
                        val initialScreen = remember {
                            val tId = intent?.getStringExtra("tmdbId")
                            if (!tId.isNullOrBlank()) {
                                val mType = intent?.getStringExtra("mediaType") ?: "tv"
                                val s = if (intent?.hasExtra("season") == true) intent?.getIntExtra("season", 1) else null
                                val ep = if (intent?.hasExtra("episode") == true) intent?.getIntExtra("episode", 1) else null
                                ScreenNav.Player(tId, mType, s, ep)
                            } else {
                                ScreenNav.Main("home")
                            }
                        }
                        val backStack = remember { mutableStateListOf<ScreenNav>(initialScreen) }
                        val currentScreen = backStack.lastOrNull() ?: ScreenNav.Main("home")

                        val drawerFocusRequester = remember { FocusRequester() }
                        val contentFocusRequester = remember { FocusRequester() }

                        var lastBackPressTime by remember { mutableStateOf(0L) }
                        val context = androidx.compose.ui.platform.LocalContext.current

                        fun navigateBack() {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        }

                        BackHandler(enabled = true) {
                            if (backStack.size > 1) {
                                navigateBack()
                            } else {
                                val now = System.currentTimeMillis()
                                if (now - lastBackPressTime < 2000) {
                                    (context as? ComponentActivity)?.finish()
                                } else {
                                    lastBackPressTime = now
                                    android.widget.Toast.makeText(context, "Press BACK again to exit NATI", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        when (val screen = currentScreen) {
                            is ScreenNav.Splash -> {
                                // Default to home
                                backStack[0] = ScreenNav.Main("home")
                            }
                            is ScreenNav.Player -> {
                            PlayerScreen(
                                viewModel = playerViewModel,
                                tmdbId = screen.tmdbId,
                                mediaType = screen.mediaType,
                                season = screen.season,
                                episode = screen.episode,
                                onBack = { navigateBack() }
                            )
                        }
                        is ScreenNav.Details -> {
                            MediaDetailScreen(
                                viewModel = detailViewModel,
                                tmdbId = screen.tmdbId,
                                mediaType = screen.mediaType,
                                onPlayClick = { id, type, s, ep ->
                                    backStack.add(ScreenNav.Player(id, type, s, ep))
                                },
                                onSimilarClick = { item ->
                                    if (backStack.isNotEmpty() && backStack.last() is ScreenNav.Details) {
                                        backStack[backStack.lastIndex] = ScreenNav.Details(item.tmdbId, item.mediaType)
                                    } else {
                                        backStack.add(ScreenNav.Details(item.tmdbId, item.mediaType))
                                    }
                                },
                                onBack = { navigateBack() }
                            )
                        }
                        is ScreenNav.Main -> {
                            val activeTab = screen.tabRoute

                            TvNavigationShell(
                                currentRoute = activeTab,
                                onNavigate = { newTab ->
                                    // When switching tabs, replace the top of the stack
                                    if (backStack.isNotEmpty()) {
                                        backStack[backStack.lastIndex] = ScreenNav.Main(newTab)
                                    } else {
                                        backStack.add(ScreenNav.Main(newTab))
                                    }
                                },
                                drawerFocusRequester = drawerFocusRequester,
                                contentFocusRequester = contentFocusRequester
                            ) {
                                when (activeTab) {
                                    "home" -> HomeScreen(
                                        viewModel = homeViewModel,
                                        onMediaClick = { mediaItem ->
                                            backStack.add(ScreenNav.Details(mediaItem.tmdbId, mediaItem.mediaType))
                                        },
                                        onPlayClick = { mediaItem ->
                                            val s = if (mediaItem.mediaType == "tv") 1 else null
                                            val ep = if (mediaItem.mediaType == "tv") 1 else null
                                            backStack.add(ScreenNav.Player(mediaItem.tmdbId, mediaItem.mediaType, s, ep))
                                        },
                                        contentFocusRequester = contentFocusRequester,
                                        drawerFocusRequester = drawerFocusRequester
                                    )
                                    "movies" -> MoviesScreen(
                                        viewModel = moviesViewModel,
                                        onMediaClick = { mediaItem ->
                                            backStack.add(ScreenNav.Details(mediaItem.tmdbId, "movie"))
                                        },
                                        onPlayClick = { mediaItem ->
                                            backStack.add(ScreenNav.Player(mediaItem.tmdbId, "movie", null, null))
                                        },
                                        drawerFocusRequester = drawerFocusRequester
                                    )
                                    "tv_shows" -> TvShowsScreen(
                                        viewModel = tvShowsViewModel,
                                        onMediaClick = { mediaItem ->
                                            backStack.add(ScreenNav.Details(mediaItem.tmdbId, "tv"))
                                        },
                                        onPlayClick = { mediaItem ->
                                            backStack.add(ScreenNav.Player(mediaItem.tmdbId, "tv", 1, 1))
                                        },
                                        drawerFocusRequester = drawerFocusRequester
                                    )
                                    "search" -> SearchScreen(
                                        viewModel = searchViewModel,
                                        onMediaClick = { mediaItem ->
                                            backStack.add(ScreenNav.Details(mediaItem.tmdbId, mediaItem.mediaType))
                                        },
                                        drawerFocusRequester = drawerFocusRequester
                                    )
                                    "watchlist" -> WatchlistScreen(
                                        viewModel = watchlistViewModel,
                                        onMediaClick = { mediaItem ->
                                            backStack.add(ScreenNav.Details(mediaItem.tmdbId, mediaItem.mediaType))
                                        },
                                        drawerFocusRequester = drawerFocusRequester
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
