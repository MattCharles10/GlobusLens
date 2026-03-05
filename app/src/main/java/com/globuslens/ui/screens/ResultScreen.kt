package com.globuslens.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.globuslens.database.entities.Product
import com.globuslens.ui.components.TranslationDialog
import com.globuslens.viewmodel.ResultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    scannedText: String,
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTranslationDialog by remember { mutableStateOf(false) }
    var selectedTextForTranslation by remember { mutableStateOf("") }

    LaunchedEffect(scannedText) {
        viewModel.processScannedText(scannedText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Original Text",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = scannedText)

                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.translatedText != null) {
                                    Text(
                                        text = "Translated Text",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = uiState.translatedText!!)
                                }

                                IconButton(
                                    onClick = {
                                        selectedTextForTranslation = scannedText
                                        showTranslationDialog = true
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Translate, contentDescription = "Translate")
                                }
                            }
                        }
                    }

                    if (uiState.products.isNotEmpty()) {
                        item {
                            Text(
                                text = "Found Products",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(uiState.products) { product ->
                            ProductSearchResultItem(
                                product = product,
                                onProductClick = { onNavigateToProductDetail(product.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(product) },
                                onAddToShoppingList = { viewModel.addToShoppingList(product) }
                            )
                        }
                    } else if (!uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No products found")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTranslationDialog) {
        TranslationDialog(
            textToTranslate = selectedTextForTranslation,
            onDismiss = { showTranslationDialog = false },
            onTranslationComplete = { translatedText ->
                viewModel.updateTranslatedText(translatedText)
            }
        )
    }
}

@Composable
fun ProductSearchResultItem(
    product: Product,
    onProductClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToShoppingList: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onProductClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )

            if (!product.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (product.isFavorite) "Remove from favorites" else "Add to favorites"
                    )
                }

                IconButton(onClick = onAddToShoppingList) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Add to shopping list")
                }
            }
        }
    }
}