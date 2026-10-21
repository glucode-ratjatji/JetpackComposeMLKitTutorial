package de.yanneckreiss.mlkittutorial.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.camera.core.AspectRatio
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CameraScreen() {
    CameraContent()
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
private fun CameraContent() {
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }
    var detectedText: String by remember { mutableStateOf("No text detected yet..") }

    // Regex to match integers or decimals right before optional spaces and "kWh" (case-insensitive)
    val kwhRegex = remember { Regex("""(\d+(?:\.\d+)?)\s*[kK][wW][hH]""") }

    // Extract the matched number, or show a fallback message if not found
    val extractedKwhValue = remember(detectedText) {
        val matchResult = kwhRegex.find(detectedText)
        matchResult?.groupValues?.get(1) ?: "No kWh value found"
    }

    fun onTextUpdated(updatedText: String) {
        detectedText = updatedText
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("House Keeper") }) },
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 1. Camera Preview
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PreviewView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(AndroidColor.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        startTextRecognition(
                            context = context,
                            cameraController = cameraController,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            onDetectedTextUpdated = ::onTextUpdated
                        )
                    }
                }
            )
            //Column(modifier = Modifier.fillMaxSize()) {
                // 2. Receptive Field Overlay (Viewfinder Box)
                ReceptiveFieldOverlay()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Extracted kWh Result in Red
                Text(
                    text = "Extracted kWh: $extractedKwhValue",
                    style = TextStyle(
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Raw Detected Text Display
                Text(
                    text = detectedText,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Draws a dark translucent overlay with a clear rectangular cutout representing
 * the receptive field where the meter should be placed.
 */
@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("ComposeModifierMissing")
@Composable
fun ReceptiveFieldOverlay() {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
     ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define the dimensions of the receptive box (e.g., width 80% of screen, height 200px)
        val boxWidth = canvasWidth * 0.8f
        val boxHeight = canvasHeight* 0.5f
        val left = (canvasWidth - boxWidth) / 2f
        val top = (canvasHeight - boxHeight) / 2f



        val textLayoutResult = textMeasurer.measure(
            text = "Centre electricity Meter in the frame",
            style = TextStyle(
                color = Color.Black,
                fontSize = 24.sp
            )
        )


        // Draw dark background
        drawRect(color = Color.Black.copy(alpha = 0.15f))

        // Cut out the clear scanning window using BlendMode.Clear
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16f, 16f),
            blendMode = BlendMode.Clear
        )
        // Draw a border around the receptive field for visual guidance
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(x = 100f, y = top-200) // Position coordinates
        )
        drawRoundRect(
            color = Color.Red,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16f, 16f),
            blendMode = BlendMode.Clear
        )

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16f, 16f),
            style = Stroke(width = 8f),
        )
    }
}

private fun startTextRecognition(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onDetectedTextUpdated: (String) -> Unit
) {
    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}