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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.globuslens.R
import com.globuslens.database.entities.Product
import com.globuslens.ui.components.DetailItem
import com.globuslens.ui.components.EmptyState
import com.globuslens.ui.components.ErrorState
import com.globuslens.ui.components.LoadingState
import com.globuslens.ui.components.TranslationDialog
import com.globuslens.viewmodel.ProductDetailViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: Long,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showTranslationDialog by remember { mutableStateOf(false) }
    var showAddToListDialog by remember { mutableStateOf(false) }

    // Load product when screen is opened
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
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
                    viewModel.loadProduct(productId)
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
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.product != null && !uiState.isLoading) {
                        IconButton(
                            onClick = { showTranslationDialog = true },
                            enabled = !uiState.isTranslating
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = "Translate")
                        }
                        IconButton(
                            onClick = {
                                viewModel.toggleFavorite(!uiState.product!!.isFavorite)
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.product!!.isFavorite)
                                    Icons.Default.Favorite
                                else
                                    Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (uiState.product!!.isFavorite)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
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
                uiState.isLoading && uiState.product == null -> {
                    LoadingState(message = "Loading product details...")
                }

                uiState.error != null && uiState.product == null -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadProduct(productId) }
                    )
                }

                uiState.product != null -> {
                    ProductDetailContent(
                        product = uiState.product!!,
                        isTranslating = uiState.isTranslating,
                        onAddToShoppingList = { showAddToListDialog = true }
                    )
                }

                else -> {
                    EmptyState(
                        icon = Icons.Default.ArrowBack,
                        title = "Product Not Found",
                        message = "The product you're looking for doesn't exist",
                        buttonText = "Go Back",
                        onButtonClick = { navController.navigateUp() }
                    )
                }
            }
        }
    }

    // Translation Dialog
    if (showTranslationDialog && uiState.product != null) {
        TranslationDialog(
            originalText = uiState.product!!.name,
            translatedText = uiState.product!!.translatedName,
            isLoading = uiState.isTranslating,
            sourceLanguage = uiState.product!!.originalLanguage ?: "auto",
            targetLanguage = "en",
            onDismiss = { showTranslationDialog = false },
            onTranslate = { text, targetLang ->
                viewModel.translateProduct(text, targetLang)
            }
        )
    }

    // Add to Shopping List Dialog
    if (showAddToListDialog && uiState.product != null) {
        AlertDialog(
            onDismissRequest = { showAddToListDialog = false },
            title = { Text("Add to Shopping List") },
            text = { Text("Add '${uiState.product!!.name}' to your shopping list?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addToShoppingList(uiState.product!!)
                        showAddToListDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddToListDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProductDetailContent(
    product: Product,
    isTranslating: Boolean,
    onAddToShoppingList: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Product Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(product.imageUrl ?: "https://via.placeholder.com/300")
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_background)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product Name Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Product Name",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (product.translatedName != null && product.translatedName != product.name) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.translatedName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (isTranslating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Translating...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Price Section (if available)
        if (product.price != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Price:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = when (product.currency) {
                            "USD" -> "$${"%.2f".format(product.price)}"
                            "EUR" -> "€${"%.2f".format(product.price)}"
                            "GBP" -> "£${"%.2f".format(product.price)}"
                            else -> "${product.currency} ${"%.2f".format(product.price)}"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Product Details Card - Using DetailItem component
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Category
                if (product.category != null) {
                    DetailItem(
                        label = "Category",
                        value = product.category
                    )
                }

                // Expiry Date
                if (product.expiryDate != null) {
                    DetailItem(
                        label = "Expiry Date",
                        value = dateFormat.format(product.expiryDate),
                        isWarning = product.expiryDate.before(Date())
                    )
                }

                // Nutrition Info
                if (product.nutritionInfo != null) {
                    DetailItem(
                        label = "Nutrition",
                        value = product.nutritionInfo
                    )
                }

                // Ingredients
                if (product.ingredients != null) {
                    DetailItem(
                        label = "Ingredients",
                        value = product.ingredients
                    )
                }

                // Allergens
                if (product.allergens != null) {
                    DetailItem(
                        label = "Allergens",
                        value = product.allergens,
                        isWarning = true
                    )
                }

                // Barcode
                if (product.barcode != null) {
                    DetailItem(
                        label = "Barcode",
                        value = product.barcode
                    )
                }

                // Scanned Date
                DetailItem(
                    label = "Scanned on",
                    value = dateFormat.format(product.scannedDate)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Button(
            onClick = onAddToShoppingList,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isTranslating
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text("Add to Shopping List")
        }
    }
}