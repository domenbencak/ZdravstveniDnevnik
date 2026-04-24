package com.example.zdravstvenidnevnik.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.zdravstvenidnevnik.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.coroutines.resume

enum class OcrMeasurementField {
    HEART_RATE,
    SPO2,
    TEMPERATURE
}

@Composable
fun CameraPreview(
    cameraController: LifecycleCameraController,
    isProcessing: Boolean,
    onClose: () -> Unit,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(cameraController, lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose {}
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PreviewView(it).apply {
                    controller = cameraController
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { previewView ->
                previewView.controller = cameraController
            }
        )

        IconButton(
            onClick = onClose,
            enabled = !isProcessing,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_close_camera),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        FilledIconButton(
            onClick = onCapture,
            enabled = !isProcessing,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = stringResource(R.string.cd_capture_image)
            )
        }

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.msg_ocr_processing),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

suspend fun captureImageBitmap(
    context: Context,
    cameraController: LifecycleCameraController
): Bitmap? = suspendCancellableCoroutine { continuation ->
    val imageFile = runCatching {
        File.createTempFile("measurement_ocr_", ".jpg", context.cacheDir)
    }.getOrNull()

    if (imageFile == null) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

    cameraController.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bitmap = runCatching {
                    val imageSource = ImageDecoder.createSource(imageFile)
                    ImageDecoder.decodeBitmap(imageSource)
                }.getOrNull()
                imageFile.delete()
                if (continuation.isActive) {
                    continuation.resume(bitmap)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                imageFile.delete()
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }
    )

    continuation.invokeOnCancellation {
        imageFile.delete()
    }
}

suspend fun recognizeNumericValueFromBitmap(bitmap: Bitmap): String? {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    return try {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val recognizedText = recognizer.process(inputImage).await()
        extractNumericValue(recognizedText.text)
    } catch (_: Exception) {
        null
    } finally {
        recognizer.close()
    }
}

fun extractNumericValue(text: String): String? {
    val numericRegex = Regex("""\d+([.,]\d+)?""")
    return numericRegex.find(text)?.value?.replace(',', '.')
}
