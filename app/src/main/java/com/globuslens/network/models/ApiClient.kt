package com.globuslens.network.models

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // LibreTranslate public instances (completely free, no API key)
    const val LIBRE_TRANSLATE_1 = "https://libretranslate.com/"
    const val LIBRE_TRANSLATE_2 = "https://translate.argosopentech.com/"
    const val LIBRE_TRANSLATE_3 = "https://translator.siliconsprawl.com/"

    // MyMemory API (free with email)
    const val MY_MEMORY_API = "https://api.mymemory.translated.net/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // LibreTranslate Retrofit instance
    fun getLibreTranslateService(baseUrl: String = LIBRE_TRANSLATE_1): LibreTranslateService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LibreTranslateService::class.java)
    }

    // MyMemory Retrofit instance
    val myMemoryService: MyMemoryService by lazy {
        Retrofit.Builder()
            .baseUrl(MY_MEMORY_API)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyMemoryService::class.java)
    }
}