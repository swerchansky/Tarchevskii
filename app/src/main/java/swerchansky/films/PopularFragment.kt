package swerchansky.films

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class PopularFragment : Fragment() {

   companion object {

      fun newInstance(): PopularFragment {
         return PopularFragment()
      }
   }

   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View? {
      return inflater.inflate(R.layout.popular_fragment, container, false)
   }
}