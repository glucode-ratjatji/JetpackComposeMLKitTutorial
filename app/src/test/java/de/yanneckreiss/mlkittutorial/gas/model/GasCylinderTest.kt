package de.yanneckreiss.mlkittutorial.gas.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GasCylinderTest {

    @Test
    fun testGasCylinderDefaultCreationAndTypealias() {
        val cylinder: gasCylinder = GasCylinder(
            sizeKg = 9,
            color = DefaultGasCylinderColors[0]
        )

        assertEquals(9, cylinder.sizeKg)
        assertEquals(DefaultGasCylinderColors[0], cylinder.color)
        assertTrue(GasCylinderSizeOptions.contains(cylinder.sizeKg))
    }

    @Test
    fun testDynamicColorChangeDirect() {
        val original = GasCylinder(sizeKg = 14, color = Color.Red)
        val newColor = Color.Blue

        val updated = original.changeColor(newColor)

        assertEquals(newColor, updated.color)
        assertEquals(14, updated.sizeKg)
    }

    @Test
    fun testDynamicColorChangeFromList() {
        val palette = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow)
        val cylinder = GasCylinder(sizeKg = 4, color = palette[0])

        val picked2 = cylinder.changeColorFromList(palette, 2)
        assertEquals(Color.Blue, picked2.color)

        val picked3 = cylinder.changeColorFromList(palette, 3)
        assertEquals(Color.Yellow, picked3.color)
    }

    @Test
    fun testDynamicColorCycle() {
        val palette = listOf(Color.Red, Color.Green, Color.Blue)
        val c0 = GasCylinder(sizeKg = 9, color = palette[0])

        val c1 = c0.cycleNextColor(palette)
        assertEquals(Color.Green, c1.color)

        val c2 = c1.cycleNextColor(palette)
        assertEquals(Color.Blue, c2.color)

        val c3 = c2.cycleNextColor(palette)
        assertEquals(Color.Red, c3.color)
    }

    @Test
    fun testAllowedSizes() {
        val supportedSizes = listOf(4, 9, 14, 48)
        assertEquals(supportedSizes, GasCylinderSizeOptions)

        supportedSizes.forEach { size ->
            val cylinder = GasCylinder(sizeKg = size)
            assertEquals(size, cylinder.sizeKg)
        }
    }

    @Test
    fun testDaysCalculations() {
        val bought = LocalDate(2026, 10, 1)
        val usage = LocalDate(2026, 10, 10)

        val cylinder = GasCylinder(
            sizeKg = 9,
            boughtDate = bought,
            usageStartDate = usage
        )

        assertEquals(9, cylinder.daysInStorageBeforeUse)
    }
}
