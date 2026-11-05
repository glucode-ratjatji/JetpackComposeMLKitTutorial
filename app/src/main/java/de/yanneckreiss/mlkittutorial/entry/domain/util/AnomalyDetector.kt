package de.yanneckreiss.mlkittutorial.entry.domain.util

import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import de.yanneckreiss.mlkittutorial.entry.domain.model.AnomalyAlert
import de.yanneckreiss.mlkittutorial.entry.domain.model.AnomalySeverity
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object AnomalyDetector {

    /**
     * Inspects historical entries grouped by utility type to identify unusual consumption spikes
     * based on units used.
     */
    fun detectAnomalies(allEntries: List<Entry>): List<AnomalyAlert> {
        val anomalies = mutableListOf<AnomalyAlert>()

        val grouped = allEntries.groupBy { it.detectedUtilityType }
        for ((type, entries) in grouped) {
            if (entries.size < 3) continue

            val sorted = entries.sortedBy { it.id }
            val consumptions = sorted.map { it.unitsUsed }

            val mean = consumptions.average()
            val variance = consumptions.map { (it - mean).pow(2) }.average()
            val stdDev = sqrt(variance)

            val recentEntries = sorted.takeLast(3)
            for (entry in recentEntries) {
                val value = entry.unitsUsed
                val thresholdZ = mean + (1.75 * stdDev)
                val thresholdRatio = mean * 1.55

                val isSpike = (value > thresholdZ && value > thresholdRatio && value > 0)
                if (isSpike) {
                    val spikePct = (((value - mean) / mean) * 100).roundToInt()
                    val (title, msg, advice, severity) = createAlertContent(type, value, mean, spikePct, entry)

                    anomalies.add(
                        AnomalyAlert(
                            id = "anomaly_${type.name}_${entry.id}",
                            utilityType = type,
                            date = entry.date,
                            observedUnits = value,
                            expectedBaselineUnits = ((mean * 10).roundToInt() / 10.0),
                            spikePercentage = spikePct.toDouble(),
                            severity = severity,
                            title = title,
                            message = msg,
                            actionableAdvice = advice
                        )
                    )
                }
            }
        }

        return anomalies
    }

    private fun createAlertContent(
        type: UtilityType,
        value: Double,
        mean: Double,
        spikePct: Int,
        entry: Entry
    ): AnomalyDetails {
        return when (type) {
            UtilityType.WATER -> {
                AnomalyDetails(
                    title = "Potential Water Leak Detected (+${spikePct}%)",
                    message = "Units used surged to ${value} L (standard baseline is ${(mean).roundToInt()} L). This sudden elevation without matching rainfall strongly suggests a plumbing leak, unclosed valve, or ruptured pipe.",
                    actionableAdvice = "Check meter while all household faucets are off. If indicator spins, shut off main valve and inspect fixtures.",
                    severity = AnomalySeverity.CRITICAL
                )
            }
            UtilityType.ELECTRICITY -> {
                AnomalyDetails(
                    title = "Abnormal Power Surge (+${spikePct}%)",
                    message = "Units used reached ${value} kWh vs. standard baseline of ${((mean * 10).roundToInt() / 10.0)} kWh on ${entry.date}.",
                    actionableAdvice = "Verify high-draw heating elements (water heater geyser, oven, A/C). Check thermostat settings and clean condenser coils.",
                    severity = AnomalySeverity.WARNING
                )
            }
            UtilityType.GAS -> {
                AnomalyDetails(
                    title = "Unusual Propane Gas Depletion (+${spikePct}%)",
                    message = "Cylinder units used measured ${value} kg vs baseline ${((mean * 10).roundToInt() / 10.0)} kg. Accelerated burn rate detected.",
                    actionableAdvice = "Inspect hose connectors and regulator seal with soapy water for bubble leaks immediately. Keep area ventilated.",
                    severity = AnomalySeverity.CRITICAL
                )
            }
        }
    }

    private data class AnomalyDetails(
        val title: String,
        val message: String,
        val actionableAdvice: String,
        val severity: AnomalySeverity
    )
}
