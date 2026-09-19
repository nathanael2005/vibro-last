package com.nate.core.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class TmdbMediaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>?
)

data class TmdbResponse<T>(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<T>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)

data class TmdbDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("genres") val genres: List<TmdbGenreDto>?,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int?,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int?,
    @SerializedName("seasons") val seasons: List<TmdbSeasonInfoDto>?
)

data class TmdbSeasonInfoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("episode_count") val episodeCount: Int?,
    @SerializedName("poster_path") val posterPath: String?
)

data class TmdbSeasonDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("episodes") val episodes: List<TmdbEpisodeDto>?
)

data class TmdbEpisodeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("episode_number") val episodeNumber: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("still_path") val stillPath: String?,
    @SerializedName("air_date") val airDate: String?,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("vote_average") val voteAverage: Double?
)

data class TmdbGenreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

interface MediaApiService {

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("trending/tv/day")
    suspend fun getTrendingTv(
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("tv/popular")
    suspend fun getPopularTv(
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("tv/airing_today")
    suspend fun getAiringTodayTv(
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("movie/{id}")
    suspend fun getMovieDetail(
        @Path("id") id: String
    ): TmdbDetailDto

    @GET("tv/{id}")
    suspend fun getTvDetail(
        @Path("id") id: String
    ): TmdbDetailDto

    @GET("tv/{id}/season/{season_number}")
    suspend fun getTvSeasonDetail(
        @Path("id") id: String,
        @Path("season_number") seasonNumber: Int
    ): TmdbSeasonDetailDto

    @GET("movie/{id}/similar")
    suspend fun getSimilarMovies(
        @Path("id") id: String
    ): TmdbResponse<TmdbMediaDto>

    @GET("tv/{id}/similar")
    suspend fun getSimilarTv(
        @Path("id") id: String
    ): TmdbResponse<TmdbMediaDto>

    @GET("discover/movie")
    suspend fun getDiscoverMovies(
        @Query("with_genres") genreId: String?,
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>

    @GET("discover/tv")
    suspend fun getDiscoverTv(
        @Query("with_genres") genreId: String?,
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMediaDto>
}
