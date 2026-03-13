package com.globuslens.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.globuslens.ui.components.BarcodeScannerPreview
import com.globuslens.ui.components.PermissionHandler
import com.globuslens.viewmodel.BarcodeScannerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    navController: NavController,
    viewModel: BarcodeScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle barcode detection
    LaunchedEffect(uiState.scannedBarcode) {
        uiState.scannedBarcode?.let { barcode ->
            delay(500)
            // Navigate to product lookup or manual entry
            if (uiState.productFound) {
                navController.navigate("product_detail/${uiState.productId}")
            }
            viewModel.clearBarcode()
        }
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
                    viewModel.retry()
                }
            }
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Barcode") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFlashlight() }) {
                        Icon(
                            if (uiState.flashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flashlight"
                        )
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
            BarcodeScannerPreview(
                onBarcodeDetected = { barcode, format ->
                    viewModel.onBarcodeDetected(barcode, format)
                },
                onError = { error ->
                    scope.launch {
                        snackbarHostState.showSnackbar(error)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Bottom sheet for manual entry
            if (uiState.showManualEntry) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Enter Barcode Manually",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        androidx.compose.material3.OutlinedTextField(
                            value = uiState.manualBarcode,
                            onValueChange = { viewModel.updateManualBarcode(it) },
                            label = { Text("Barcode number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.lookupManualBarcode() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.manualBarcode.isNotBlank()
                        ) {
                            Text("Look Up Product")
                        }

                        TextButton(
                            onClick = { viewModel.toggleManualEntry(false) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }

            // Loading overlay
            if (uiState.isProcessing) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Looking up product...")
                    }
                }
            }

            // Barcode not found dialog
            if (uiState.showNotFoundDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissNotFoundDialog() },
                    title = { Text("Product Not Found") },
                    text = { Text("No product found for barcode ${uiState.scannedBarcode}. Would you like to create a manual entry?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.dismissNotFoundDialog()
                                navController.navigate("manual_product_entry/${uiState.scannedBarcode}")
                            }
                        ) {
                            Text("Create Entry")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissNotFoundDialog() }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}