package com.globuslens.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globuslens.database.entities.Product
import com.globuslens.database.entities.ShoppingItem
import com.globuslens.repository.ProductRepository
import com.globuslens.repository.ShoppingListRepository
import com.globuslens.repository.TranslationRepository
import com.globuslens.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val translationRepository: TranslationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadProduct(productId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                productRepository.getProductById(productId).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    product = resource.data,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    error = resource.message ?: "Failed to load product",
                                    isLoading = false,
                                    product = null
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error loading product: ${e.message}",
                        isLoading = false,
                        product = null
                    )
                }
            }
        }
    }

    fun toggleFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            val currentProduct = _uiState.value.product
            if (currentProduct == null) {
                _uiState.update { it.copy(error = "Product not found") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = productRepository.toggleFavorite(currentProduct.id, isFavorite)

                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                product = currentProduct.copy(isFavorite = isFavorite),
                                isLoading = false
                            )
                        }
                        _successMessage.update {
                            if (isFavorite) "Added to favorites" else "Removed from favorites"
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to update favorite",
                                isLoading = false
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error updating favorite: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun translateProduct(text: String, targetLang: String) {
        viewModelScope.launch {
            val currentProduct = _uiState.value.product
            if (currentProduct == null) {
                _uiState.update { it.copy(error = "Product not found") }
                return@launch
            }

            _uiState.update { it.copy(isTranslating = true, error = null) }

            try {
                val result = translationRepository.translateText(text, targetLang)

                when (result) {
                    is Resource.Success -> {
                        val updatedProduct = currentProduct.copy(
                            translatedName = result.data,
                            originalLanguage = "auto"
                        )

                        val updateResult = productRepository.updateProduct(updatedProduct)

                        when (updateResult) {
                            is Resource.Success -> {
                                _uiState.update {
                                    it.copy(
                                        product = updatedProduct,
                                        isTranslating = false
                                    )
                                }
                                _successMessage.update { "Translation completed" }
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        error = updateResult.message ?: "Failed to save translation",
                                        isTranslating = false
                                    )
                                }
                            }
                            else -> {
                                _uiState.update { it.copy(isTranslating = false) }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Translation failed",
                                isTranslating = false
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isTranslating = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Translation error: ${e.message}",
                        isTranslating = false
                    )
                }
            }
        }
    }

    fun addToShoppingList(product: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val shoppingItem = ShoppingItem(
                    productId = product.id,
                    name = product.name,
                    quantity = 1,
                    price = product.price,
                    notes = "From scanned product",
                    category = product.category
                )

                val result = shoppingListRepository.addShoppingItem(shoppingItem)

                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _successMessage.update { "Added to shopping list" }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to add to shopping list",
                                isLoading = false
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error adding to shopping list: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateProduct(updatedProduct: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = productRepository.updateProduct(updatedProduct)

                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                product = updatedProduct,
                                isLoading = false
                            )
                        }
                        _successMessage.update { "Product updated" }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to update product",
                                isLoading = false
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error updating product: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteProduct() {
        viewModelScope.launch {
            val currentProduct = _uiState.value.product
            if (currentProduct == null) {
                _uiState.update { it.copy(error = "Product not found") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = productRepository.deleteProduct(currentProduct)

                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                product = null,
                                isLoading = false
                            )
                        }
                        _successMessage.update { "Product deleted" }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to delete product",
                                isLoading = false
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error deleting product: ${e.message}",
                        isLoading = false
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
}

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val isTranslating: Boolean = false,
    val error: String? = null
)