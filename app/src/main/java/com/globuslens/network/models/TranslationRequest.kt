package com.globuslens.network.models

import com.google.gson.annotations.SerializedName

data class LibreTranslateRequest(
    @SerializedName("q") val text : String ,
    @SerializedName("source") val source: String = "auto",
    @SerializedName("target") val target: String,
    @SerializedName("format") val format: String = "text",
    @SerializedName("api_key") val apiKey: String? = null
)

data class MyMemoryRequest(
    val q : String,
    val langpair : String,
    val mt : Int =1 ,
    val ie : String = "UTF-8",
    val oe : String = "UTF-8"
)