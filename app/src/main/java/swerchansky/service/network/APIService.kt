package swerchansky.service.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import swerchansky.films.ConstantValues.API_KEY
import swerchansky.films.ConstantValues.TOP_TYPE
import swerchansky.service.entity.FilmDetailsEntity
import swerchansky.service.entity.PageEntity

interface APIService {
   @Headers("X-API-KEY: $API_KEY")
   @GET("api/v2.2/films/top")
   fun getTopFilms(
      @Query("page") page: Int,
      @Query("type") type: String = TOP_TYPE
   ): Call<PageEntity>

   @GET
   fun getImage(@Url url: String): Call<ResponseBody>

   @Headers("X-API-KEY: $API_KEY")
   @GET("api/v2.2/films/{id}")
   fun getFilmDetails(@Path("id") id: Int): Call<FilmDetailsEntity>
}