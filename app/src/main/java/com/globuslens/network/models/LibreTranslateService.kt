package com.globuslens.network.models


import com.globuslens.network.models.LibreTranslateRequest
import com.globuslens.network.models.TranslationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LibreTranslateService {
    @POST("translate")
    suspend fun translateText(
        @Body request: LibreTranslateRequest
    ): TranslationResponse
}