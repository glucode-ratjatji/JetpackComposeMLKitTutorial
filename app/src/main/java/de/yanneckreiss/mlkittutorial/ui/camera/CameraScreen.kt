package de.yanneckreiss.mlkittutorial.ui.camera

import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Build
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
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

// ---- Palette -------------------------------------------------------------
private val PanelColor = Color(0xFF14171C)
private val PanelMuted = Color(0xFF9AA3AF)
private val AccentRed = Color(0xFFFF4D4F)
private val ScrimColor = Color.Black.copy(alpha = 0.55f)

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CameraScreen(
    onManualEntry: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    CameraContent(onManualEntry = onManualEntry, onCancel = onCancel)
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
private fun CameraContent(
    onManualEntry: () -> Unit,
    onCancel: () -> Unit
) {
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }
    var detectedText: String by remember { mutableStateOf("No text detected yet..") }

    // Integers or decimals directly before optional spaces and "kWh" (case-insensitive)
    val kwhRegex = remember { Regex("""(\d+(?:\.\d+)?)\s*[kK][wW][hH]""") }

    val extractedKwhValue = remember(detectedText) {
        kwhRegex.find(detectedText)?.groupValues?.get(1) ?: "No kWh value found"
    }

    fun onTextUpdated(updatedText: String) {
        detectedText = updatedText
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("House Keeper", fontWeight = FontWeight.SemiBold) },
                backgroundColor = PanelColor,
                contentColor = Color.White,
                elevation = 0.dp
            )
        },
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 1. Camera preview
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(AndroidColor.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        startTextRecognition(
                            context = ctx,
                            cameraController = cameraController,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            onDetectedTextUpdated = ::onTextUpdated
                        )
                    }
                }
            )

            ReceptiveFieldOverlay()

            BottomPanel(
                extractedKwhValue = extractedKwhValue,
                detectedText = detectedText,
                onManualEntry = onManualEntry,
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun BottomPanel(
    extractedKwhValue: String,
    detectedText: String,
    onManualEntry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(PanelColor)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Actions sit above the red result text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onManualEntry,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Manual entry", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Extracted kWh result in red
        Text(
            text = "Extracted kWh: $extractedKwhValue",
            style = TextStyle(
                color = AccentRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Raw detected text, quieter
        Text(
            text = detectedText,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(color = PanelMuted, fontSize = 13.sp)
        )
    }
}

/**
 * Dim scrim with a clear scanning window. Instead of a full outline, only the four
 * corners are drawn in white as rounded L-shaped brackets.
 */
@Composable
fun ReceptiveFieldOverlay(modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val boxWidth = size.width * 0.82f
        val boxHeight = size.height * 0.30f
        val left = (size.width - boxWidth) / 2f
        // Sit slightly above centre so the bottom panel never covers the frame
        val top = size.height * 0.30f

        // Scrim, then punch out the window
        drawRect(color = ScrimColor)
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Four white corners
        drawCornerBrackets(
            left = left,
            top = top,
            width = boxWidth,
            height = boxHeight,
            armLength = 36.dp.toPx(),
            radius = 28.dp.toPx(),
            strokeWidth = 4.dp.toPx()
        )

        // Hint above the frame
        val hint = textMeasurer.measure(
            text = "Centre the electricity meter in the frame",
            style = TextStyle(
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        )
        drawText(
            textLayoutResult = hint,
            topLeft = Offset(
                x = (size.width - hint.size.width) / 2f,
                y = top - hint.size.height - 20.dp.toPx()
            )
        )
    }
}

private fun DrawScope.drawCornerBrackets(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    armLength: Float,
    radius: Float,
    strokeWidth: Float
) {
    val right = left + width
    val bottom = top + height
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val white = Color.White

    // Top-left
    drawPath(
        Path().apply {
            moveTo(left, top + armLength)
            lineTo(left, top + radius)
            quadraticBezierTo(left, top, left + radius, top)
            lineTo(left + armLength, top)
        }, white, style = stroke
    )
    // Top-right
    drawPath(
        Path().apply {
            moveTo(right - armLength, top)
            lineTo(right - radius, top)
            quadraticBezierTo(right, top, right, top + radius)
            lineTo(right, top + armLength)
        }, white, style = stroke
    )
    // Bottom-right
    drawPath(
        Path().apply {
            moveTo(right, bottom - armLength)
            lineTo(right, bottom - radius)
            quadraticBezierTo(right, bottom, right - radius, bottom)
            lineTo(right - armLength, bottom)
        }, white, style = stroke
    )
    // Bottom-left
    drawPath(
        Path().apply {
            moveTo(left + armLength, bottom)
            lineTo(left + radius, bottom)
            quadraticBezierTo(left, bottom, left, bottom - radius)
            lineTo(left, bottom - armLength)
        }, white, style = stroke
    )
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