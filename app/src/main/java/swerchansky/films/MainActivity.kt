package swerchansky.films

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import swerchansky.films.ConstantValues.FAILURE_FRAGMENT_TAG
import swerchansky.films.ConstantValues.FAVOURITE_FRAGMENT_TAG
import swerchansky.films.ConstantValues.FILM_TOP_LIST_READY
import swerchansky.films.ConstantValues.NETWORK_FAILURE
import swerchansky.films.ConstantValues.POPULAR_FRAGMENT_TAG
import swerchansky.service.FilmService


class MainActivity : AppCompatActivity() {
   companion object {
      const val TAG = "MainActivity"
      const val FILM_SERVICE_TAG = "FilmService"
   }

   private lateinit var popularFragmentButton: AppCompatButton
   private lateinit var favouriteFragmentButton: AppCompatButton
   private lateinit var filmServiceIntent: Intent
   private val popularFragment = PopularFragment()
   private val favouriteFragment = FavouriteFragment()
   private val failureFragment = FailureFragment()
   private var isBound = false
   var filmsTopListReady = false
   var filmService: FilmService? = null

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

   private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent) {
         when (intent.getIntExtra("type", -1)) {
            NETWORK_FAILURE -> {
               setFragment(failureFragment, FAILURE_FRAGMENT_TAG)
            }
            FILM_TOP_LIST_READY -> {
               filmsTopListReady = true
            }
         }
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
      LocalBroadcastManager.getInstance(this)
         .registerReceiver(messageReceiver, IntentFilter(FILM_SERVICE_TAG))
   }

   override fun onDestroy() {
      super.onDestroy()
      if (isBound) {
         unbindService(boundServiceConnection)
      }
      LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
   }

   fun restart() {
      if (filmsTopListReady) {
         setFragment(popularFragment, POPULAR_FRAGMENT_TAG)
         return
      }
      filmService?.getFilmsList()
      setFragment(popularFragment, POPULAR_FRAGMENT_TAG)
   }

   private fun inverseButtonColor(button: AppCompatButton) {
      if (button.textColors.defaultColor == ContextCompat.getColor(
            this,
            R.color.light_blue_white
         )
      ) {
         button.setTextColor(ContextCompat.getColor(this, R.color.light_blue_teal))
         button.background = ContextCompat.getDrawable(this, R.drawable.active_button_background)
      } else {
         button.setTextColor(ContextCompat.getColor(this, R.color.light_blue_white))
         button.background = ContextCompat.getDrawable(this, R.drawable.inactive_button_background)
      }
   }

   private fun setFragment(fragment: Fragment, fragmentTag: String) {
      val ft = supportFragmentManager.beginTransaction()
      ft.replace(R.id.fragmentView, fragment, fragmentTag)
      ft.commit()
   }

   private fun initFragments() {
      setFragment(popularFragment, POPULAR_FRAGMENT_TAG)

      popularFragmentButton.setOnClickListener {
         val myFragment: FailureFragment? =
            supportFragmentManager.findFragmentByTag(FAILURE_FRAGMENT_TAG) as FailureFragment?
         if (myFragment?.isVisible == true) {
            return@setOnClickListener
         }
         if (filmService?.isFilmsListJobWork() == false && !filmsTopListReady) {
            setFragment(failureFragment, FAILURE_FRAGMENT_TAG)
         } else {
            if ((supportFragmentManager.findFragmentByTag(POPULAR_FRAGMENT_TAG) as PopularFragment?)
                  ?.isVisible != true
            ) {
               inverseButtonColor(popularFragmentButton)
               inverseButtonColor(favouriteFragmentButton)
            }
            setFragment(popularFragment, POPULAR_FRAGMENT_TAG)
         }
      }

      favouriteFragmentButton.setOnClickListener {
         if ((supportFragmentManager.findFragmentByTag(FAVOURITE_FRAGMENT_TAG) as FavouriteFragment?)
               ?.isVisible != true
         ) {
            inverseButtonColor(popularFragmentButton)
            inverseButtonColor(favouriteFragmentButton)
         }
         setFragment(favouriteFragment, FAVOURITE_FRAGMENT_TAG)
      }
   }

}