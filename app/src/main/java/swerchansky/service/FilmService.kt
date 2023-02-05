package swerchansky.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import swerchansky.database.FilmDBEntity
import swerchansky.database.FilmsDatabase
import swerchansky.films.ConstantValues.DELETE_FAVOURITE_FILM_REQUEST
import swerchansky.films.ConstantValues.FAVOURITE_FILM_WAS_DELETED
import swerchansky.films.ConstantValues.FILM_FAVOURITE_CHANGED
import swerchansky.films.ConstantValues.FILM_FAVOURITE_LIST_READY
import swerchansky.films.ConstantValues.FILM_TOP_LIST_READY
import swerchansky.films.ConstantValues.NETWORK_FAILURE
import swerchansky.films.ConstantValues.SAVE_OR_DELETE_FAVOURITE_FILM
import swerchansky.films.R
import swerchansky.service.entity.CountryEntity
import swerchansky.service.entity.FilmDetailsEntity
import swerchansky.service.entity.FilmEntity
import swerchansky.service.entity.GenreEntity
import swerchansky.service.network.NetworkHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class FilmService : Service() {
   companion object {
      const val TAG = "FilmService"
      const val MAIN_ACTIVITY_TAG = "MainActivity"
   }

   private val networkHelper = NetworkHelper()
   private val scope = CoroutineScope(Dispatchers.IO)
   private val filmsDatabase by lazy {
      FilmsDatabase.getDatabase(this).filmsDAO()
   }
   val topFilms = mutableListOf<FilmEntity>()
   val favouritesFilms = mutableListOf<FilmEntity>()

   private var getFilmsListJob: Job? = null

   private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent) {
         when (intent.getIntExtra("type", -1)) {
            SAVE_OR_DELETE_FAVOURITE_FILM -> {
               addOrDeleteFavouriteFilm(
                  intent.getIntExtra(
                     "filmId", -1
                  )
               )
            }
            DELETE_FAVOURITE_FILM_REQUEST -> {
               scope.launch {
                  withContext(Dispatchers.IO) {
                     deleteFavouriteFilm(
                        intent.getIntExtra(
                           "filmId", -1
                        )
                     )
                     sendIntent(
                        FAVOURITE_FILM_WAS_DELETED,
                        intent.getIntExtra("filmId", -1).toString()
                     )
                  }
               }
            }
         }
      }
   }

   override fun onCreate() {
      super.onCreate()
      getFilmsList()
      LocalBroadcastManager.getInstance(this)
         .registerReceiver(messageReceiver, IntentFilter(MAIN_ACTIVITY_TAG))
   }

   override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

   override fun onBind(intent: Intent?): IBinder {
      return MyBinder()
   }

   inner class MyBinder : Binder() {
      fun getService() = this@FilmService
   }

   override fun onUnbind(intent: Intent?): Boolean {
      return super.onUnbind(intent)
   }

   override fun onDestroy() {
      super.onDestroy()
      scope.cancel()
      LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
   }

   fun getFilmTopDetails(filmId: Int): FilmDetailsEntity? {
      val (filmDetails, filmDetailsCode, filmPosterCode) =
         try {
            networkHelper.getFilmDetails(filmId)
         } catch (e: Exception) {
            sendIntent(NETWORK_FAILURE)
            return null
         }
      if (filmDetailsCode != 200 && filmPosterCode != 200) {
         sendIntent(NETWORK_FAILURE)
         return null
      }
      return filmDetails
   }

   fun getFilmFavouriteDetails(filmId: Int): FilmDetailsEntity {
      return filmsDatabase.getFilmItemById(filmId.toLong())
         .toFilmDetailsEntity()
   }

   fun getFilmsList() {
      getFilmsListJob = scope.launch {
         withContext(Dispatchers.IO) {
            repeat(5) {
               val result = kotlin.runCatching {
                  networkHelper.getTopFilms(it + 1).execute()
                     .body()?.films?.let { filmList -> topFilms += filmList }
               }
               if (result.isFailure) {
                  sendIntent(NETWORK_FAILURE)
                  return@withContext
               }
            }
            if (getFilmsPreviewPosters()) return@withContext
            sendIntent(FILM_TOP_LIST_READY)
         }
      }
   }

   fun isFilmsListJobWork(): Boolean {
      return getFilmsListJob?.isActive ?: false
   }

   fun addOrDeleteFavouriteFilm(filmId: Int) {
      scope.launch {
         withContext(Dispatchers.IO) {
            if (filmsDatabase.isFilmIsExist(filmId.toLong())) {
               deleteFavouriteFilm(filmId)
            } else {
               addFavouriteFilm(filmId)
            }
            sendIntent(FILM_FAVOURITE_CHANGED, filmId.toString())
         }
      }
   }

   fun getFavouritesFilms(wait: Boolean = false) {
      val currentScope = scope.launch {
         withContext(Dispatchers.IO) {
            favouritesFilms.clear()
            filmsDatabase.getAllFilms()
               .let { it.forEach { film -> favouritesFilms += film.toFilmEntity() } }
            sendIntent(FILM_FAVOURITE_LIST_READY)
         }
      }
      if (wait) runBlocking { currentScope.join() }
   }

   private suspend fun addFavouriteFilm(filmId: Int) {
      val (filmDetails, filmDetailsCode, filmPosterCode) =
         try {
            networkHelper.getFilmDetails(filmId)
         } catch (e: Exception) {
            sendIntent(NETWORK_FAILURE)
            return
         }
      if (filmDetailsCode != 200 && filmPosterCode != 200 && filmDetails == null) {
         sendIntent(NETWORK_FAILURE)
         return
      }

      filmsDatabase.insertFilm(filmDetails!!.toFilmDBEntity())
      writeImageToCache(filmDetails.filmPoster, filmDetails.kinopoiskId)
      withContext(Dispatchers.Main) {
         sendToast(
            this@FilmService.resources.getString(R.string.addedToFavourites),
            this@FilmService
         )
      }
   }

   private suspend fun deleteFavouriteFilm(filmId: Int) {
      filmsDatabase.deleteFilm(filmId.toLong())
      deleteImageFromCache(filmId.toLong())
      withContext(Dispatchers.Main) {
         sendToast(
            this@FilmService.resources.getString(R.string.deletedFromFavourites),
            this@FilmService
         )
      }
   }

   private fun getFilmsPreviewPosters(): Boolean {
      for (i in topFilms.indices) {
         val (filmPoster, filmPosterCode) =
            try {
               networkHelper.getPreviewImage(topFilms[i].posterUrlPreview)
            } catch (e: Exception) {
               sendIntent(NETWORK_FAILURE)
               return true
            }
         if (filmPosterCode != 200) {
            sendIntent(NETWORK_FAILURE)
            return true
         }
         topFilms[i].posterImagePreview =
            filmPoster
      }
      return false
   }


   private fun deleteImageFromCache(imageId: Long) {
      val file =
         File(this@FilmService.cacheDir, "$imageId.png")
      if (file.exists()) {
         file.delete()
      }
   }

   private fun writeImageToCache(image: Bitmap?, imageId: Long) {
      image ?: return
      val file =
         File(this@FilmService.cacheDir, "$imageId.png").also { it.createNewFile() }
      val bos = ByteArrayOutputStream()
      image.compress(Bitmap.CompressFormat.PNG, 0, bos)
      val bitmapData = bos.toByteArray()
      FileOutputStream(file).use {
         with(it) {
            write(bitmapData)
            flush()
         }
      }
   }

   private fun getImageFromCache(imageId: Long): Bitmap? {
      return try {
         val file = File(cacheDir, "$imageId.png")
         if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
         } else {
            null
         }
      } catch (e: Exception) {
         null
      }
   }

   private fun sendIntent(type: Int, text: String = "") {
      val intent = Intent(TAG)
      intent.putExtra("type", type)
      intent.putExtra("text", text)
      LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
   }

   private fun sendToast(message: String, context: Context, time: Long = 1000) {
      val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
      object : CountDownTimer(time, 200) {
         override fun onTick(millisUntilFinished: Long) {
            toast.show()
         }

         override fun onFinish() {
            toast.cancel()
         }
      }.start()
   }

   private fun FilmDBEntity.toFilmEntity() = FilmEntity(
      filmId = filmId.toInt(),
      nameRu = nameRu,
      year = year,
      posterUrlPreview = "",
      posterImagePreview = getImageFromCache(filmId),
      genres = genres.split(":").map { GenreEntity(it) },
   )

   private fun FilmDBEntity.toFilmDetailsEntity() = FilmDetailsEntity(
      kinopoiskId = filmId,
      nameRu = nameRu,
      posterUrl = posterUrl,
      description = description,
      year = year,
      countries = countries?.split(":")?.map { CountryEntity(it) },
      genres = genres.split(":").map { GenreEntity(it) },
      filmPoster = getImageFromCache(filmId),
   )

   private fun FilmDetailsEntity.toFilmDBEntity() = FilmDBEntity(
      filmId = kinopoiskId,
      nameRu = nameRu,
      posterUrl = posterUrl,
      description = description,
      year = year,
      countries = countries?.joinToString(":") { it.country },
      genres = genres.joinToString(":") { it.genre },
   )
}