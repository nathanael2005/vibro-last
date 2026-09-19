package com.nate.core.data.repository

import com.nate.core.data.api.MediaApiService
import com.nate.core.data.local.FavoriteEntity
import com.nate.core.data.local.FavoritesDao
import com.nate.core.data.local.PlaybackResumeDao
import com.nate.core.data.local.PlaybackResumeEntity
import com.nate.core.data.local.WatchHistoryDao
import com.nate.core.data.local.WatchHistoryEntity
import com.nate.core.domain.model.Category
import com.nate.core.domain.model.Favorite
import com.nate.core.domain.model.MediaItem
import com.nate.core.domain.model.PlaybackResume
import com.nate.core.domain.model.StreamInfo
import com.nate.core.domain.model.WatchHistory
import com.nate.core.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val apiService: MediaApiService,
    private val watchHistoryDao: WatchHistoryDao,
    private val favoritesDao: FavoritesDao,
    private val playbackResumeDao: PlaybackResumeDao
) : MediaRepository {

    // Network API Calls
    override suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        val categories = mutableListOf<Category>()
        try {
            coroutineScope {
                val trendingMoviesDeferred = async { runCatching { apiService.getTrendingMovies().results.map { it.toDomain("movie") } }.getOrDefault(emptyList()) }
                val trendingTvDeferred = async { runCatching { apiService.getTrendingTv().results.map { it.toDomain("tv") } }.getOrDefault(emptyList()) }
                val popularMoviesDeferred = async { runCatching { apiService.getPopularMovies().results.map { it.toDomain("movie") } }.getOrDefault(emptyList()) }
                val popularTvDeferred = async { runCatching { apiService.getPopularTv().results.map { it.toDomain("tv") } }.getOrDefault(emptyList()) }
                val topRatedMoviesDeferred = async { runCatching { apiService.getTopRatedMovies().results.map { it.toDomain("movie") } }.getOrDefault(emptyList()) }
                val topRatedTvDeferred = async { runCatching { apiService.getTopRatedTv().results.map { it.toDomain("tv") } }.getOrDefault(emptyList()) }

                val trendingMovies = trendingMoviesDeferred.await()
                val trendingTv = trendingTvDeferred.await()
                val popularMovies = popularMoviesDeferred.await()
                val popularTv = popularTvDeferred.await()
                val topRatedMovies = topRatedMoviesDeferred.await()
                val topRatedTv = topRatedTvDeferred.await()

                if (trendingMovies.isNotEmpty()) {
                    categories.add(Category(id = "trending_movies", name = "Trending Movies", items = trendingMovies))
                }
                if (trendingTv.isNotEmpty()) {
                    categories.add(Category(id = "trending_tv", name = "Trending TV Series", items = trendingTv))
                }
                if (popularMovies.isNotEmpty()) {
                    categories.add(Category(id = "popular_movies", name = "Popular Movies", items = popularMovies))
                }
                if (popularTv.isNotEmpty()) {
                    categories.add(Category(id = "popular_tv", name = "Popular TV Shows", items = popularTv))
                }
                if (topRatedMovies.isNotEmpty()) {
                    categories.add(Category(id = "top_rated_movies", name = "Top Rated Movies", items = topRatedMovies))
                }
                if (topRatedTv.isNotEmpty()) {
                    categories.add(Category(id = "top_rated_tv", name = "Top Rated TV Series", items = topRatedTv))
                }
            }
        } catch (_: Exception) {}
        categories
    }

    override suspend fun getMoviesCategories(): List<Category> = withContext(Dispatchers.IO) {
        val categories = mutableListOf<Category>()
        try {
            coroutineScope {
                val popularDeferred = async { runCatching { apiService.getPopularMovies().results.map { it.toDomain("movie") } }.getOrDefault(emptyList()) }
                val topRatedDeferred = async { runCatching { apiService.getTopRatedMovies().results.map { it.toDomain("movie") } }.getOrDefault(emptyList()) }
                val nowPlayingDeferred = async { runCatching { apiService.getNowPlayingMovies().results.map { it.toDomain("movie") } }.getOrDefault(emptyList()) }
                val actionDeferred = async { runCatching { apiService.getDiscoverMovies("28").results.map { it.toDomain("movie") } }.getOrDefault(emptyList()) }
                val scifiDeferred = async { runCatching { apiService.getDiscoverMovies("878").results.map { it.toDomain("movie") } }.getOrDefault(emptyList()) }
                val comedyDeferred = async { runCatching { apiService.getDiscoverMovies("35").results.map { it.toDomain("movie") } }.getOrDefault(emptyList()) }

                val popular = popularDeferred.await()
                val topRated = topRatedDeferred.await()
                val nowPlaying = nowPlayingDeferred.await()
                val action = actionDeferred.await()
                val scifi = scifiDeferred.await()
                val comedy = comedyDeferred.await()

                if (popular.isNotEmpty()) categories.add(Category(id = "popular_movies", name = "Popular Movies", items = popular))
                if (topRated.isNotEmpty()) categories.add(Category(id = "top_rated_movies", name = "Top Rated Movies", items = topRated))
                if (nowPlaying.isNotEmpty()) categories.add(Category(id = "now_playing_movies", name = "Now Playing in Theaters", items = nowPlaying))
                if (action.isNotEmpty()) categories.add(Category(id = "action_movies", name = "Action Blockbusters", items = action))
                if (scifi.isNotEmpty()) categories.add(Category(id = "scifi_movies", name = "Sci-Fi & Fantasy", items = scifi))
                if (comedy.isNotEmpty()) categories.add(Category(id = "comedy_movies", name = "Comedy Hits", items = comedy))
            }
        } catch (_: Exception) {}
        categories
    }

    override suspend fun getTvCategories(): List<Category> = withContext(Dispatchers.IO) {
        val categories = mutableListOf<Category>()
        try {
            coroutineScope {
                val popularDeferred = async { runCatching { apiService.getPopularTv().results.map { it.toDomain("tv") } }.getOrDefault(emptyList()) }
                val topRatedDeferred = async { runCatching { apiService.getTopRatedTv().results.map { it.toDomain("tv") } }.getOrDefault(emptyList()) }
                val airingTodayDeferred = async { runCatching { apiService.getAiringTodayTv().results.map { it.toDomain("tv") } }.getOrDefault(emptyList()) }
                val dramaDeferred = async { runCatching { apiService.getDiscoverTv("18").results.map { it.toDomain("tv") } }.getOrDefault(emptyList()) }
                val animationDeferred = async { runCatching { apiService.getDiscoverTv("16").results.map { it.toDomain("tv") } }.getOrDefault(emptyList()) }

                val popular = popularDeferred.await()
                val topRated = topRatedDeferred.await()
                val airingToday = airingTodayDeferred.await()
                val drama = dramaDeferred.await()
                val animation = animationDeferred.await()

                if (popular.isNotEmpty()) categories.add(Category(id = "popular_tv", name = "Popular TV Shows", items = popular))
                if (topRated.isNotEmpty()) categories.add(Category(id = "top_rated_tv", name = "Top Rated TV Series", items = topRated))
                if (airingToday.isNotEmpty()) categories.add(Category(id = "airing_today_tv", name = "Airing Today", items = airingToday))
                if (drama.isNotEmpty()) categories.add(Category(id = "drama_tv", name = "Drama Series", items = drama))
                if (animation.isNotEmpty()) categories.add(Category(id = "animation_tv", name = "Animation & Anime", items = animation))
            }
        } catch (_: Exception) {}
        categories
    }

    override suspend fun getTrending(page: Int): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val movies = apiService.getTrendingMovies(page).results.map { it.toDomain("movie") }
            val tv = apiService.getTrendingTv(page).results.map { it.toDomain("tv") }
            (movies + tv).shuffled()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun searchMedia(query: String, page: Int): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            apiService.searchMulti(query, page).results
                .filter { it.posterPath != null || it.backdropPath != null }
                .map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getMediaDetail(type: String, tmdbId: String): MediaItem = withContext(Dispatchers.IO) {
        try {
            if (type == "tv") {
                apiService.getTvDetail(tmdbId).toDomain("tv")
            } else {
                apiService.getMovieDetail(tmdbId).toDomain("movie")
            }
        } catch (_: Exception) {
            MediaItem(
                tmdbId = tmdbId,
                title = "Video $tmdbId",
                overview = "",
                posterPath = null,
                backdropPath = null,
                releaseYear = "",
                genres = emptyList(),
                rating = 0.0,
                runtime = null,
                mediaType = type
            )
        }
    }

    override suspend fun getFullMediaDetail(type: String, tmdbId: String): com.nate.core.domain.model.MediaDetail = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                val isTv = type == "tv"
                val detailDeferred = async {
                    if (isTv) apiService.getTvDetail(tmdbId) else apiService.getMovieDetail(tmdbId)
                }
                val similarDeferred = async {
                    runCatching {
                        if (isTv) apiService.getSimilarTv(tmdbId).results.map { it.toDomain("tv") }
                        else apiService.getSimilarMovies(tmdbId).results.map { it.toDomain("movie") }
                    }.getOrDefault(emptyList())
                }

                val dto = detailDeferred.await()
                val similar = similarDeferred.await()
                val mediaItem = dto.toDomain(type)

                val seasonsInfo = dto.seasons?.filter { it.seasonNumber > 0 }?.map {
                    com.nate.core.domain.model.SeasonInfo(
                        seasonNumber = it.seasonNumber,
                        name = it.name ?: "Season ${it.seasonNumber}",
                        episodeCount = it.episodeCount ?: 0,
                        posterPath = it.posterPath?.let { p -> "https://image.tmdb.org/t/p/w500$p" }
                    )
                } ?: emptyList()

                com.nate.core.domain.model.MediaDetail(
                    mediaItem = mediaItem,
                    numberOfSeasons = dto.numberOfSeasons ?: seasonsInfo.size,
                    numberOfEpisodes = dto.numberOfEpisodes ?: 0,
                    genres = dto.genres?.map { it.name } ?: emptyList(),
                    runtime = mediaItem.runtime,
                    tagline = dto.tagline,
                    seasons = seasonsInfo,
                    similar = similar
                )
            }
        } catch (_: Exception) {
            val baseItem = getMediaDetail(type, tmdbId)
            com.nate.core.domain.model.MediaDetail(
                mediaItem = baseItem,
                numberOfSeasons = if (type == "tv") 1 else 0,
                numberOfEpisodes = 0,
                genres = emptyList(),
                runtime = null,
                tagline = null,
                seasons = emptyList(),
                similar = emptyList()
            )
        }
    }

    override suspend fun getTvSeasonDetail(tvId: String, seasonNumber: Int): com.nate.core.domain.model.SeasonDetail = withContext(Dispatchers.IO) {
        try {
            val dto = apiService.getTvSeasonDetail(tvId, seasonNumber)
            val episodes = dto.episodes?.map { ep ->
                val runtimeStr = ep.runtime?.let { "${it}m" }
                com.nate.core.domain.model.EpisodeItem(
                    id = ep.id,
                    seasonNumber = ep.seasonNumber,
                    episodeNumber = ep.episodeNumber,
                    name = ep.name ?: "Episode ${ep.episodeNumber}",
                    overview = ep.overview ?: "",
                    stillPath = ep.stillPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w500$it" },
                    runtime = runtimeStr,
                    voteAverage = ep.voteAverage
                )
            } ?: emptyList()

            com.nate.core.domain.model.SeasonDetail(
                seasonNumber = dto.seasonNumber,
                name = dto.name ?: "Season $seasonNumber",
                episodes = episodes
            )
        } catch (_: Exception) {
            com.nate.core.domain.model.SeasonDetail(
                seasonNumber = seasonNumber,
                name = "Season $seasonNumber",
                episodes = emptyList()
            )
        }
    }

    override suspend fun getStreamInfo(
        tmdbId: String,
        type: String,
        season: Int?,
        episode: Int?
    ): StreamInfo = withContext(Dispatchers.IO) {
        val s = season ?: 1
        val ep = episode ?: 1
        val isTv = type == "tv"

        val mediaItem = runCatching { getMediaDetail(type, tmdbId) }.getOrNull()
        val mediaName = mediaItem?.title ?: if (isTv) "Series" else "Movie"
        val displayTitle = if (isTv) "$mediaName • S${s} E${ep}" else mediaName

        StreamInfo(
            tmdbId = tmdbId,
            title = displayTitle,
            streams = emptyList(),
            subtitles = emptyList()
        )
    }

    private fun com.nate.core.data.api.TmdbMediaDto.toDomain(forcedType: String? = null): MediaItem {
        val detectedType = forcedType ?: mediaType ?: if (title != null) "movie" else "tv"
        val rawDate = releaseDate ?: firstAirDate ?: ""
        return MediaItem(
            tmdbId = id.toString(),
            title = title ?: name ?: "Untitled",
            overview = overview ?: "",
            posterPath = posterPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w500$it" },
            backdropPath = backdropPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/original$it" },
            releaseYear = rawDate.take(4),
            genres = emptyList(),
            rating = voteAverage ?: 0.0,
            runtime = null,
            mediaType = detectedType
        )
    }

    private fun com.nate.core.data.api.TmdbDetailDto.toDomain(forcedType: String): MediaItem {
        val rawDate = releaseDate ?: firstAirDate ?: ""
        val runtimeStr = runtime?.let { "${it / 60}h ${it % 60}m" }
        return MediaItem(
            tmdbId = id.toString(),
            title = title ?: name ?: "Untitled",
            overview = overview ?: "",
            posterPath = posterPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w500$it" },
            backdropPath = backdropPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/original$it" },
            releaseYear = rawDate.take(4),
            genres = genres?.map { it.name } ?: emptyList(),
            rating = voteAverage ?: 0.0,
            runtime = runtimeStr,
            mediaType = forcedType
        )
    }

    // Local Watch History
    override fun getWatchHistory(): Flow<List<WatchHistory>> {
        return watchHistoryDao.getWatchHistory().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun saveWatchHistory(item: WatchHistory) = withContext(Dispatchers.IO) {
        watchHistoryDao.insertWatchHistory(WatchHistoryEntity.fromDomain(item))
    }

    override suspend fun deleteWatchHistory(tmdbId: String) = withContext(Dispatchers.IO) {
        watchHistoryDao.deleteWatchHistory(tmdbId)
    }

    override suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        watchHistoryDao.clearWatchHistory()
    }

    // Local Favorites
    override fun getFavorites(): Flow<List<Favorite>> {
        return favoritesDao.getFavorites().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun isFavorite(tmdbId: String): Flow<Boolean> {
        return favoritesDao.isFavorite(tmdbId).flowOn(Dispatchers.IO)
    }

    override suspend fun saveFavorite(item: Favorite) = withContext(Dispatchers.IO) {
        favoritesDao.insertFavorite(FavoriteEntity.fromDomain(item))
    }

    override suspend fun deleteFavorite(tmdbId: String) = withContext(Dispatchers.IO) {
        favoritesDao.deleteFavorite(tmdbId)
    }

    // Local Playback Resume
    override suspend fun getPlaybackResume(
        tmdbId: String,
        season: Int,
        episode: Int
    ): PlaybackResume? = withContext(Dispatchers.IO) {
        val compositeId = PlaybackResumeEntity.buildId(tmdbId, season, episode)
        playbackResumeDao.getPlaybackResumeById(compositeId)?.toDomain()
    }

    override suspend fun savePlaybackResume(item: PlaybackResume) = withContext(Dispatchers.IO) {
        playbackResumeDao.insertPlaybackResume(PlaybackResumeEntity.fromDomain(item))
    }

    override suspend fun deletePlaybackResume(tmdbId: String, season: Int, episode: Int) = withContext(Dispatchers.IO) {
        val compositeId = PlaybackResumeEntity.buildId(tmdbId, season, episode)
        playbackResumeDao.deletePlaybackResumeById(compositeId)
    }
}
