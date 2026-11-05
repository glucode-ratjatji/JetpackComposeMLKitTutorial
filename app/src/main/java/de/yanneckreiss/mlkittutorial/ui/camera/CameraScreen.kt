package de.yanneckreiss.mlkittutorial.ui.camera

import android.annotation.SuppressLint
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel.EntryViewModel
import de.yanneckreiss.mlkittutorial.ui.no_permission.NoPermissionScreen

// Color Palette
private val PanelColor = Color(0xFF14171C)
private val PanelMuted = Color(0xFF9AA3AF)
private val AccentGreen = Color(0xFF52C41A)
private val ScrimColor = Color.Black.copy(alpha = 0.55f)

@SuppressLint("ComposeViewModelForwarding")
@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainScreen(
    viewModel: EntryViewModel,
    onNavigateBack: () -> Unit
) {
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    MainContent(
        hasPermission = cameraPermissionState.status.isGranted,
        onRequestPermission = cameraPermissionState::launchPermissionRequest,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    )
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
private fun MainContent(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    viewModel: EntryViewModel,
    onNavigateBack: () -> Unit
) {
    if (hasPermission) {
        CameraScreen(viewModel = viewModel, onNavigateBack = onNavigateBack)
    } else {
        NoPermissionScreen(onRequestPermission)
    }
}

@SuppressLint("ComposeModifierMissing")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CameraScreen(
    viewModel: EntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context: Context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }
    var detectedText: String by remember { mutableStateOf("Scanning meter reading..") }

    // Regex to match kWh values (e.g., "1234.5 kWh" or "1234kWh")
    val kwhRegex = remember { Regex("""(\d+(?:\.\d+)?)\s*[kK][wW][hH]""") }

    val extractedKwhValue = remember(detectedText) {
        kwhRegex.find(detectedText)?.groupValues?.get(1) ?: ""
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Scan Electricity Meter", fontWeight = FontWeight.SemiBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PanelColor)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 1. Camera Preview & ML Kit Analyzer
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
                            onDetectedTextUpdated = { detectedText = it }
                        )
                    }
                }
            )

            ReceptiveFieldOverlay()

            BottomPanel(
                extractedKwhValue = extractedKwhValue,
                detectedText = detectedText,
                onConfirm = { validKwh ->
                    viewModel.addScannedReading(kwhStr = validKwh)
                    onNavigateBack()
                },
                onCancel = onNavigateBack
            )
        }
    }
}

@Composable
private fun BottomPanel(
    extractedKwhValue: String,
    detectedText: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    val isReadyToConfirm = extractedKwhValue.isNotBlank() && extractedKwhValue.toDoubleOrNull() != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(PanelColor)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
        ,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Extracted kWh result feedback
        Text(
            text = if (isReadyToConfirm) "⚡ Detected kWh: $extractedKwhValue" else "Align meter in frame...",
            style = TextStyle(
                color = if (isReadyToConfirm) AccentGreen else Color.Yellow,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "Raw OCR: $detectedText",
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(color = PanelMuted, fontSize = 12.sp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { if (isReadyToConfirm) onConfirm(extractedKwhValue) },
                enabled = isReadyToConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    disabledContainerColor = Color.DarkGray
                )
            ) {
                Text("Confirm & Save", fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ReceptiveFieldOverlay(modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val boxWidth = size.width * 0.82f
        val boxHeight = size.height * 0.25f
        val left = (size.width - boxWidth) / 2f
        val top = (size.height - boxHeight) / 2f - 40.dp.toPx()

        // Scrim background
        drawRect(color = ScrimColor)

        // Punch out scanning window
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Corner brackets guide
        drawCornerBrackets(left, top, boxWidth, boxHeight, 32.dp.toPx(), 24.dp.toPx(), 4.dp.toPx())

        // Hint text
        val hint = textMeasurer.measure(
            text = "Position electricity numbers inside frame",
            style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        )
        drawText(
            textLayoutResult = hint,
            topLeft = Offset(x = (size.width - hint.size.width) / 2f, y = top - hint.size.height - 16.dp.toPx())
        )
    }
}

private fun DrawScope.drawCornerBrackets(
    left: Float, top: Float, width: Float, height: Float,
    armLength: Float, radius: Float, strokeWidth: Float
) {
    val right = left + width
    val bottom = top + height
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val white = Color.White

    // Top-left
    drawPath(Path().apply { moveTo(left, top + armLength); lineTo(left, top + radius); quadraticBezierTo(left, top, left + radius, top); lineTo(left + armLength, top) }, white, style = stroke)
    // Top-right
    drawPath(Path().apply { moveTo(right - armLength, top); lineTo(right - radius, top); quadraticBezierTo(right, top, right, top + radius); lineTo(right, top + armLength) }, white, style = stroke)
    // Bottom-right
    drawPath(Path().apply { moveTo(right, bottom - armLength); lineTo(right, bottom - radius); quadraticBezierTo(right, bottom, right - radius, bottom); lineTo(right - armLength, bottom) }, white, style = stroke)
    // Bottom-left
    drawPath(Path().apply { moveTo(left + armLength, bottom); lineTo(left + radius, bottom); quadraticBezierTo(left, bottom, left, bottom - radius); lineTo(left, bottom - armLength) }, white, style = stroke)
}

private fun startTextRecognition(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onDetectedTextUpdated: (String) -> Unit
) {
    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    // Assuming TextRecognitionAnalyzer is your ML Kit Analyzer implementation
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        de.yanneckreiss.mlkittutorial.ui.camera.TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
    )
    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}