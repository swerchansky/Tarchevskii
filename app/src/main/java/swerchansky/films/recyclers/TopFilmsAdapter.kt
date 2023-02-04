package swerchansky.films.recyclers


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import swerchansky.films.FilmInfoActivity
import swerchansky.films.R
import swerchansky.service.entity.FilmEntity


class TopFilmsAdapter(private val context: Context, private val films: List<FilmEntity>) :
   RecyclerView.Adapter<RecyclerView.ViewHolder>() {


   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      return ViewHolder(
         LayoutInflater.from(context).inflate(R.layout.film_card, parent, false)
      )
   }

   override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      val film = films[position]
      val viewHolder = holder as ViewHolder
      viewHolder.filmName.text = film.nameRu
      viewHolder.filmAdditional.text = context.resources.getString(
         R.string.film_additional,
         film.genres.first().genre,
         film.year ?: "..."
      )
      viewHolder.filmPoster.setImageBitmap(film.posterImagePreview)
      viewHolder.filmCard.setOnClickListener {
         val intent = Intent(context, FilmInfoActivity::class.java)
         intent.putExtra("filmPosition", position)
         context.startActivity(intent)
      }
   }

   override fun getItemCount() = films.size

   class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val filmPoster: ShapeableImageView = view.findViewById(R.id.filmPoster)
      val filmName: TextView = view.findViewById(R.id.filmName)
      val filmAdditional: TextView = view.findViewById(R.id.filmAdditional)
      val filmCard: CardView = view.findViewById(R.id.filmCard)
   }

}