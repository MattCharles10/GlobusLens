package com.globuslens.ui.screens

import android.Manifest
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.globuslens.ui.components.CameraPreview
import com.globuslens.ui.components.BottomNavBar
import com.globuslens.viewmodel.ScannerViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var showTranslationDialog by remember { mutableStateOf(false) }
    var currentScannedText by remember { mutableStateOf("") }
    val recognizedText by viewModel.recognizedText.collectAsState()

    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotEmpty()) {
            currentScannedText = recognizedText
            showTranslationDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "GlobusLens Scanner",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Point camera at text to translate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Camera Preview
            when (cameraPermissionState.status) {
                is PermissionStatus.Granted -> {
                    val controller = remember { LifecycleCameraController(context) }

                    CameraPreview(
                        controller = controller,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onImageCaptured = { bitmap ->
                            viewModel.processCapturedImage(bitmap)
                        }
                    )

                    // Capture Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        FloatingActionButton(
                            onClick = {
                                val executor = ContextCompat.getMainExecutor(context)
                                takePhoto(controller, executor) { bitmap ->
                                    viewModel.processCapturedImage(bitmap)
                                }
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Capture",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Camera permission required",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }

        // Bottom Navigation
        BottomNavBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter),
            currentRoute = Screen.Scanner.route
        )

        // Translation Dialog
        if (showTranslationDialog && currentScannedText.isNotEmpty()) {
            TranslationDialog(
                text = currentScannedText,
                onDismiss = { showTranslationDialog = false },
                onSaveToFavorites = {
                    viewModel.saveToFavorites(currentScannedText)
                    showTranslationDialog = false
                },
                onAddToShoppingList = {
                    viewModel.addToShoppingList(currentScannedText)
                    showTranslationDialog = false
                }
            )
        }
    }
}

private fun takePhoto(
    controller: LifecycleCameraController,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit
) {
    val file = File.createTempFile("captured", ".jpg")

    controller.takePicture(
        ImageCapture.OutputFileOptions.Builder(file).build(),
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                // Convert file to bitmap and process
                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                onImageCaptured(bitmap)
                file.delete()
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}