package com.globuslens.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globuslens.camera.TextRecognizer
import com.globuslens.database.entities.Product
import com.globuslens.database.entities.ShoppingItem
import com.globuslens.repository.ProductRepository
import com.globuslens.repository.ShoppingListRepository
import com.globuslens.repository.TranslationRepository
import com.globuslens.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val translationRepository: TranslationRepository,
    private val productRepository: ProductRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val textRecognizer: TextRecognizer
) : ViewModel() {

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _translationResult = MutableStateFlow<Resource<TranslationResponse>>(Resource.Idle)
    val translationResult: StateFlow<Resource<TranslationResponse>> = _translationResult.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun processCapturedImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val text = textRecognizer.recognizeText(bitmap)
                _recognizedText.value = text
                translateText(text)
            } catch (e: Exception) {
                _recognizedText.value = ""
                _translationResult.value = Resource.Error("Failed to recognize text: ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun translateText(text: String) {
        viewModelScope.launch {
            _translationResult.value = Resource.Loading
            try {
                val result = translationRepository.translateText(
                    text = text,
                    sourceLang = "en",
                    targetLang = "ru"
                )
                _translationResult.value = Resource.Success(result)
            } catch (e: Exception) {
                _translationResult.value = Resource.Error("Translation failed: ${e.message}")
            }
        }
    }

    fun saveToFavorites(text: String) {
        viewModelScope.launch {
            val product = Product(
                originalText = text,
                translatedText = _translationResult.value.data?.translatedText ?: "",
                sourceLanguage = "en",
                targetLanguage = "ru",
                timestamp = System.currentTimeMillis(),
                isFavorite = true
            )
            productRepository.insertProduct(product)
        }
    }

    fun addToShoppingList(text: String) {
        viewModelScope.launch {
            val item = ShoppingItem(
                name = text,
                translatedName = _translationResult.value.data?.translatedText ?: "",
                quantity = 1,
                isChecked = false,
                timestamp = System.currentTimeMillis()
            )
            shoppingListRepository.insertItem(item)
        }
    }
}