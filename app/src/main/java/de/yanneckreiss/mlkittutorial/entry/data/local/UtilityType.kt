package de.yanneckreiss.mlkittutorial.entry.data.local

import androidx.compose.ui.graphics.Color

enum class UtilityType(
    val id: String,
    val displayName: String,
    val emoji: String,
    val defaultUnit: String,
    val colorHex: Long
) {
    ELECTRICITY(
        id = "ELECTRICITY",
        displayName = "Electricity",
        emoji = "⚡",
        defaultUnit = "kWh",
        colorHex = 0xFFFFB300 // Amber / Golden
    ),
    WATER(
        id = "WATER",
        displayName = "Water",
        emoji = "💧",
        defaultUnit = "L",
        colorHex = 0xFF0288D1 // Ocean Blue
    ),
    GAS(
        id = "GAS",
        displayName = "Propane Gas",
        emoji = "♨️",
        defaultUnit = "kg",
        colorHex = 0xFFE64A19 // Warm Flame Orange
    );

    val composeColor: Color
        get() = Color(colorHex)

    companion object {
        fun fromString(value: String?): UtilityType {
            val v = value?.lowercase()?.trim() ?: return ELECTRICITY
            return when {
                v.contains("water") || v.contains("aqua") || v.contains("m3") || v.contains("m³") || v.contains("liter") || v.contains("litre") || v.contains("💧") -> WATER
                v.contains("gas") || v.contains("propane") || v.contains("cylinder") || v.contains("kg") || v.contains("♨") -> GAS
                else -> ELECTRICITY
            }
        }
    }
}
