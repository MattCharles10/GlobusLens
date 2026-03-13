package com.globuslens.ui.components

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.globuslens.camera.rememberCameraManager

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScannerPreview(
    onBarcodeDetected: (String, Int) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = rememberCameraManager()

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    var isCameraInitialized by remember { mutableStateOf(false) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status is PermissionStatus.Granted && previewView != null && !isCameraInitialized) {
            try {
                cameraManager.startBarcodeScanner(
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView!!,
                    onBarcodeDetected = onBarcodeDetected
                )
                isCameraInitialized = true
            } catch (e: Exception) {
                onError("Failed to start barcode scanner: ${e.message}")
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (cameraPermissionState.status) {
            is PermissionStatus.Granted -> {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also {
                            previewView = it
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Scanning overlay
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Align barcode within frame",
                        modifier = Modifier.padding(12.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Barcode guide frame
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                        .fillMaxSize(0.7f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 4.dp.toPx()
                            val cornerLength = 40.dp.toPx()

                            // Top-left corner
                            drawLine(
                                color = Color.White,
                                start = Offset(0f, 0f),
                                end = Offset(cornerLength, 0f),
                                strokeWidth = strokeWidth
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(0f, 0f),
                                end = Offset(0f, cornerLength),
                                strokeWidth = strokeWidth
                            )

                            // Top-right corner
                            drawLine(
                                color = Color.White,
                                start = Offset(size.width, 0f),
                                end = Offset(size.width - cornerLength, 0f),
                                strokeWidth = strokeWidth
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(size.width, 0f),
                                end = Offset(size.width, cornerLength),
                                strokeWidth = strokeWidth
                            )

                            // Bottom-left corner
                            drawLine(
                                color = Color.White,
                                start = Offset(0f, size.height),
                                end = Offset(cornerLength, size.height),
                                strokeWidth = strokeWidth
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(0f, size.height),
                                end = Offset(0f, size.height - cornerLength),
                                strokeWidth = strokeWidth
                            )

                            // Bottom-right corner
                            drawLine(
                                color = Color.White,
                                start = Offset(size.width, size.height),
                                end = Offset(size.width - cornerLength, size.height),
                                strokeWidth = strokeWidth
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(size.width, size.height),
                                end = Offset(size.width, size.height - cornerLength),
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                }
            }

            is PermissionStatus.Denied -> {
                PermissionHandler(
                    permissionState = cameraPermissionState,
                    modifier = Modifier.fillMaxSize()
                )
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
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                                "Camera permission is needed to scan barcodes. Please grant permission to continue."
                            } else {
                                "Camera permission is required. Please enable it in app settings."
                            }
                        }
                        else -> "Camera permission is required to use the barcode scanner."
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