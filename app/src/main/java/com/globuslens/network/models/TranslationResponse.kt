package com.globuslens.database.entities

import com.google.gson.annotations.SerializedName

data class LibreTranslateResponse(
    @SerializedName("translatedText") val translatedText: String,

)

data class MyMemoryResponse(
    @SerializedName("responseData") val responseData: ResponseData,
    @SerializedName("responseStatus") val responseStatus: Int,
    @SerializedName("responseDetails") val responseDetails: String? = null
)

data class ResponseData(
    @SerializedName("translatedText") val translatedText: String,
    @SerializedName("match") val match: Double = 1.0
)
