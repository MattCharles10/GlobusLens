package com.globuslens.network

import com.globuslens.network.models.LibreTranslateRequest
import com.globuslens.network.models.LibreTranslateResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LibreTranslateService {
    @POST("translate")
    suspend fun translate(
        @Body request: LibreTranslateRequest
    ) : LibreTranslateResponse
}