package com.globuslens.ui.screens

import android.Manifest
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.globuslens.ui.components.CameraPreview
import com.globuslens.viewmodel.ScannerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showPermissionDialog by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    // Check permission on launch
    LaunchedEffect(Unit) {
        when (val status = cameraPermissionState.status) {
            is PermissionStatus.Denied -> {
                if (!status.shouldShowRationale) {
                    showPermissionDialog = true
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            }
            else -> {}
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
                    // Retry action
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

    // Navigate to result when product is saved
    LaunchedEffect(uiState.savedProductId) {
        uiState.savedProductId?.let { id ->
            delay(500)
            navController.navigate("result/$id") {
                popUpTo("scanner") { inclusive = false }
            }
            viewModel.onResultShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val status = cameraPermissionState.status) {
                is PermissionStatus.Granted -> {
                    CameraPreview(
                        onTextDetected = { text ->
                            viewModel.onTextDetected(text)
                        },
                        onError = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay UI based on state
                    when {
                        uiState.isProcessing -> {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Processing text...")
                                }
                            }
                        }
                        uiState.detectedText.isNotBlank() -> {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Detected:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Safely display detected text
                                    val detectedDisplay = if (uiState.detectedText.length > 100) {
                                        uiState.detectedText.take(100) + "..."
                                    } else {
                                        uiState.detectedText
                                    }
                                    Text(
                                        text = detectedDisplay,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2
                                    )

                                    // Safely display translated text if available
                                    uiState.translatedText?.let { translated ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Translated:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        val translatedDisplay = if (translated.length > 100) {
                                            translated.take(100) + "..."
                                        } else {
                                            translated
                                        }
                                        Text(
                                            text = translatedDisplay,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { viewModel.saveProduct() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Save Product")
                                    }
                                }
                            }
                        }
                        else -> {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Point camera at product text",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                is PermissionStatus.Denied -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Camera Permission Required",
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (status.shouldShowRationale)
                                        "We need camera access to scan product labels. Please grant permission to continue."
                                    else
                                        "Camera permission is required to use the scanner feature. Please grant permission in settings.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        if (status.shouldShowRationale) {
                                            cameraPermissionState.launchPermissionRequest()
                                        } else {
                                            showPermissionDialog = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (status.shouldShowRationale) "Grant Permission" else "Open Settings")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Permission explanation dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("Camera permission is needed to scan product labels. Please enable it in app settings.") },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        // Open app settings - you can implement this later
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}