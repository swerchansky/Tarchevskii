package swerchansky.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "films")
class FilmDBEntity(
   @PrimaryKey val filmId: Long,
   val nameRu: String,
   val posterUrl: String,
   val year: String?,
   val description: String?,
   val countries: String?,
   val genres: String,
)