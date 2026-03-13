package com.globuslens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globuslens.database.entities.Product
import com.globuslens.repository.ProductRepository
import com.globuslens.repository.TranslationRepository
import com.globuslens.utils.Constants
import com.globuslens.utils.LanguagePreferences
import com.globuslens.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val translationRepository: TranslationRepository,
    private val languagePreferences: LanguagePreferences  // Add this
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private var lastScanTime = 0L
    private val scanDebounceMs = 2000L

    // Current target language
    private var targetLanguage = Constants.DEFAULT_TARGET_LANG

    init {
        // Load saved language preference
        viewModelScope.launch {
            languagePreferences.targetLanguageFlow.collect { lang ->
                targetLanguage = lang
                _uiState.update {
                    it.copy(targetLanguage = lang)
                }
            }
        }
    }

    fun onTextDetected(text: String) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastScanTime < scanDebounceMs || text.length < Constants.MIN_TEXT_LENGTH) {
            return
        }

        lastScanTime = currentTime

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    detectedText = text,
                    error = null
                )
            }

            try {
                // Translate the detected text using selected target language
                val translationResult = translationRepository.translateText(text, targetLanguage)

                when (translationResult) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                translatedText = translationResult.data,
                                isProcessing = false
                            )
                        }
                        _successMessage.update {
                            "Translated to ${getLanguageName(targetLanguage)}"
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = translationResult.message ?: "Translation failed",
                                isProcessing = false
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isProcessing = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error processing text: ${e.message}",
                        isProcessing = false
                    )
                }
            }
        }
    }

    fun saveProduct() {
        val state = _uiState.value
        val name = state.translatedText ?: state.detectedText

        if (name.isBlank()) {
            _uiState.update { it.copy(error = "No text detected to save") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }

            try {
                val product = Product(
                    name = state.detectedText,
                    translatedName = state.translatedText,
                    scannedDate = Date(),
                    originalLanguage = "auto",
                    targetLanguage = targetLanguage  // Save which language it was translated to
                )

                val result = productRepository.saveProduct(product)

                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                savedProductId = result.data,
                                isProcessing = false
                            )
                        }
                        _successMessage.update { "Product saved successfully" }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to save product",
                                isProcessing = false
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isProcessing = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error saving product: ${e.message}",
                        isProcessing = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _successMessage.update { null }
    }

    fun resetScan() {
        _uiState.update {
            ScannerUiState(targetLanguage = targetLanguage)  // Preserve language
        }
        lastScanTime = 0L
    }

    fun onResultShown() {
        _uiState.update { it.copy(savedProductId = null) }
    }

    private fun getLanguageName(code: String): String {
        return Constants.SUPPORTED_LANGUAGES[code] ?: "English"
    }

    // Optional: Manually set target language (if needed)
    fun setTargetLanguage(languageCode: String) {
        viewModelScope.launch {
            languagePreferences.setTargetLanguage(languageCode)
            // UI will update via the flow collection in init
        }
    }
}

data class ScannerUiState(
    val detectedText: String = "",
    val translatedText: String? = null,
    val isProcessing: Boolean = false,
    val savedProductId: Long? = null,
    val error: String? = null,
    val targetLanguage: String = Constants.DEFAULT_TARGET_LANG  // Add this
)