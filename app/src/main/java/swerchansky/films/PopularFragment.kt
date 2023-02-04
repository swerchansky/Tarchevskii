package swerchansky.films

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import swerchansky.films.ConstantValues.FILM_LIST_READY
import swerchansky.films.recyclers.TopFilmsAdapter

class PopularFragment : Fragment() {
   companion object {
      const val TAG = "PopularFragment"
      const val FILM_SERVICE_TAG = "FilmService"
   }

   private lateinit var recycler: RecyclerView

   private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent) {
         when (intent.getIntExtra("type", -1)) {
            FILM_LIST_READY -> {
               recycler.apply {
                  layoutManager = LinearLayoutManager(requireContext())
                  adapter =
                     TopFilmsAdapter(
                        requireActivity(),
                        (activity as MainActivity).filmService!!.films
                     )
               }
            }
         }
      }
   }

   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View? {
      return inflater.inflate(R.layout.popular_fragment, container, false)
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      recycler = view.findViewById(R.id.popularList)
      (activity as MainActivity).filmService?.let {
         recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter =
               TopFilmsAdapter(requireActivity(), (activity as MainActivity).filmService!!.films)
         }
      }
      LocalBroadcastManager.getInstance(requireContext())
         .registerReceiver(messageReceiver, IntentFilter(FILM_SERVICE_TAG))
   }

   override fun onDestroy() {
      super.onDestroy()
      LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver)
   }

}