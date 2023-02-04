package swerchansky.service.network

import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
   @GET("1ch")
   fun getLastMessages(
      @Query("lastKnownId") lastKnownId: Long,
      @Query("limit") limit: Long
   )


}