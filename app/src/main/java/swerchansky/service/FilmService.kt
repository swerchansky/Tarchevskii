package swerchansky.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class FilmService : Service() {
   companion object {
      const val TAG = "FilmService"
      const val MAIN_ACTIVITY_TAG = "MainActivity"
   }

   override fun onCreate() {
      super.onCreate()
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