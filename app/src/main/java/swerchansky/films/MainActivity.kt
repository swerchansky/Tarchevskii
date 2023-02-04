package swerchansky.films

import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
   private lateinit var popularFragmentButton: Button
   private lateinit var favouriteFragmentButton: Button
   private val popularFragment = PopularFragment()
   private val favouriteFragment = FavouriteFragment()

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      window.setFlags(
         WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
         WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
      )
      setContentView(R.layout.activity_main)

      popularFragmentButton = findViewById(R.id.popularButton)
      favouriteFragmentButton = findViewById(R.id.favouriteButton)

      setFragment(popularFragment)

      popularFragmentButton.setOnClickListener {
         setFragment(popularFragment)
      }

      favouriteFragmentButton.setOnClickListener {
         setFragment(favouriteFragment)
      }
   }

   private fun setFragment(fragment: Fragment) {
      val ft = supportFragmentManager.beginTransaction()
      ft.replace(R.id.fragmentView, fragment)
      ft.commit()
   }

}