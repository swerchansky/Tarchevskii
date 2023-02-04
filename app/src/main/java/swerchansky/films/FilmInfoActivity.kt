package swerchansky.films

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import swerchansky.service.FilmService

class FilmInfoActivity : AppCompatActivity() {
   private lateinit var filmPoster: ImageView
   private lateinit var fullFilmName: TextView
   private lateinit var filmDescription: TextView
   private lateinit var filmYear: TextView
   private lateinit var filmGenres: TextView
   private lateinit var filmCountries: TextView
   private lateinit var filmServiceIntent: Intent

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
               val filmDetails = withContext(Dispatchers.IO) {
                  filmService!!.getFilmDetails(position)
               }
               if (filmDetails != null) {
                  withContext(Dispatchers.Main) {
                     filmPoster.setImageBitmap(filmDetails.filmPoster)
                     fullFilmName.text = filmDetails.nameRu
                     filmDescription.text = filmDetails.description
                     filmYear.text =
                        filmDetails.year
                           ?: this@FilmInfoActivity.resources.getString(R.string.unknown)
                     filmGenres.text = filmDetails.genres?.joinToString(", ") { it.genre }
                        ?: this@FilmInfoActivity.resources.getString(R.string.unknown)
                     filmCountries.text = filmDetails.countries?.joinToString(", ") { it.country }
                        ?: this@FilmInfoActivity.resources.getString(R.string.unknown)
                  }
               } else {
                  finish()
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
      window.setFlags(
         WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
         WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
      )
      setContentView(R.layout.activity_film_details)

      initViews()

      filmServiceIntent = Intent(this, FilmService::class.java)
      startService(filmServiceIntent)
      bindService(filmServiceIntent, boundServiceConnection, BIND_AUTO_CREATE)
   }

   private fun initViews() {
      filmPoster = findViewById(R.id.filmPoster)
      fullFilmName = findViewById(R.id.fullFilmName)
      filmDescription = findViewById(R.id.filmDescription)
      filmYear = findViewById(R.id.filmYear)
      filmGenres = findViewById(R.id.filmGenres)
      filmCountries = findViewById(R.id.filmCountries)
   }

   override fun onDestroy() {
      super.onDestroy()
      if (isBound) {
         unbindService(boundServiceConnection)
      }
   }
}