package com.globuslens.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.globuslens.database.entities.Product
import com.globuslens.ui.components.EmptyState
import com.globuslens.ui.components.ErrorState
import com.globuslens.ui.components.LoadingState
import com.globuslens.ui.components.ProductCard
import com.globuslens.viewmodel.FavoritesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var productToRemove by remember { mutableStateOf<Product?>(null) }

    // Load favorites when screen is opened
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
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
                    viewModel.loadFavorites()
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
                title = { Text("Favorites") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.favorites.isEmpty() -> {
                    LoadingState(message = "Loading favorites...")
                }

                uiState.error != null && uiState.favorites.isEmpty() -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadFavorites() }
                    )
                }

                uiState.favorites.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Favorite,
                        title = "No Favorites Yet",
                        message = "Products you mark as favorite will appear here",
                        buttonText = "Scan Products",
                        onButtonClick = {
                            navController.navigate("scanner") {
                                popUpTo("favorites") { inclusive = false }
                            }
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                    ) {
                        items(
                            items = uiState.favorites,
                            key = { it.id }
                        ) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = { productId ->
                                    navController.navigate("product_detail/$productId")
                                },
                                onFavoriteClick = { productId, isFavorite ->
                                    if (!isFavorite) {
                                        productToRemove = product
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    // Remove from favorites confirmation dialog
    productToRemove?.let { product ->
        AlertDialog(
            onDismissRequest = { productToRemove = null },
            title = { Text("Remove from Favorites") },
            text = { Text("Are you sure you want to remove '${product.name}' from your favorites?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeFromFavorites(product)
                        productToRemove = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productToRemove = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}