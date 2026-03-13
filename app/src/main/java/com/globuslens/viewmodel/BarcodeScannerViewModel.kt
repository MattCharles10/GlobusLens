package com.globuslens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globuslens.database.entities.Product
import com.globuslens.repository.ProductRepository
import com.globuslens.utils.BarcodeLookup
import com.globuslens.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val barcodeLookup: BarcodeLookup
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeScannerUiState())
    val uiState: StateFlow<BarcodeScannerUiState> = _uiState.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun onBarcodeDetected(barcode: String, format: Int) {
        if (_uiState.value.isProcessing) return

        _uiState.update {
            it.copy(
                scannedBarcode = barcode,
                barcodeFormat = format,
                isProcessing = true,
                showManualEntry = false
            )
        }

        lookupBarcode(barcode)
    }

    private fun lookupBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                // First check local database - FIXED: getProductByBarcode is a suspend function, not a Flow
                val existingProduct = withContext(Dispatchers.IO) {
                    productRepository.getProductByBarcode(barcode)
                }

                if (existingProduct != null) {
                    // Product found locally
                    _uiState.update {
                        it.copy(
                            productFound = true,
                            productId = existingProduct.id,
                            isProcessing = false
                        )
                    }
                    _successMessage.update { "Product found in your library" }
                    return@launch
                }

                // Look up online
                val lookupResult = withContext(Dispatchers.IO) {
                    barcodeLookup.lookupBarcode(barcode)
                }

                when (lookupResult) {
                    is Resource.Success -> {
                        lookupResult.data?.let { productInfo ->
                            // Create product from lookup data
                            val product = Product(
                                name = productInfo.productName.ifBlank { "Unknown Product" },
                                barcode = barcode,
                                category = productInfo.category,
                                imageUrl = productInfo.imageUrl,
                                scannedDate = Date()
                            )

                            val saveResult = withContext(Dispatchers.IO) {
                                productRepository.saveProduct(product)
                            }

                            when (saveResult) {
                                is Resource.Success -> {
                                    _uiState.update {
                                        it.copy(
                                            productFound = true,
                                            productId = saveResult.data ?: 0L,
                                            isProcessing = false
                                        )
                                    }
                                    _successMessage.update { "Product found and saved" }
                                }
                                is Resource.Error -> {
                                    _uiState.update {
                                        it.copy(
                                            error = saveResult.message ?: "Failed to save product",
                                            isProcessing = false,
                                            showNotFoundDialog = true
                                        )
                                    }
                                }
                                else -> {}
                            }
                        } ?: run {
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    showNotFoundDialog = true
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                showNotFoundDialog = true,
                                error = lookupResult.message
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error looking up barcode: ${e.message}",
                        isProcessing = false,
                        showNotFoundDialog = true
                    )
                }
            }
        }
    }

    fun lookupManualBarcode() {
        val barcode = _uiState.value.manualBarcode
        if (barcode.isNotBlank()) {
            onBarcodeDetected(barcode, 0)
            toggleManualEntry(false)
        }
    }

    fun toggleFlashlight() {
        _uiState.update { it.copy(flashlightOn = !it.flashlightOn) }
        // Implement flashlight toggle via CameraX
    }

    fun toggleManualEntry(show: Boolean) {
        _uiState.update { it.copy(showManualEntry = show) }
    }

    fun updateManualBarcode(barcode: String) {
        _uiState.update { it.copy(manualBarcode = barcode) }
    }

    fun dismissNotFoundDialog() {
        _uiState.update {
            it.copy(
                showNotFoundDialog = false,
                scannedBarcode = null,
                isProcessing = false
            )
        }
    }

    fun clearBarcode() {
        _uiState.update {
            it.copy(
                scannedBarcode = null,
                isProcessing = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun retry() {
        _uiState.value.scannedBarcode?.let { barcode ->
            lookupBarcode(barcode)
        }
    }

    fun clearSuccessMessage() {
        _successMessage.update { null }
    }
}

data class BarcodeScannerUiState(
    val scannedBarcode: String? = null,
    val barcodeFormat: Int = 0,
    val isProcessing: Boolean = false,
    val productFound: Boolean = false,
    val productId: Long = 0L,
    val error: String? = null,
    val flashlightOn: Boolean = false,
    val showManualEntry: Boolean = false,
    val manualBarcode: String = "",
    val showNotFoundDialog: Boolean = false
)