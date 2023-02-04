package swerchansky.service.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import swerchansky.films.ConstantValues.URL
import swerchansky.service.entity.FilmDetailsEntity

class NetworkHelper {
   private val mapper = JsonMapper
      .builder()
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build()
      .registerModule(KotlinModule.Builder().build())
   private val retrofit = Retrofit.Builder()
      .baseUrl(URL)
      .addConverterFactory(JacksonConverterFactory.create(mapper))
      .build()
      .create(APIService::class.java)

   fun getTopFilms(page: Int = 1) = retrofit.getTopFilms(page)

   fun getPreviewImage(url: String): Bitmap {
      val response = retrofit.getImage(url).execute()
      val bytes = response.body()!!.bytes()
      return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
   }

   fun getFilmDetails(id: Int): FilmDetailsEntity {
      val response = retrofit.getFilmDetails(id).execute()
      val filmDetails = response.body()!!
      val imageResponse = retrofit.getImage(filmDetails.posterUrl).execute()
      val bytes = imageResponse.body()!!.bytes()
      filmDetails.filmPoster = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
      return filmDetails
   }
}