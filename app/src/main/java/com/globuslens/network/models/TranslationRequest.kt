package com.globuslens.network.models

import com.google.gson.annotations.SerializedName

data class LibreTranslateRequest(
    @SerializedName("q") val text: String,
    @SerializedName("source") val source: String,
    @SerializedName("target") val target: String,
    @SerializedName("format") val format: String = "text",
    @SerializedName("api_key") val apiKey: String? = null
)

data class MyMemoryRequest(
    @SerializedName("q") val text: String,
    @SerializedName("langpair") val langPair: String,
    @SerializedName("de") val email: String? = "your.email@example.com",
    @SerializedName("key") val apiKey: String? = null
)

data class TranslationResponse(
    @SerializedName("translatedText") val translatedText: String,
    @SerializedName("responseStatus") val responseStatus: Int = 200
)

// MyMemory specific
data class MyMemoryResponse(
    @SerializedName("responseData") val responseData: ResponseData,
    @SerializedName("responseStatus") val responseStatus: Int,
    @SerializedName("responseDetails") val responseDetails: String? = null
)

data class ResponseData(
    @SerializedName("translatedText") val translatedText: String,
    @SerializedName("match") val match: Double = 0.0
)