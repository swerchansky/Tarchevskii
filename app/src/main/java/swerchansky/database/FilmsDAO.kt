package swerchansky.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FilmsDAO {
   @Insert
   fun insertFilm(film: FilmDBEntity)

   @Query("SELECT EXISTS(SELECT * FROM films WHERE filmId=:filmId)")
   fun isFilmIsExist(filmId: Long): Boolean

   @Query("delete from films where filmId=:filmId")
   fun deleteFilm(filmId: Long)

   @Query("select * from films")
   fun getAllFilms(): List<FilmDBEntity>

   @Query("select * from films where filmId=:filmId")
   fun getFilmItemById(filmId: Long): FilmDBEntity
}