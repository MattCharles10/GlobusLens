package com.globuslens.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.globuslens.database.entities.ShoppingItem
import com.globuslens.ui.components.AddShoppingItemDialog
import com.globuslens.ui.components.EmptyState
import com.globuslens.ui.components.ErrorState
import com.globuslens.ui.components.LoadingState
import com.globuslens.ui.components.ShoppingListItem
import com.globuslens.viewmodel.ShoppingListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    navController: NavController,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDialog by viewModel.showAddItemDialog.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showClearDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ShoppingItem?>(null) }

    // Load shopping list when screen is opened
    LaunchedEffect(Unit) {
        viewModel.loadShoppingList()
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long,
                    actionLabel = "Retry"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.loadShoppingList()
                }
            }
            viewModel.clearError()
        }
    }

    // Handle success messages
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearSuccessMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping List") },
                actions = {
                    if (uiState.items.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true }
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear checked"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAddItemDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    LoadingState(message = "Loading shopping list...")
                }

                uiState.error != null && uiState.items.isEmpty() -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadShoppingList() }
                    )
                }

                uiState.items.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.ShoppingCart,
                        title = "Your Shopping List is Empty",
                        message = "Add items from scanned products or create your own",
                        buttonText = "Add First Item",
                        onButtonClick = { viewModel.toggleAddItemDialog() }
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Total price card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total:",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "$${"%.2f".format(uiState.totalPrice)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 80.dp
                            )
                        ) {
                            items(
                                items = uiState.items,
                                key = { it.id }
                            ) { item ->
                                ShoppingListItem(
                                    item = item,
                                    onCheckedChange = { itemId, isChecked ->
                                        viewModel.toggleItemChecked(itemId, isChecked)
                                    },
                                    onQuantityChange = { itemId, quantity ->
                                        viewModel.updateQuantity(itemId, quantity)
                                    },
                                    onDelete = { shoppingItem ->
                                        itemToDelete = shoppingItem
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Item Dialog
    if (showDialog) {
        AddShoppingItemDialog(
            onDismiss = { viewModel.toggleAddItemDialog() },
            onAdd = { name, quantity, price, notes ->
                viewModel.addItem(name, quantity, price, notes)
            }
        )
    }

    // Clear All Checked Items Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Checked Items") },
            text = { Text("Are you sure you want to remove all checked items from your shopping list?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCheckedItems()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Single Item Confirmation Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Remove Item") },
            text = { Text("Are you sure you want to remove '${item.name}' from your shopping list?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeShoppingItem(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { itemToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}