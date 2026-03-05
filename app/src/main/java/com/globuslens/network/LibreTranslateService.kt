package com.globuslens.network

import com.globuslens.database.entities.LibreTranslateRequest
import com.globuslens.database.entities.LibreTranslateResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LibreTranslateService {
    @POST("translate")
    suspend fun translate(
        @Body request: LibreTranslateRequest
    ) : LibreTranslateResponse
}