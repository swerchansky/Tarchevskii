package swerchansky.films

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import swerchansky.service.FilmService

class MainActivity : AppCompatActivity() {
   companion object {
      const val TAG = "MainActivity"
      const val MESSAGE_SERVICE_TAG = "FilmService"
   }

   private lateinit var popularFragmentButton: Button
   private lateinit var favouriteFragmentButton: Button
   private lateinit var filmServiceIntent: Intent
   private val popularFragment = PopularFragment()
   private val favouriteFragment = FavouriteFragment()
   private var filmService: FilmService? = null
   private var isBound = false

   private val boundServiceConnection: ServiceConnection = object : ServiceConnection {
      override fun onServiceConnected(name: ComponentName, service: IBinder) {
         val binderBridge = service as FilmService.MyBinder
         filmService = binderBridge.getService()
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
      setContentView(R.layout.activity_main)

      popularFragmentButton = findViewById(R.id.popularButton)
      favouriteFragmentButton = findViewById(R.id.favouriteButton)

      initFragments()

      filmServiceIntent = Intent(this, FilmService::class.java)
      startService(filmServiceIntent)
      bindService(filmServiceIntent, boundServiceConnection, BIND_AUTO_CREATE)
   }

   private fun setFragment(fragment: Fragment) {
      val ft = supportFragmentManager.beginTransaction()
      ft.replace(R.id.fragmentView, fragment)
      ft.commit()
   }

   private fun initFragments() {
      setFragment(popularFragment)

      popularFragmentButton.setOnClickListener {
         setFragment(popularFragment)
      }

      favouriteFragmentButton.setOnClickListener {
         setFragment(favouriteFragment)
      }
   }

}