package com.globuslens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globuslens.database.entities.ShoppingItem
import com.globuslens.repository.ShoppingListRepository
import com.globuslens.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private val _showAddItemDialog = MutableStateFlow(false)
    val showAddItemDialog: StateFlow<Boolean> = _showAddItemDialog.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadShoppingList()
    }

    fun loadShoppingList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            shoppingListRepository.getAllShoppingItems().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val items = resource.data ?: emptyList()
                        val total = items.filter { !it.isChecked }
                            .sumOf { it.price?.times(it.quantity) ?: 0.0 }

                        _uiState.update {
                            it.copy(
                                items = items,
                                totalPrice = total,
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
                                error = resource.message ?: "Failed to load shopping list",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun toggleAddItemDialog() {
        _showAddItemDialog.update { !it }
    }

    fun addItem(name: String, quantity: Int, price: Double?, notes: String?) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Item name cannot be empty") }
            return
        }

        if (quantity <= 0) {
            _uiState.update { it.copy(error = "Quantity must be greater than 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val item = ShoppingItem(
                name = name,
                quantity = quantity,
                price = price,
                notes = notes
            )

            val result = shoppingListRepository.addShoppingItem(item)

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _successMessage.update { "Item added successfully" }
                    toggleAddItemDialog()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message ?: "Failed to add item",
                            isLoading = false
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun toggleItemChecked(itemId: Long, isChecked: Boolean) {
        viewModelScope.launch {
            try {
                shoppingListRepository.toggleItemChecked(itemId, isChecked)
                updateTotalPrice()
                _successMessage.update {
                    if (isChecked) "Item marked as done" else "Item unmarked"
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update item: ${e.message}") }
            }
        }
    }

    fun updateQuantity(itemId: Long, quantity: Int) {
        if (quantity <= 0) {
            _uiState.update { it.copy(error = "Quantity must be greater than 0") }
            return
        }

        viewModelScope.launch {
            try {
                val currentItems = _uiState.value.items
                val item = currentItems.find { it.id == itemId } ?: return@launch

                val updatedItem = item.copy(quantity = quantity)
                val result = shoppingListRepository.updateShoppingItem(updatedItem)

                when (result) {
                    is Resource.Success -> {
                        updateTotalPrice()
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update quantity: ${e.message}") }
            }
        }
    }

    // Renamed from deleteItem to removeShoppingItem for better clarity
    fun removeShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val result = shoppingListRepository.deleteShoppingItem(item)

                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _successMessage.update { "'${item.name}' removed from list" }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to remove item",
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
                        error = "Failed to remove item: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearCheckedItems() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val result = shoppingListRepository.clearCheckedItems()

                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _successMessage.update { "Checked items cleared" }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to clear items",
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
                        error = "Failed to clear items: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun updateTotalPrice() {
        val currentItems = _uiState.value.items
        val total = currentItems.filter { !it.isChecked }
            .sumOf { it.price?.times(it.quantity) ?: 0.0 }
        _uiState.update { it.copy(totalPrice = total) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _successMessage.update { null }
    }
}

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)