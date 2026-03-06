package com.globuslens.repository

import com.globuslens.network.LibreTranslateService
import com.globuslens.network.MyMemoryService
import com.globuslens.database.entities.LibreTranslateRequest
import com.globuslens.utils.Constants
import com.globuslens.utils.NetworkUtils
import com.globuslens.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepository @Inject constructor(
    private val libreTranslateService: LibreTranslateService,
    private val myMemoryService: MyMemoryService,
    private val networkUtils: NetworkUtils
) {

    suspend fun translateText(
        text: String,
        targetLang: String = Constants.DEFAULT_TARGET_LANG
    ): Resource<String> = withContext(Dispatchers.IO) {

        // First try offline dictionary for common terms
        val offlineTranslation = translateOffline(text)
        if (offlineTranslation != null) {
            return@withContext Resource.Success(offlineTranslation)
        }

        // If no internet, return original text
        if (!networkUtils.isNetworkAvailable()) {
            return@withContext Resource.Success(text) // Return original if offline
        }

        // Try LibreTranslate first (free, no API key)
        val libreResult = try {
            translateWithLibre(text, targetLang)
        } catch (e: Exception) {
            null
        }

        if (libreResult != null) {
            return@withContext Resource.Success(libreResult)
        }

        // Fallback to MyMemory (free with email)
        return@withContext try {
            val myMemoryResult = translateWithMyMemory(text, targetLang)
            Resource.Success(myMemoryResult)
        } catch (e: Exception) {
            // If all APIs fail, return original text
            Resource.Success(text)
        }
    }

    private fun translateOffline(text: String): String? {
        val lowerText = text.lowercase()
        return Constants.OFFLINE_DICTIONARY[lowerText] ?:
        Constants.OFFLINE_DICTIONARY.entries.find {
            lowerText.contains(it.key)
        }?.value
    }

    private suspend fun translateWithLibre(text: String, targetLang: String): String? {
        return try {
            val request = LibreTranslateRequest(
                text = text,
                target = targetLang
            )
            val response = libreTranslateService.translate(request)
            response.translatedText
        } catch (e: IOException) {
            null
        } catch (e: HttpException) {
            null
        }
    }

    private suspend fun translateWithMyMemory(text: String, targetLang: String): String {
        val langPair = "auto|$targetLang"
        val response = myMemoryService.translate(
            text = text,
            langPair = langPair,
            mt = 1
        )
        return response.responseData.translatedText
    }
}