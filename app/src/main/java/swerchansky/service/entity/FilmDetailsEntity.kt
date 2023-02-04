package swerchansky.service.entity

import android.graphics.Bitmap

data class FilmDetailsEntity(
   val nameRu: String,
   val posterUrl: String,
   val year: String?,
   val description: String?,
   val countries: List<CountryEntity>?,
   val genres: List<GenreEntity>?,
   var filmPoster: Bitmap? = null
)