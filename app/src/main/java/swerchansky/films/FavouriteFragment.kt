package swerchansky.films

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import swerchansky.films.ConstantValues.FAVOURITE_FILM_DELETED
import swerchansky.films.ConstantValues.FILM_FAVOURITE_LIST_READY
import swerchansky.films.recyclers.FavouriteFilmsAdapter

class FavouriteFragment : Fragment() {
   companion object {
      const val TAG = "FavouriteFragment"
      const val FILM_SERVICE_TAG = "FilmService"
   }

   private lateinit var recycler: RecyclerView
   private lateinit var shimmer: ShimmerFrameLayout

   private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
      @SuppressLint("NotifyDataSetChanged")
      override fun onReceive(context: Context?, intent: Intent) {
         when (intent.getIntExtra("type", -1)) {
            FILM_FAVOURITE_LIST_READY -> {
               shimmer.stopShimmer()
               shimmer.visibility = View.GONE
               recycler.apply {
                  layoutManager = LinearLayoutManager(requireContext())
                  adapter =
                     FavouriteFilmsAdapter(
                        requireActivity(),
                        (activity as MainActivity).filmService!!.favouritesFilms
                     )
               }
            }
            FAVOURITE_FILM_DELETED -> {
               val position = intent.getStringExtra("text")!!.toInt()
               (activity as MainActivity).filmService!!.favouritesFilms.removeAt(position)
               recycler.adapter?.notifyItemRemoved(intent.getStringExtra("text")!!.toInt())
               Handler(Looper.getMainLooper()).postDelayed({
                  recycler.adapter?.notifyDataSetChanged()
               }, 300)
            }
         }
      }
   }

   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View? {
      return inflater.inflate(R.layout.favourite_fragment, container, false)
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)

      recycler = view.findViewById(R.id.favouriteList)
      shimmer = view.findViewById(R.id.shimmerLayout)

      (activity as MainActivity).filmService?.getFavouritesFilms()


      LocalBroadcastManager.getInstance(requireContext())
         .registerReceiver(messageReceiver, IntentFilter(FILM_SERVICE_TAG))
   }

   override fun onDestroy() {
      super.onDestroy()
      LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver)
   }
}