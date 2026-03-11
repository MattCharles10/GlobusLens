package com.globuslens.ui.components

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.globuslens.camera.rememberCameraManager

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreview(
    onTextDetected: (String) -> Unit,
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
                cameraManager.startCamera(
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView!!,
                    onTextDetected = onTextDetected
                )
                isCameraInitialized = true
            } catch (e: Exception) {
                onError("Failed to start camera: ${e.message}")
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
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "Point camera at product text",
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            is PermissionStatus.Denied -> {
                val deniedState = cameraPermissionState.status as PermissionStatus.Denied
                if (deniedState.shouldShowRationale) {
                    PermissionRationale()
                } else {
                    PermissionDenied()
                }
            }
        }
    }
}

@Composable
fun PermissionRationale() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Text(
                text = "Camera permission is needed to scan product labels",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun PermissionDenied() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = "Camera permission denied. Cannot scan products.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}