package de.yanneckreiss.mlkittutorial.gas.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import java.util.UUID

/**
 * Predefined palette of authentic LPG and Propane gas cylinder colors.
 * Allows users to dynamically change cylinder color to match their real cylinder brand/type:
 * - Propane Red (Classic LPG)
 * - Gas Blue (Standard Industrial)
 * - Charcoal Slate (Modern Graphite)
 * - Camping Green (Outdoor / Camping)
 * - Safety Orange (High-Visibility Industrial)
 * - Steel Silver (Alloy / Galvanized)
 * - Caution Yellow (Commercial)
 * - Deep Teal (Specialty Gas)
 * - Plum Purple (Patio Gas)
 */
val DefaultGasCylinderColors: List<Color> = listOf(
    Color(0xFFD32F2F), // Propane Red
    Color(0xFF1976D2), // Industrial Blue
    Color(0xFF37474F), // Charcoal Slate
    Color(0xFF2E7D32), // Camping Green
    Color(0xFFF57C00), // Safety Orange
    Color(0xFF78909C), // Steel Silver
    Color(0xFFFBC02D), // Caution Yellow
    Color(0xFF00796B), // Deep Teal
    Color(0xFF6A1B9A)  // Plum Purple
)

/**
 * Standard gas cylinder size options in kilograms (kg) as requested:
 * 4kg (Portable/Camping), 9kg (BBQ/Patio), 14kg (Domestic Household), 48kg (Bulk Heating).
 */
val GasCylinderSizeOptions: List<Int> = listOf(4, 9, 14, 48)

/**
 * Metadata for cylinder size options including user-friendly description.
 */
data class CylinderSizeInfo(
    val sizeKg: Int,
    val title: String,
    val description: String,
    val estimatedDaysDuration: Int
)

val CylinderSizeInfoList: List<CylinderSizeInfo> = listOf(
    CylinderSizeInfo(4, "4 kg", "Portable / Camping & Cooker", 20),
    CylinderSizeInfo(9, "9 kg", "Standard BBQ & Patio Heater", 50),
    CylinderSizeInfo(14, "14 kg", "Domestic Household & Stove", 85),
    CylinderSizeInfo(48, "48 kg", "Commercial & Whole-House Heating", 240)
)

/**
 * Represents a gas cylinder entry for tracking gas consumption throughout the year.
 * Supports dynamic color changing using a list of colors.
 */
data class GasCylinder(
    val id: String = UUID.randomUUID().toString(),
    val sizeKg: Int = 9,
    val color: Color = DefaultGasCylinderColors[0],
    val boughtDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val usageStartDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val notes: String = "",
    val supplier: String = "LPG Supplier",
    val isActive: Boolean = true
) {
    /**
     * Dynamically changes the color of this cylinder to [newColor].
     */
    fun changeColor(newColor: Color): GasCylinder {
        return copy(color = newColor)
    }

    /**
     * Dynamically changes color by selecting an index from a provided [colors] list.
     */
    fun changeColorFromList(colors: List<Color>, index: Int): GasCylinder {
        if (colors.isEmpty()) return this
        val safeIndex = index.coerceIn(0, colors.lastIndex)
        return copy(color = colors[safeIndex])
    }

    /**
     * Dynamically cycles to the next color in the [colors] palette.
     */
    fun cycleNextColor(colors: List<Color> = DefaultGasCylinderColors): GasCylinder {
        if (colors.isEmpty()) return this
        val currentIndex = colors.indexOf(color)
        val nextIndex = if (currentIndex in colors.indices) {
            (currentIndex + 1) % colors.size
        } else {
            0
        }
        return copy(color = colors[nextIndex])
    }

    /**
     * Calculates the number of days the cylinder was in storage before being hooked up.
     */
    val daysInStorageBeforeUse: Int
        get() {
            val days = boughtDate.daysUntil(usageStartDate)
            return if (days >= 0) days else 0
        }

    /**
     * Calculates days currently elapsed since usage started.
     */
    val daysInUse: Int
        get() {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val days = usageStartDate.daysUntil(today)
            return if (days >= 0) days else 0
        }

    /**
     * Estimated remaining days based on average consumption for the chosen cylinder size.
     */
    val estimatedRemainingDays: Int
        get() {
            val totalEst = CylinderSizeInfoList.find { it.sizeKg == sizeKg }?.estimatedDaysDuration ?: 50
            val remaining = totalEst - daysInUse
            return if (remaining >= 0) remaining else 0
        }
}

/**
 * Typealias to support both lowercase 'gasCylinder' as stated in requirements
 * and standard Kotlin PascalCase 'GasCylinder'.
 */
typealias gasCylinder = GasCylinder
