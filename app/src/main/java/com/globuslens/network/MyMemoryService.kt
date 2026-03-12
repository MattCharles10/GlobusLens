package com.globuslens.network

import com.globuslens.network.models.MyMemoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MyMemoryService {

    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String,
        @Query("mt") mt: Int = 1,
        @Query("ie") ie: String = "UTF-8",
        @Query("oe") oe: String = "UTF-8"
    ): MyMemoryResponse
}