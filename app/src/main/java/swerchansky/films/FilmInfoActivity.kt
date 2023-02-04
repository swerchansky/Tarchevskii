package swerchansky.films

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import swerchansky.service.FilmService

class FilmInfoActivity : AppCompatActivity() {
   private val scope = CoroutineScope(Dispatchers.IO)
   private var filmService: FilmService? = null
   private var isBound = false

   private val boundServiceConnection: ServiceConnection = object : ServiceConnection {
      override fun onServiceConnected(name: ComponentName, service: IBinder) {
         val binderBridge: FilmService.MyBinder = service as FilmService.MyBinder
         filmService = binderBridge.getService()
         val position = intent.getIntExtra("filmPosition", -1)
         if (position != -1) {
            scope.launch {
               val image = withContext(Dispatchers.IO) {

               }
            }
         }
         isBound = true
      }

      override fun onServiceDisconnected(name: ComponentName) {
         isBound = false
         filmService = null
      }
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_film_info)
   }
}