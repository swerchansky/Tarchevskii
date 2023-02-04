package swerchansky.service.network

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import swerchansky.films.ConstantValues.URL

class NetworkHelper {
   private val mapper = JsonMapper
      .builder()
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
      .serializationInclusion(JsonInclude.Include.NON_NULL)
      .build()
      .registerModule(KotlinModule.Builder().build())
   private val retrofit = Retrofit.Builder()
      .baseUrl(URL)
      .addConverterFactory(JacksonConverterFactory.create(mapper))
      .build()
      .create(APIService::class.java)
}