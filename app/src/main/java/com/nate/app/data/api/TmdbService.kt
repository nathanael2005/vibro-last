package com.nate.app.data.api

import com.nate.app.data.model.Media
import com.nate.app.data.model.SeasonDetails
import com.nate.app.data.model.TmdbResponse
import com.nate.app.data.model.TvShowDetails
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {
    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("trending/tv/day")
    suspend fun getTrendingTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("tv/airing_today")
    suspend fun getAiringTodayTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("tv/on_the_air")
    suspend fun getOnTheAirTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>

    @GET("discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
    ): TmdbResponse<Media>

    @GET("discover/tv")
    suspend fun discoverTvByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
    ): TmdbResponse<Media>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
    ): Media

    @GET("tv/{tv_id}")
    suspend fun getTvShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
    ): TvShowDetails

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String,
    ): SeasonDetails

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
    ): TmdbResponse<Media>
}
