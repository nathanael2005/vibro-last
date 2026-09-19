package com.nate.app.data.api

data class GenreOption(val id: Int, val label: String)

object TmdbGenres {
  val movieGenres: List<GenreOption> = listOf(
      GenreOption(28, "Action"),
      GenreOption(35, "Comedy"),
      GenreOption(878, "Sci-Fi"),
      GenreOption(18, "Drama"),
      GenreOption(27, "Horror"),
      GenreOption(16, "Animation"),
      GenreOption(53, "Thriller"),
      GenreOption(12, "Adventure"),
  )

  fun movieGenreId(label: String): Int? =
      movieGenres.firstOrNull { it.label.equals(label, ignoreCase = true) }?.id

  fun tvGenreId(label: String): Int? = movieGenreId(label)
}
