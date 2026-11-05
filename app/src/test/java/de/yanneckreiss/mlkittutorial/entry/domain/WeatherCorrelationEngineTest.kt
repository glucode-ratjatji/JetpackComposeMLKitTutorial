package de.yanneckreiss.mlkittutorial.entry.domain

import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.domain.util.WeatherCorrelationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherCorrelationEngineTest {

    @Test
    fun testHighTemperature_TriggersCoolingLoad_ForElectricity() {
        val hotEntry = Entry(
            description = "[Electricity] High AC consumption",
            temperatureCelsius = 34.0,
            weatherScore = 8.5
        )

        val analysis = WeatherCorrelationEngine.analyzeImpact(hotEntry)

        assertEquals("Elevated Cooling Load (A/C)", analysis.heatingCoolingDemand)
        assertTrue(analysis.loadSeverityPercentage > 0)
        assertTrue(analysis.correlationExplanation.contains("HVAC"))
    }

    @Test
    fun testFreezingTemperature_TriggersHeatingLoad_ForElectricity() {
        val coldEntry = Entry(
            description = "[Electricity] Cold winter heating",
            temperatureCelsius = 8.0,
            weatherScore = 8.0
        )

        val analysis = WeatherCorrelationEngine.analyzeImpact(coldEntry)

        assertEquals("Elevated Heating Load", analysis.heatingCoolingDemand)
        assertTrue(analysis.loadSeverityPercentage > 0)
        assertTrue(analysis.correlationExplanation.contains("heating"))
    }

    @Test
    fun testHeavyRain_SuppressesOutdoorWaterDemand() {
        val rainyEntry = Entry(
            description = "[Water] Rain day",
            temperatureCelsius = 19.0,
            precipitation = 16.0,
            weatherScore = 2.0
        )

        val analysis = WeatherCorrelationEngine.analyzeImpact(rainyEntry)

        assertTrue(analysis.impactHeadline.contains("Rainfall"))
        assertTrue(analysis.correlationExplanation.contains("irrigation"))
    }

    @Test
    fun testColdTemperature_IncreasesPropaneGasHeatingBurn() {
        val coldGasEntry = Entry(
            description = "[Gas] Cylinder heating",
            temperatureCelsius = 10.0,
            windSpeedKms = 25.0,
            weatherScore = 7.8
        )

        val analysis = WeatherCorrelationEngine.analyzeImpact(coldGasEntry)

        assertEquals("Intensive Heating Burn", analysis.heatingCoolingDemand)
        assertTrue(analysis.loadSeverityPercentage > 0)
    }
}
