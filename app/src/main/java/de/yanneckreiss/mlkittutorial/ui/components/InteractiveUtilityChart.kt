package de.yanneckreiss.mlkittutorial.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import de.yanneckreiss.mlkittutorial.entry.domain.model.ChartDataPoint
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun InteractiveUtilityChart(
    dataPoints: List<ChartDataPoint>,
    utilityType: UtilityType,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No trend data available for selected period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Animation progress for smooth line drawing
    val animProgress = remember(dataPoints, utilityType) { Animatable(0f) }
    LaunchedEffect(dataPoints, utilityType) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 650))
    }

    var selectedIndex by remember(dataPoints) { mutableStateOf<Int?>(null) }
    val primaryColor = utilityType.composeColor
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val values = dataPoints.map { it.value }
    val maxValue = remember(values) { (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0) * 1.15 }
    val minValue = 0.0
    val avgValue = remember(values) { if (values.isNotEmpty()) values.average() else 0.0 }

    Column(modifier = modifier.fillMaxWidth()) {
        // Active point tooltip inspection card
        val activePoint = selectedIndex?.let { dataPoints.getOrNull(it) } ?: dataPoints.lastOrNull()
        if (activePoint != null) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📅 ${activePoint.dateString.ifEmpty { activePoint.label }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", activePoint.value)} ${utilityType.defaultUnit} used",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            if (activePoint.meterReading > 0.0) {
                                Text(
                                    text = "(Meter: ${String.format(Locale.US, "%.0f", activePoint.meterReading)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = if (activePoint.weatherScore >= 7.0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Score: ${String.format(Locale.US, "%.1f", activePoint.weatherScore)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = if (activePoint.weatherScore >= 7.0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        if (activePoint.isAnomaly) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = "⚠️ SPIKE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }
                }
            }
        }

        // Canvas for Drawing Interactive Line & Gradient Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(dataPoints) {
                    detectTapGestures { offset ->
                        val pointSpacing = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                        val index = (offset.x / pointSpacing).roundToInt().coerceIn(0, dataPoints.size - 1)
                        selectedIndex = index
                    }
                }
                .pointerInput(dataPoints) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val pointSpacing = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                        val index = (change.position.x / pointSpacing).roundToInt().coerceIn(0, dataPoints.size - 1)
                        selectedIndex = index
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val paddingBottom = 24.dp.toPx()
                val chartHeight = height - paddingBottom
                val pointCount = dataPoints.size

                if (pointCount < 2) return@Canvas

                val stepX = width / (pointCount - 1)

                // 1. Draw horizontal grid lines (0%, 50%, 100%)
                val gridLines = listOf(0.0, maxValue * 0.5, maxValue)
                for (gridVal in gridLines) {
                    val y = (chartHeight - ((gridVal / maxValue) * chartHeight)).toFloat()
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                // 2. Draw Average Baseline Line
                val avgY = (chartHeight - ((avgValue / maxValue) * chartHeight)).toFloat()
                drawLine(
                    color = primaryColor.copy(alpha = 0.45f),
                    start = Offset(0f, avgY),
                    end = Offset(width, avgY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f))
                )

                // 3. Compute Coordinates
                val points = dataPoints.mapIndexed { index, dp ->
                    val x = index * stepX
                    val normalizedY = ((dp.value - minValue) / (maxValue - minValue)).toFloat()
                    val targetY = chartHeight - (normalizedY * chartHeight)
                    val animatedY = chartHeight - ((chartHeight - targetY) * animProgress.value)
                    Offset(x, animatedY)
                }

                // 4. Construct Smooth Cubic Bezier Line and Gradient Fill Paths
                val strokePath = Path()
                val fillPath = Path()

                strokePath.moveTo(points.first().x, points.first().y)
                fillPath.moveTo(points.first().x, chartHeight)
                fillPath.lineTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y

                    strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }

                fillPath.lineTo(points.last().x, chartHeight)
                fillPath.close()

                // Draw Gradient Area Under Curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.03f)
                        ),
                        startY = 0f,
                        endY = chartHeight
                    )
                )

                // Draw Main Trend Line
                drawPath(
                    path = strokePath,
                    color = primaryColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // 5. Draw Data Points, Anomalies, and Selected Inspection Cursor
                points.forEachIndexed { i, pt ->
                    val dp = dataPoints[i]
                    val isSelected = (selectedIndex == i)

                    if (dp.isAnomaly) {
                        // Pulsing outer halo for anomaly spike
                        drawCircle(
                            color = Color.Red.copy(alpha = 0.35f),
                            radius = 11.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.Red,
                            radius = 6.dp.toPx(),
                            center = pt
                        )
                    } else if (isSelected) {
                        drawCircle(
                            color = primaryColor,
                            radius = 7.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )
                    } else if (pointCount <= 14) {
                        drawCircle(
                            color = primaryColor,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // If user selected a specific point, draw a vertical scrubbing cursor
                selectedIndex?.let { idx ->
                    val selectedPt = points.getOrNull(idx)
                    if (selectedPt != null) {
                        drawLine(
                            color = onSurface.copy(alpha = 0.6f),
                            start = Offset(selectedPt.x, 0f),
                            end = Offset(selectedPt.x, chartHeight),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }
                }
            }
        }

        // X-Axis Labels (first, middle, last)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val firstLabel = dataPoints.firstOrNull()?.label ?: ""
            val midLabel = dataPoints.getOrNull(dataPoints.size / 2)?.label ?: ""
            val lastLabel = dataPoints.lastOrNull()?.label ?: ""

            Text(text = firstLabel, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
            if (dataPoints.size > 2) {
                Text(text = midLabel, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
            }
            Text(text = lastLabel, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
        }
    }
}
