package com.globuslens.ui.screens

import android.graphics.Bitmap
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.globuslens.ui.components.BottomNavBar
import com.globuslens.ui.components.CameraPreview
import com.globuslens.utils.Resource
import com.globuslens.viewmodel.ScannerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val scanState by viewModel.scanState
    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status is PermissionStatus.Denied &&
            !(cameraPermissionState.status as PermissionStatus.Denied).shouldShowRationale) {
            showPermissionDialog = true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                cameraPermissionState.status.isGranted -> {
                    CameraContent(
                        onImageCaptured = { bitmap ->
                            viewModel.scanProduct(bitmap)
                        },
                        onError = { exception ->
                            // Handle error
                        },
                        scanState = scanState,
                        onProductScanned = { productId ->
                            navController.navigate("result/$productId")
                        }
                    )
                }

                cameraPermissionState.status is PermissionStatus.Denied -> {
                    PermissionDeniedContent(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                        showRationale = (cameraPermissionState.status as PermissionStatus.Denied).shouldShowRationale
                    )
                }
            }
        }
    }
}

@Composable
fun CameraContent(
    onImageCaptured: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    scanState: Resource<Int>,
    onProductScanned: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            onImageCaptured = onImageCaptured,
            onError = onError,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = "Point camera at product label",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyLarge
        )

        when (scanState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is Resource.Success -> {
                LaunchedEffect(scanState.data) {
                    onProductScanned(scanState.data)
                }
            }
            is Resource.Error -> {
                // Show error message
            }
            else -> {}
        }
    }
}

@Composable
fun PermissionDeniedContent(
    onRequestPermission: () -> Unit,
    showRationale: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (showRationale)
                "Camera permission is required to scan products"
            else
                "Camera permission is required. Please enable it in settings",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestPermission
        ) {
            Text("Grant Permission")
        }
    }
}