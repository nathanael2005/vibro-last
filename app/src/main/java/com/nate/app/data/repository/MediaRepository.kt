package com.nate.app.data.repository

import com.nate.app.data.api.ApiResult
import com.nate.app.data.api.TmdbGenres
import com.nate.app.data.api.TmdbService
import com.nate.app.data.local.PreferencesManager
import com.nate.app.data.model.Episode
import com.nate.app.data.model.Media
import com.nate.app.data.model.Season
import com.nate.app.data.model.TvShowDetails
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

import okhttp3.CertificatePinner
import com.nate.app.util.SecurityUtils

class MediaRepository(private val preferencesManager: PreferencesManager) {

    private val api: TmdbService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
        
        val decryptedBaseUrl = SecurityUtils.decrypt(
            "LiUAHVBGVUpAHSc/AAk2GQUbCAguA0oKIxVWVk4=",
            "NateStream"
        )
        
        Retrofit.Builder()
            .baseUrl(decryptedBaseUrl.ifEmpty { "https://api.themoviedb.org/3/" })
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbService::class.java)
    }

    private suspend fun getApiKey(): String =
        preferencesManager.apiKeyFlow.first().trim()

    private suspend fun <T> safeCall(block: suspend () -> T): ApiResult<T> = withContext(Dispatchers.IO) {
        val key = getApiKey()
        if (key.isEmpty()) {
            return@withContext ApiResult.Error("Add your TMDB API key in Settings to browse content.")
        }
        try {
            ApiResult.Success(block())
        } catch (e: HttpException) {
            val message = when (e.code()) {
                401 -> "Invalid TMDB API key. Check Settings and try again."
                404 -> "Content not found on TMDB."
                429 -> "TMDB rate limit reached. Wait a moment and try again."
                else -> "TMDB request failed (${e.code()})."
            }
            ApiResult.Error(message, e)
        } catch (_: SocketTimeoutException) {
            ApiResult.Error("Request timed out. Check your connection and try again.")
        } catch (_: IOException) {
            ApiResult.Error("Network error. Check your connection and try again.")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unexpected error loading content.", e)
        }
    }

    private suspend fun paginated(
        pages: Int = 2,
        fetchPage: suspend (page: Int) -> List<Media>,
    ): ApiResult<List<Media>> = safeCall {
        val combined = mutableListOf<Media>()
        for (page in 1..pages) {
            combined += fetchPage(page)
        }
        combined.distinctBy { "${it.mediaType}-${it.id}" }
    }

    suspend fun getTrendingMovies(): ApiResult<List<Media>> = paginated { page ->
        api.getTrendingMovies(getApiKey(), page).results.onEach { it.mediaType = "movie" }
    }

    suspend fun getTrendingTvShows(): ApiResult<List<Media>> = paginated { page ->
        api.getTrendingTvShows(getApiKey(), page).results.onEach { it.mediaType = "tv" }
    }

    suspend fun getPopularMovies(): ApiResult<List<Media>> = paginated { page ->
        api.getPopularMovies(getApiKey(), page).results.onEach { it.mediaType = "movie" }
    }

    suspend fun getTopRatedMovies(): ApiResult<List<Media>> = paginated { page ->
        api.getTopRatedMovies(getApiKey(), page).results.onEach { it.mediaType = "movie" }
    }

    suspend fun getNowPlayingMovies(): ApiResult<List<Media>> = paginated { page ->
        api.getNowPlayingMovies(getApiKey(), page).results.onEach { it.mediaType = "movie" }
    }

    suspend fun getUpcomingMovies(): ApiResult<List<Media>> = paginated { page ->
        api.getUpcomingMovies(getApiKey(), page).results.onEach { it.mediaType = "movie" }
    }

    suspend fun getPopularTvShows(): ApiResult<List<Media>> = paginated { page ->
        api.getPopularTvShows(getApiKey(), page).results.onEach { it.mediaType = "tv" }
    }

    suspend fun getTopRatedTvShows(): ApiResult<List<Media>> = paginated { page ->
        api.getTopRatedTvShows(getApiKey(), page).results.onEach { it.mediaType = "tv" }
    }

    suspend fun getAiringTodayTvShows(): ApiResult<List<Media>> = paginated { page ->
        api.getAiringTodayTvShows(getApiKey(), page).results.onEach { it.mediaType = "tv" }
    }

    suspend fun getOnTheAirTvShows(): ApiResult<List<Media>> = paginated { page ->
        api.getOnTheAirTvShows(getApiKey(), page).results.onEach { it.mediaType = "tv" }
    }

    suspend fun getHomeCatalog(): ApiResult<HomeCatalog> {
        val key = getApiKey()
        if (key.isEmpty()) {
            return ApiResult.Error("Add your TMDB API key in Settings to browse content.")
        }
        return withContext(Dispatchers.IO) {
            coroutineScope {
                val trendingMoviesDeferred = async { getTrendingMovies() }
                val trendingTvDeferred = async { getTrendingTvShows() }
                val popularMoviesDeferred = async { getPopularMovies() }
                val popularTvDeferred = async { getPopularTvShows() }

                val trendingMovies = trendingMoviesDeferred.await()
                val trendingTv = trendingTvDeferred.await()
                val popularMovies = popularMoviesDeferred.await()
                val popularTv = popularTvDeferred.await()

                val trendingMoviesData = (trendingMovies as? ApiResult.Success)?.data ?: emptyList()
                val trendingTvData = (trendingTv as? ApiResult.Success)?.data ?: emptyList()
                val popularMoviesData = (popularMovies as? ApiResult.Success)?.data ?: emptyList()
                val popularTvData = (popularTv as? ApiResult.Success)?.data ?: emptyList()

                if (trendingMoviesData.isEmpty() && trendingTvData.isEmpty() && popularMoviesData.isEmpty() && popularTvData.isEmpty()) {
                    val firstError = (trendingMovies as? ApiResult.Error)?.message
                        ?: (trendingTv as? ApiResult.Error)?.message
                        ?: (popularMovies as? ApiResult.Error)?.message
                        ?: (popularTv as? ApiResult.Error)?.message
                        ?: "Failed to load home catalog"
                    return@coroutineScope ApiResult.Error(firstError)
                }

                ApiResult.Success(
                    HomeCatalog(
                        trendingMovies = trendingMoviesData,
                        trendingTvShows = trendingTvData,
                        popularMovies = popularMoviesData,
                        popularTvShows = popularTvData,
                    )
                )
            }
        }
    }

    suspend fun getMoviesCatalog(): ApiResult<MoviesCatalog> {
        val key = getApiKey()
        if (key.isEmpty()) {
            return ApiResult.Error("Add your TMDB API key in Settings to browse content.")
        }
        return withContext(Dispatchers.IO) {
            coroutineScope {
                val trendingDeferred = async { getTrendingMovies() }
                val popularDeferred = async { getPopularMovies() }
                val topRatedDeferred = async { getTopRatedMovies() }
                val nowPlayingDeferred = async { getNowPlayingMovies() }
                val upcomingDeferred = async { getUpcomingMovies() }

                val trending = trendingDeferred.await()
                val popular = popularDeferred.await()
                val topRated = topRatedDeferred.await()
                val nowPlaying = nowPlayingDeferred.await()
                val upcoming = upcomingDeferred.await()

                val trendingData = (trending as? ApiResult.Success)?.data ?: emptyList()
                val popularData = (popular as? ApiResult.Success)?.data ?: emptyList()
                val topRatedData = (topRated as? ApiResult.Success)?.data ?: emptyList()
                val nowPlayingData = (nowPlaying as? ApiResult.Success)?.data ?: emptyList()
                val upcomingData = (upcoming as? ApiResult.Success)?.data ?: emptyList()

                if (trendingData.isEmpty() && popularData.isEmpty() && topRatedData.isEmpty() && nowPlayingData.isEmpty() && upcomingData.isEmpty()) {
                    val firstError = (trending as? ApiResult.Error)?.message ?: "Failed to load movies catalog"
                    return@coroutineScope ApiResult.Error(firstError)
                }

                ApiResult.Success(
                    MoviesCatalog(
                        trendingMovies = trendingData,
                        popularMovies = popularData,
                        topRatedMovies = topRatedData,
                        nowPlayingMovies = nowPlayingData,
                        upcomingMovies = upcomingData,
                    )
                )
            }
        }
    }

    suspend fun getTvCatalog(): ApiResult<TvCatalog> {
        val key = getApiKey()
        if (key.isEmpty()) {
            return ApiResult.Error("Add your TMDB API key in Settings to browse content.")
        }
        return withContext(Dispatchers.IO) {
            coroutineScope {
                val trendingDeferred = async { getTrendingTvShows() }
                val popularDeferred = async { getPopularTvShows() }
                val topRatedDeferred = async { getTopRatedTvShows() }
                val airingTodayDeferred = async { getAiringTodayTvShows() }
                val onTheAirDeferred = async { getOnTheAirTvShows() }

                val trending = trendingDeferred.await()
                val popular = popularDeferred.await()
                val topRated = topRatedDeferred.await()
                val airingToday = airingTodayDeferred.await()
                val onTheAir = onTheAirDeferred.await()

                val trendingData = (trending as? ApiResult.Success)?.data ?: emptyList()
                val popularData = (popular as? ApiResult.Success)?.data ?: emptyList()
                val topRatedData = (topRated as? ApiResult.Success)?.data ?: emptyList()
                val airingTodayData = (airingToday as? ApiResult.Success)?.data ?: emptyList()
                val onTheAirData = (onTheAir as? ApiResult.Success)?.data ?: emptyList()

                if (trendingData.isEmpty() && popularData.isEmpty() && topRatedData.isEmpty() && airingTodayData.isEmpty() && onTheAirData.isEmpty()) {
                    val firstError = (trending as? ApiResult.Error)?.message ?: "Failed to load TV catalog"
                    return@coroutineScope ApiResult.Error(firstError)
                }

                ApiResult.Success(
                    TvCatalog(
                        trendingTvShows = trendingData,
                        popularTvShows = popularData,
                        topRatedTvShows = topRatedData,
                        airingTodayTvShows = airingTodayData,
                        onTheAirTvShows = onTheAirData,
                    )
                )
            }
        }
    }

    suspend fun getMovieDetails(movieId: Int): ApiResult<Media> = safeCall {
        api.getMovieDetails(movieId, getApiKey()).copy(mediaType = "movie")
    }

    suspend fun getTvShowDetails(tvId: Int): ApiResult<TvShowDetails> = safeCall {
        api.getTvShowDetails(tvId, getApiKey())
    }

    suspend fun getEpisodesForSeason(tvId: Int, seasonNumber: Int): ApiResult<List<Episode>> = safeCall {
        api.getSeasonDetails(tvId, seasonNumber, getApiKey()).episodes
    }

    suspend fun search(query: String): ApiResult<List<Media>> = paginated { page ->
        api.searchMulti(query, getApiKey(), page).results
            .filter { it.mediaType == "movie" || it.mediaType == "tv" }
    }

    suspend fun discoverByGenreId(genreId: Int): ApiResult<List<Media>> = paginated { page ->
        coroutineScope {
            val movies = async {
                api.discoverMoviesByGenre(getApiKey(), genreId, page).results
                    .onEach { it.mediaType = "movie" }
            }
            val tv = async {
                api.discoverTvByGenre(getApiKey(), genreId, page).results
                    .onEach { it.mediaType = "tv" }
            }
            movies.await() + tv.await()
        }
    }

    suspend fun discoverByGenre(genreLabel: String): ApiResult<List<Media>> {
        val genreId = TmdbGenres.movieGenreId(genreLabel)
            ?: return ApiResult.Error("Unknown genre: $genreLabel")
        return discoverByGenreId(genreId)
    }

    suspend fun validateApiKey(candidate: String): ApiResult<Unit> {
        if (candidate.isBlank()) {
            return ApiResult.Error("API key cannot be empty.")
        }
        return try {
            api.getTrendingMovies(candidate, page = 1)
            ApiResult.Success(Unit)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                ApiResult.Error("That API key was rejected by TMDB.")
            } else {
                ApiResult.Error("Could not verify API key (${e.code()}).", e)
            }
        } catch (_: IOException) {
            ApiResult.Error("Could not reach TMDB to verify the key. Check your connection.")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Could not verify API key.", e)
        }
    }

    private fun <T> ApiResult<T>.requireData(): T = when (this) {
        is ApiResult.Success -> data
        is ApiResult.Error -> throw IllegalStateException(message)
    }
}

data class HomeCatalog(
    val trendingMovies: List<Media>,
    val trendingTvShows: List<Media>,
    val popularMovies: List<Media>,
    val popularTvShows: List<Media>,
)

data class MoviesCatalog(
    val trendingMovies: List<Media>,
    val popularMovies: List<Media>,
    val topRatedMovies: List<Media>,
    val nowPlayingMovies: List<Media>,
    val upcomingMovies: List<Media>,
)

data class TvCatalog(
    val trendingTvShows: List<Media>,
    val popularTvShows: List<Media>,
    val topRatedTvShows: List<Media>,
    val airingTodayTvShows: List<Media>,
    val onTheAirTvShows: List<Media>,
)

fun List<Season>.regularSeasons(): List<Season> = filter { it.seasonNumber > 0 }
