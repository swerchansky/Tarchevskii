package swerchansky.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FilmDBEntity::class], version = 1)

abstract class FilmsDatabase : RoomDatabase() {
   abstract fun filmsDAO(): FilmsDAO

   companion object {

      @Volatile
      private var INSTANCE: FilmsDatabase? = null

      fun getDatabase(context: Context): FilmsDatabase {
         if (INSTANCE == null) {
            synchronized(this) {
               INSTANCE = buildDatabase(context)
            }
         }
         return INSTANCE!!
      }

      private fun buildDatabase(context: Context): FilmsDatabase {
         return Room.databaseBuilder(
            context.applicationContext,
            FilmsDatabase::class.java,
            "films"
         ).build()
      }
   }

}