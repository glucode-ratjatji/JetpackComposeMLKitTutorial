package de.yanneckreiss.mlkittutorial.gas.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.yanneckreiss.mlkittutorial.gas.model.GasCylinder

/**
 * A modern, beautifully rendered propane gas cylinder composable.
 * Displays dynamic colors, realistic 3D cylindrical specular highlights,
 * authentic propane valve & shroud collar, and dynamically adjusts its dimensions
 * according to the selected size (4kg, 9kg, 14kg, 48kg).
 */
@Composable
fun PropaneGasCylinderComposable(
    cylinder: GasCylinder,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    // Smooth animated color transition when dynamic color changes
    val animatedColor by animateColorAsState(
        targetValue = cylinder.color,
        animationSpec = tween(durationMillis = 450),
        label = "cylinderColor"
    )

    // Dynamic sizing based on weight
    val targetHeight: Dp = when (cylinder.sizeKg) {
        4 -> 190.dp
        9 -> 235.dp
        14 -> 275.dp
        48 -> 325.dp
        else -> 235.dp
    }

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cylinderHeight"
    )

    var isTapped by remember { mutableStateOf(false) }
    val tapScale by animateFloatAsState(
        targetValue = if (isTapped) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "tapScale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .scale(tapScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isInteractive
            ) {
                isTapped = true
                onClick?.invoke()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchedEffect(isTapped) {
            if (isTapped) {
                kotlinx.coroutines.delay(120)
                isTapped = false
            }
        }

        // Propane Cylinder Visual Drawing
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(animatedHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPropaneCylinder(
                    baseColor = animatedColor,
                    sizeKg = cylinder.sizeKg
                )
            }

            // Embedded Stencil Label on the cylinder body
            CylinderDecalLabel(
                sizeKg = cylinder.sizeKg,
                modifier = Modifier
                    .padding(bottom = (animatedHeight.value * 0.28f).dp)
                    .width(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Floor Shadow
        Canvas(
            modifier = Modifier
                .width(130.dp)
                .height(14.dp)
        ) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.28f),
                        Color.Black.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width / 2f
                )
            )
        }
    }
}

/**
 * Custom Canvas drawing routine for the realistic Propane Cylinder structure.
 */
private fun DrawScope.drawPropaneCylinder(
    baseColor: Color,
    sizeKg: Int
) {
    val totalWidth = size.width
    val totalHeight = size.height

    // Calculate proportional regions
    val bodyWidth = totalWidth * 0.78f
    val bodyLeft = (totalWidth - bodyWidth) / 2f
    val bodyRight = bodyLeft + bodyWidth

    val collarHeight = totalHeight * 0.16f
    val valveHeight = collarHeight * 0.65f
    val domeHeight = totalHeight * 0.12f
    val footRingHeight = totalHeight * 0.09f

    val bodyTop = collarHeight + domeHeight * 0.65f
    val bodyBottom = totalHeight - footRingHeight - (domeHeight * 0.65f)
    val bodyHeight = bodyBottom - bodyTop

    // Derive lighting shades from dynamic color
    val darkShade = baseColor.darken(0.35f)
    val lightHighlight = baseColor.lighten(0.35f)
    val specularWhite = Color.White.copy(alpha = 0.45f)

    // 3D Cylindrical lighting brush
    val cylinderBrush = Brush.horizontalGradient(
        0.00f to darkShade,
        0.18f to baseColor,
        0.35f to lightHighlight,
        0.55f to baseColor,
        0.85f to darkShade.darken(0.15f),
        1.00f to darkShade.darken(0.3f),
        startX = bodyLeft,
        endX = bodyRight
    )

    // Specular shine strip
    val shineBrush = Brush.horizontalGradient(
        0.00f to Color.Transparent,
        0.40f to specularWhite,
        1.00f to Color.Transparent,
        startX = bodyLeft + bodyWidth * 0.28f,
        endX = bodyLeft + bodyWidth * 0.45f
    )

    // ==========================================
    // 1. FOOT RING / BASE STAND (Welded bottom)
    // ==========================================
    val footRingWidth = when (sizeKg) {
        4 -> bodyWidth * 0.90f
        48 -> bodyWidth * 0.82f
        else -> bodyWidth * 0.85f
    }
    val footRingLeft = (totalWidth - footRingWidth) / 2f
    val footRingTop = totalHeight - footRingHeight

    // Foot ring shadow/inner dark area
    drawRoundRect(
        color = darkShade.darken(0.4f),
        topLeft = Offset(footRingLeft, footRingTop),
        size = Size(footRingWidth, footRingHeight),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )

    // Foot ring outer face with gradient
    drawRoundRect(
        brush = cylinderBrush,
        topLeft = Offset(footRingLeft + 2.dp.toPx(), footRingTop + 1.dp.toPx()),
        size = Size(footRingWidth - 4.dp.toPx(), footRingHeight - 2.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )

    // Base ventilation / drainage slot (typical of propane cylinders)
    val slotWidth = footRingWidth * 0.25f
    val slotHeight = footRingHeight * 0.4f
    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset((totalWidth - slotWidth) / 2f, footRingTop + (footRingHeight - slotHeight) / 2f),
        size = Size(slotWidth, slotHeight),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    // ==========================================
    // 2. LOWER DOME (Bottom of the tank)
    // ==========================================
    val lowerDomeTop = bodyBottom - (domeHeight * 0.4f)
    drawRoundRect(
        brush = cylinderBrush,
        topLeft = Offset(bodyLeft, lowerDomeTop),
        size = Size(bodyWidth, domeHeight * 1.1f),
        cornerRadius = CornerRadius(bodyWidth * 0.45f, bodyWidth * 0.45f)
    )

    // ==========================================
    // 3. MAIN CYLINDRICAL TANK BODY
    // ==========================================
    drawRect(
        brush = cylinderBrush,
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyWidth, bodyHeight)
    )

    // Specular 3D shine strip on main body
    drawRect(
        brush = shineBrush,
        topLeft = Offset(bodyLeft + bodyWidth * 0.28f, bodyTop),
        size = Size(bodyWidth * 0.17f, bodyHeight)
    )

    // Circumferential Welded Seam Bead (Center factory weld line)
    val weldY = bodyTop + bodyHeight * 0.52f
    drawLine(
        color = darkShade.darken(0.3f),
        start = Offset(bodyLeft, weldY),
        end = Offset(bodyRight, weldY),
        strokeWidth = 2.dp.toPx()
    )
    drawLine(
        color = lightHighlight.copy(alpha = 0.5f),
        start = Offset(bodyLeft, weldY + 2.dp.toPx()),
        end = Offset(bodyRight, weldY + 2.dp.toPx()),
        strokeWidth = 1.2.dp.toPx()
    )

    // Top circumferential weld line
    val topWeldY = bodyTop + 4.dp.toPx()
    drawLine(
        color = darkShade.darken(0.2f),
        start = Offset(bodyLeft, topWeldY),
        end = Offset(bodyRight, topWeldY),
        strokeWidth = 1.5.dp.toPx()
    )

    // ==========================================
    // 4. UPPER DOME (Shoulder)
    // ==========================================
    drawRoundRect(
        brush = cylinderBrush,
        topLeft = Offset(bodyLeft, collarHeight),
        size = Size(bodyWidth, domeHeight * 1.3f),
        cornerRadius = CornerRadius(bodyWidth * 0.45f, bodyWidth * 0.45f)
    )

    // Specular highlight on upper shoulder
    drawRoundRect(
        brush = shineBrush,
        topLeft = Offset(bodyLeft + bodyWidth * 0.28f, collarHeight),
        size = Size(bodyWidth * 0.17f, domeHeight * 1.3f),
        cornerRadius = CornerRadius(bodyWidth * 0.45f, bodyWidth * 0.45f)
    )

    // ==========================================
    // 5. BRASS VALVE & REGULATOR OUTLET
    // ==========================================
    val valveCenterX = totalWidth / 2f
    val valveBaseY = collarHeight + (domeHeight * 0.35f)
    val valveTopY = valveBaseY - valveHeight

    // Brass fitting base
    val brassColor = Color(0xFFD4AF37)
    val brassDark = Color(0xFF997A15)
    val brassBrush = Brush.horizontalGradient(
        listOf(brassDark, brassColor, brassDark),
        startX = valveCenterX - 10.dp.toPx(),
        endX = valveCenterX + 10.dp.toPx()
    )

    // Valve stem
    drawRoundRect(
        brush = brassBrush,
        topLeft = Offset(valveCenterX - 6.dp.toPx(), valveTopY + 8.dp.toPx()),
        size = Size(12.dp.toPx(), valveHeight - 6.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Horizontal gas outlet port (pointing to the right)
    drawRoundRect(
        brush = brassBrush,
        topLeft = Offset(valveCenterX + 3.dp.toPx(), valveTopY + 12.dp.toPx()),
        size = Size(10.dp.toPx(), 7.dp.toPx()),
        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
    )
    // Outlet nozzle opening
    drawCircle(
        color = Color(0xFF333333),
        center = Offset(valveCenterX + 13.dp.toPx(), valveTopY + 15.5.dp.toPx()),
        radius = 2.dp.toPx()
    )

    // Black/Dark Handwheel Shut-off Knob on top
    val knobWidth = 24.dp.toPx()
    val knobHeight = 7.dp.toPx()
    drawRoundRect(
        color = Color(0xFF1E1E1E),
        topLeft = Offset(valveCenterX - knobWidth / 2f, valveTopY),
        size = Size(knobWidth, knobHeight),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    // Knob serrations / grips
    for (i in 0..4) {
        val lineX = (valveCenterX - knobWidth / 2f) + (knobWidth / 5f) * i + 2.dp.toPx()
        drawLine(
            color = Color(0xFF424242),
            start = Offset(lineX, valveTopY + 1.dp.toPx()),
            end = Offset(lineX, valveTopY + knobHeight - 1.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
    }

    // ==========================================
    // 6. TOP PROTECTIVE SHROUD / COLLAR HANDLE
    // ==========================================
    val collarWidth = bodyWidth * 0.72f
    val collarLeft = (totalWidth - collarWidth) / 2f
    val collarTop = 4.dp.toPx()

    // Outer Shroud shape
    val shroudPath = Path().apply {
        addRoundRect(
            RoundRect(
                left = collarLeft,
                top = collarTop,
                right = collarLeft + collarWidth,
                bottom = collarTop + collarHeight + 6.dp.toPx(),
                topLeftCornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                topRightCornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                bottomLeftCornerRadius = CornerRadius(0f, 0f),
                bottomRightCornerRadius = CornerRadius(0f, 0f)
            )
        )
    }

    drawPath(
        path = shroudPath,
        brush = Brush.horizontalGradient(
            0.0f to darkShade,
            0.3f to lightHighlight,
            0.6f to baseColor,
            1.0f to darkShade,
            startX = collarLeft,
            endX = collarLeft + collarWidth
        )
    )

    // Rolled Top Rim highlight on Collar
    drawRoundRect(
        color = lightHighlight.copy(alpha = 0.7f),
        topLeft = Offset(collarLeft, collarTop),
        size = Size(collarWidth, 3.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Handhold Cutouts (Left and Right oval slots in the collar)
    val handleSlotWidth = collarWidth * 0.28f
    val handleSlotHeight = collarHeight * 0.40f
    val handleSlotTop = collarTop + collarHeight * 0.32f

    // Left handle slot cutout
    drawRoundRect(
        color = Color(0x3D000000),
        topLeft = Offset(collarLeft + 5.dp.toPx(), handleSlotTop),
        size = Size(handleSlotWidth, handleSlotHeight),
        cornerRadius = CornerRadius(handleSlotHeight / 2f, handleSlotHeight / 2f)
    )
    drawRoundRect(
        color = lightHighlight.copy(alpha = 0.3f),
        topLeft = Offset(collarLeft + 5.dp.toPx(), handleSlotTop),
        size = Size(handleSlotWidth, handleSlotHeight),
        cornerRadius = CornerRadius(handleSlotHeight / 2f, handleSlotHeight / 2f),
        style = Stroke(width = 1.dp.toPx())
    )

    // Right handle slot cutout
    drawRoundRect(
        color = Color(0x3D000000),
        topLeft = Offset(collarLeft + collarWidth - handleSlotWidth - 5.dp.toPx(), handleSlotTop),
        size = Size(handleSlotWidth, handleSlotHeight),
        cornerRadius = CornerRadius(handleSlotHeight / 2f, handleSlotHeight / 2f)
    )
    drawRoundRect(
        color = lightHighlight.copy(alpha = 0.3f),
        topLeft = Offset(collarLeft + collarWidth - handleSlotWidth - 5.dp.toPx(), handleSlotTop),
        size = Size(handleSlotWidth, handleSlotHeight),
        cornerRadius = CornerRadius(handleSlotHeight / 2f, handleSlotHeight / 2f),
        style = Stroke(width = 1.dp.toPx())
    )
}

/**
 * Modern stencil label and hazard badge on the cylinder tank.
 */
@Composable
private fun CylinderDecalLabel(
    sizeKg: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.92f),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flammable Gas Icon & Warning Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔥",
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "PROPANE",
                    color = Color(0xFFC62828),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            // Big Stamped Size Display
            Text(
                text = "$sizeKg kg",
                color = Color(0xFF212121),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                lineHeight = 18.sp
            )

            // Stamped Spec Line
            Text(
                text = "LPG • UN 1978",
                color = Color(0xFF757575),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// Color lightening & darkening helper extensions
private fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

private fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}
