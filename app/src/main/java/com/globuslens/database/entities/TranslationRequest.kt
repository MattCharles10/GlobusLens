package com.globuslens.database.entities

import com.google.gson.annotations.SerializedName
import okio.Source
import java.text.Format

data class LibreTranslateRequest(
    @SerializedName("q") val text : String ,
    @SerializedName("source") val source: String = "auto",
    @SerializedName("target") val target: String,
    @SerializedName("format") val format: String = "text"
)

data class MyMemoryRequest(
    val q : String,
    val langpair : String,
    val mt : Int =1 ,
    val ie : String = "UTF-8",
    val oe : String = "UTF-8"
)