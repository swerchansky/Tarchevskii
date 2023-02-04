package swerchansky.films

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class FavouriteFragment : Fragment() {

   companion object {

      fun newInstance(): FavouriteFragment {
         return FavouriteFragment()
      }
   }

   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View? {
      return inflater.inflate(R.layout.favourite_fragment, container, false)
   }
}