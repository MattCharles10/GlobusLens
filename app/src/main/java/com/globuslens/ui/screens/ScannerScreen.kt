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
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.globuslens.ui.components.CameraPreview
import com.globuslens.viewmodel.ScannerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
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
    val context = LocalContext.current

    var showPermissionDialog by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    // Check permission on launch
    LaunchedEffect(Unit) {
        when (cameraPermissionState.status) {
            is PermissionStatus.Denied -> {
                if (!(cameraPermissionState.status as PermissionStatus.Denied).shouldShowRationale) {
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
            navController.navigate("product_detail/$id") {
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
            when (cameraPermissionState.status) {
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
                            ProcessingOverlay(
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                        uiState.detectedText.isNotBlank() -> {
                            DetectedTextOverlay(
                                detectedText = uiState.detectedText,
                                translatedText = uiState.translatedText,
                                onSave = { viewModel.saveProduct() },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                        else -> {
                            ScanningInstructionOverlay(
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }

                is PermissionStatus.Denied -> {
                    val deniedState = cameraPermissionState.status as PermissionStatus.Denied
                    PermissionDeniedContent(
                        showRationale = deniedState.shouldShowRationale,
                        onRequestPermission = {
                            if (deniedState.shouldShowRationale) {
                                cameraPermissionState.launchPermissionRequest()
                            } else {
                                showPermissionDialog = true
                            }
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                        // Open app settings
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

@Composable
fun ProcessingOverlay(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
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

@Composable
fun DetectedTextOverlay(
    detectedText: String,
    translatedText: String?,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
            Text(
                text = detectedText.take(100) +
                        if (detectedText.length > 100) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            if (translatedText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Translated:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = translatedText.take(100) + if (translatedText.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Product")
            }
        }
    }
}

@Composable
fun ScanningInstructionOverlay(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(16.dp)
    ) {
        Text(
            text = "Point camera at product text",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PermissionDeniedContent(
    showRationale: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                text = if (showRationale)
                    "We need camera access to scan product labels. Please grant permission to continue."
                else
                    "Camera permission is required to use the scanner feature. Please grant permission in settings.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showRationale) "Grant Permission" else "Open Settings")
            }
        }
    }
}