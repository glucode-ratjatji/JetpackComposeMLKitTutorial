package de.yanneckreiss.mlkittutorial.entry.domain.util

import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import de.yanneckreiss.mlkittutorial.entry.domain.model.WeatherImpactBreakdown
import kotlin.math.abs
import kotlin.math.roundToInt

object WeatherCorrelationEngine {

    /**
     * Deconstructs environmental factors and explains how they historically correlate with usage spikes.
     */
    fun analyzeImpact(entry: Entry): WeatherImpactBreakdown {
        val type = entry.detectedUtilityType
        val temp = entry.temperatureCelsius
        val humidity = 50.0
        val rain = entry.precipitation
        val wind = entry.windSpeedKms
        val cloud = entry.cloudCoverage
        val score = entry.weatherScore

        return when (type) {
            UtilityType.ELECTRICITY -> analyzeElectricity(temp, humidity, cloud, score)
            UtilityType.WATER -> analyzeWater(temp, rain, humidity, score)
            UtilityType.GAS -> analyzeGas(temp, wind, score)
        }
    }

    private fun analyzeElectricity(
        temp: Double,
        humidity: Double,
        cloud: Double,
        score: Double
    ): WeatherImpactBreakdown {
        val (headline, demand, explanation, pct) = when {
            temp >= 28.0 -> {
                val spike = ((temp - 24.0) * 8.5).roundToInt().coerceIn(15, 65)
                val humidityNote = if (humidity > 60.0) " with elevated humidity (${humidity.toInt()}%) increasing heat index" else ""
                Quadruple(
                    "High Cooling Load Demand",
                    "Elevated Cooling Load (A/C)",
                    "Ambient heat of ${temp}°C$humidityNote triggered sustained HVAC compression cycles, correlating with an estimated +$spike% surge in electricity consumption over comfortable room baselines (21°C).",
                    spike
                )
            }
            temp <= 14.0 -> {
                val spike = ((18.0 - temp) * 6.5).roundToInt().coerceIn(12, 55)
                Quadruple(
                    "High Heating & Thermal Load",
                    "Elevated Heating Load",
                    "Chilly outdoor temperature of ${temp}°C forced indoor space heaters and heat pumps into peak defrost/heating operation, contributing to a +$spike% baseline load increase.",
                    spike
                )
            }
            cloud > 75.0 -> {
                val spike = 18
                Quadruple(
                    "Reduced Solar Irradiance & Lighting Demand",
                    "Lighting & Solar Deficit",
                    "Dense overcast cloud cover (${cloud.toInt()}%) reduced natural daylighting and rooftop solar yields, prompting increased indoor luminaire usage.",
                    spike
                )
            }
            else -> {
                Quadruple(
                    "Optimal Ambient Conditions",
                    "Neutral / Baseline Load",
                    "Mild climate (${temp}°C, ${humidity.toInt()}% humidity) falls within the passive comfort band (18–24°C). Space conditioning electrical draw remained at nominal baseline.",
                    0
                )
            }
        }

        return WeatherImpactBreakdown(
            weatherScore = score,
            temperatureCelsius = temp,
            humidity = humidity,
            cloudCoverage = cloud,
            precipitation = 0.0,
            windSpeedKms = 12.0,
            visibilityKms = 10.0,
            impactHeadline = headline,
            correlationExplanation = explanation,
            heatingCoolingDemand = demand,
            loadSeverityPercentage = pct
        )
    }

    private fun analyzeWater(
        temp: Double,
        rain: Double,
        humidity: Double,
        score: Double
    ): WeatherImpactBreakdown {
        val (headline, demand, explanation, pct) = when {
            rain > 5.0 -> {
                val saving = ((rain * 4.0)).roundToInt().coerceIn(20, 65)
                Quadruple(
                    "Rainfall Suppressed Irrigation",
                    "Negative Demand (Rain Delay)",
                    "Precipitation of ${rain}mm replenished soil moisture, reducing outdoor garden and lawn irrigation needs by approximately ~$saving%.",
                    -saving
                )
            }
            temp >= 30.0 -> {
                val spike = ((temp - 26.0) * 7.0).roundToInt().coerceIn(15, 50)
                Quadruple(
                    "High Evapotranspiration Rate",
                    "Elevated Outdoor Demand",
                    "Intense summer heat of ${temp}°C and low humidity accelerated evapotranspiration, driving increased lawn hydration, pool top-ups, and personal water consumption (+${spike}%).",
                    spike
                )
            }
            else -> {
                Quadruple(
                    "Nominal Water Demand",
                    "Baseline Indoor Consumption",
                    "Stable weather conditions with no precipitation extremes. Consumption driven primarily by consistent indoor domestic fixtures.",
                    0
                )
            }
        }

        return WeatherImpactBreakdown(
            weatherScore = score,
            temperatureCelsius = temp,
            humidity = humidity,
            cloudCoverage = 20.0,
            precipitation = rain,
            windSpeedKms = 8.0,
            visibilityKms = 10.0,
            impactHeadline = headline,
            correlationExplanation = explanation,
            heatingCoolingDemand = demand,
            loadSeverityPercentage = abs(pct)
        )
    }

    private fun analyzeGas(
        temp: Double,
        wind: Double,
        score: Double
    ): WeatherImpactBreakdown {
        val (headline, demand, explanation, pct) = when {
            temp <= 12.0 -> {
                val windFactor = if (wind > 20.0) " compounded by strong wind gusts (${wind.toInt()} km/h)" else ""
                val spike = ((18.0 - temp) * 5.0).roundToInt().coerceIn(15, 60)
                Quadruple(
                    "Peak Water & Space Heating Load",
                    "Intensive Heating Burn",
                    "Cold outdoor ambient of ${temp}°C$windFactor required gas geysers/water heaters and furnaces to burn propane at higher BTU rates to reach target delta-T (+${spike}% fuel rate).",
                    spike
                )
            }
            else -> {
                Quadruple(
                    "Steady Cooking / Baseline Burn",
                    "Nominal Gas Consumption",
                    "Moderate temperatures maintained steady gas hot water demand with no cold-weather thermal loss on exterior cylinder regulators.",
                    0
                )
            }
        }

        return WeatherImpactBreakdown(
            weatherScore = score,
            temperatureCelsius = temp,
            humidity = 45.0,
            cloudCoverage = 30.0,
            precipitation = 0.0,
            windSpeedKms = wind,
            visibilityKms = 10.0,
            impactHeadline = headline,
            correlationExplanation = explanation,
            heatingCoolingDemand = demand,
            loadSeverityPercentage = pct
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
