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
               //TODO: add error handling
               val result = kotlin.runCatching {
                  networkHelper.getTopFilms(it + 1).execute()
                     .body()?.films?.let { filmList -> films.addAll(filmList) }
               }
            }
            for (i in films.indices) {
               films[i].posterImagePreview =
                  networkHelper.getPreviewImage(films[i].posterUrlPreview)
            }
            val intent = Intent(TAG)
            intent.putExtra("type", FILM_LIST_READY)
            LocalBroadcastManager.getInstance(this@FilmService).sendBroadcast(intent)
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

}