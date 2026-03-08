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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    // Request permission when screen is first shown
    LaunchedEffect(Unit) {
        if (cameraPermissionState.status is PermissionStatus.Denied) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearError()
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (cameraPermissionState.status) {
                is PermissionStatus.Denied -> {
                    PermissionHandler(
                        permissionState = cameraPermissionState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is PermissionStatus.Granted -> {
                    when {
                        uiState.isProcessing -> {
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
                                    Text(
                                        text = uiState.detectedText.take(100) +
                                                if (uiState.detectedText.length > 100) "..." else "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    uiState.translatedText?.let {
                                        Text(
                                            text = "Translated:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = it.take(100) + if (it.length > 100) "..." else "",
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
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    permissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    val permissionStatus = permissionState.status

    Box(
        modifier = modifier,
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
                    text = when (permissionStatus) {
                        is PermissionStatus.Denied -> {
                            if (permissionStatus.shouldShowRationale) {
                                "We need camera access to scan product labels. Please grant permission to continue."
                            } else {
                                "Camera permission is required to use the scanner feature. Please grant permission in settings."
                            }
                        }
                        else -> "Camera permission is required to use the scanner feature."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { permissionState.launchPermissionRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}