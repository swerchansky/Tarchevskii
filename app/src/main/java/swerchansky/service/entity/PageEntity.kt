package swerchansky.service.entity

import android.graphics.Bitmap

data class PageEntity(
   val pagesCount: Int,
   val films: List<FilmEntity>
)

data class FilmEntity(
   val filmId: Int,
   val nameRu: String,
   val year: String?,
   val genres: List<GenreEntity>,
   val posterUrlPreview: String,
   var posterImagePreview: Bitmap? = null,
)

data class GenreEntity(
   val genre: String
)