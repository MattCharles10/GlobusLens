package com.globuslens.network.models

import com.globuslens.network.models.MyMemoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MyMemoryService {
    @GET("get")
    suspend fun translateText(
        @Query("q") text: String,
        @Query("langpair") langPair: String,
        @Query("de") email: String? = "your.email@example.com",
        @Query("key") apiKey: String? = null,
        @Query("mt") mt: Int = 1,  // Include machine translation
        @Query("onlyprivate") onlyPrivate: Int = 0
    ): MyMemoryResponse
}