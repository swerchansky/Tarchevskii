package swerchansky.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import swerchansky.films.ConstantValues.FILM_LIST_READY
import swerchansky.films.ConstantValues.NETWORK_FAILURE
import swerchansky.service.entity.FilmDetailsEntity
import swerchansky.service.entity.FilmEntity
import swerchansky.service.network.NetworkHelper

class FilmService : Service() {
   companion object {
      const val TAG = "FilmService"
      const val MAIN_ACTIVITY_TAG = "MainActivity"
   }

   private val networkHelper = NetworkHelper()
   val films = mutableListOf<FilmEntity>()
   private val scope = CoroutineScope(Dispatchers.IO)

   override fun onCreate() {
      super.onCreate()
      scope.launch {
         withContext(Dispatchers.IO) {
            repeat(5) {
               val result = kotlin.runCatching {
                  networkHelper.getTopFilms(it + 1).execute()
                     .body()?.films?.let { filmList -> films += filmList }
               }
               if (result.isFailure) {
                  sendIntent(NETWORK_FAILURE)
                  return@withContext
               }
            }
            for (i in films.indices) {
               val (filmPoster, filmPosterCode) =
                  try {
                     networkHelper.getPreviewImage(films[i].posterUrlPreview)
                  } catch (e: Exception) {
                     sendIntent(NETWORK_FAILURE)
                     return@withContext
                  }
               if (filmPosterCode != 200) {
                  sendIntent(NETWORK_FAILURE)
                  return@withContext
               }
               films[i].posterImagePreview =
                  filmPoster
            }
            sendIntent(FILM_LIST_READY)
         }
      }
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
   }

   fun getFilmDetails(position: Int): FilmDetailsEntity? {
      val (filmDetails, filmDetailsCode, filmPosterCode) =
         try {
            networkHelper.getFilmDetails(films[position].filmId)
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

   private fun sendIntent(type: Int, text: String = "") {
      val intent = Intent(TAG)
      intent.putExtra("type", type)
      intent.putExtra("text", text)
      LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
   }

}