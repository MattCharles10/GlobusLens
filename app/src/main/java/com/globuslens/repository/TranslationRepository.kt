package com.globuslens.repository

import android.util.Log
import com.globuslens.network.LibreTranslateService
import com.globuslens.network.MyMemoryService
import com.globuslens.network.models.LibreTranslateRequest
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

    private val TAG = "TranslationRepo"

    suspend fun translateText(
        text: String,
        targetLang: String = Constants.DEFAULT_TARGET_LANG
    ): Resource<String> = withContext(Dispatchers.IO) {

        Log.d(TAG, "Translating '$text' to language code: $targetLang")

        // First try offline dictionary for common terms
        val offlineTranslation = translateOffline(text)
        if (offlineTranslation != null) {
            Log.d(TAG, "Offline translation found: $offlineTranslation")
            return@withContext Resource.Success(offlineTranslation)
        }

        // If no internet, return original text
        if (!networkUtils.isNetworkAvailable()) {
            Log.w(TAG, "No internet connection, returning original text")
            return@withContext Resource.Success(text)
        }

        // Try LibreTranslate first (free, no API key)
        Log.d(TAG, "Attempting LibreTranslate...")
        val libreResult = try {
            translateWithLibre(text, targetLang)
        } catch (e: Exception) {
            Log.e(TAG, "LibreTranslate error: ${e.message}")
            null
        }

        if (libreResult != null) {
            Log.d(TAG, "LibreTranslate success: $libreResult")
            return@withContext Resource.Success(libreResult)
        }

        // Fallback to MyMemory (free with email)
        Log.d(TAG, "Attempting MyMemory fallback...")
        return@withContext try {
            val myMemoryResult = translateWithMyMemory(text, targetLang)
            Log.d(TAG, "MyMemory success: $myMemoryResult")
            Resource.Success(myMemoryResult)
        } catch (e: Exception) {
            Log.e(TAG, "MyMemory error: ${e.message}")
            // If all APIs fail, return original text
            Resource.Success(text)
        }
    }

    private fun translateOffline(text: String): String? {
        val lowerText = text.lowercase()

        // Check for exact match first
        Constants.OFFLINE_DICTIONARY[lowerText]?.let {
            return it
        }

        // Check for partial matches (for phrases containing known words)
        return Constants.OFFLINE_DICTIONARY.entries.find { (key, _) ->
            lowerText.contains(key)
        }?.value
    }

    private suspend fun translateWithLibre(text: String, targetLang: String): String? {
        return try {
            val request = LibreTranslateRequest(
                text = text,
                source = "auto",
                target = targetLang,
                format = "text"
            )
            val response = libreTranslateService.translate(request)
            response.translatedText
        } catch (e: IOException) {
            Log.e(TAG, "LibreTranslate IO error: ${e.message}")
            null
        } catch (e: HttpException) {
            Log.e(TAG, "LibreTranslate HTTP error: ${e.code()}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "LibreTranslate unexpected error: ${e.message}")
            null
        }
    }

    private suspend fun translateWithMyMemory(text: String, targetLang: String): String {
        val langPair = "auto|$targetLang"
        val response = myMemoryService.translate(
            text = text,
            langPair = langPair,
            mt = 1,
            ie = "UTF-8",
            oe = "UTF-8"
        )

        // Check if response is valid
        return if (response.responseStatus == 200) {
            response.responseData.translatedText
        } else {
            throw Exception("MyMemory error: ${response.responseDetails ?: "Unknown error"}")
        }
    }

    // Helper function to get supported languages
    fun getSupportedLanguages(): Map<String, String> {
        return Constants.SUPPORTED_LANGUAGES
    }

    // Helper function to validate language code
    fun isValidLanguageCode(code: String): Boolean {
        return Constants.SUPPORTED_LANGUAGES.containsKey(code)
    }
}