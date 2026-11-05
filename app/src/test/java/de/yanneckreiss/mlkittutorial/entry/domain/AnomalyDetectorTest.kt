package de.yanneckreiss.mlkittutorial.entry.domain

import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import de.yanneckreiss.mlkittutorial.entry.domain.model.AnomalySeverity
import de.yanneckreiss.mlkittutorial.entry.domain.util.AnomalyDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnomalyDetectorTest {

    @Test
    fun testWaterSpike_DetectedAsCriticalLeakAnomaly() {
        val normalEntries = listOf(
            Entry(id = 1, description = "[Water] Reading 1", unitsConsumedSinceLastVisit = 290.0),
            Entry(id = 2, description = "[Water] Reading 2", unitsConsumedSinceLastVisit = 310.0),
            Entry(id = 3, description = "[Water] Reading 3", unitsConsumedSinceLastVisit = 300.0),
            Entry(id = 4, description = "[Water] Reading 4", unitsConsumedSinceLastVisit = 295.0),
            Entry(id = 5, description = "[Water] Reading 5", unitsConsumedSinceLastVisit = 820.0) // Leak spike!
        )

        val anomalies = AnomalyDetector.detectAnomalies(normalEntries)

        assertTrue(anomalies.isNotEmpty())
        val alert = anomalies.first()
        assertEquals(UtilityType.WATER, alert.utilityType)
        assertEquals(AnomalySeverity.CRITICAL, alert.severity)
        assertTrue(alert.title.contains("Leak"))
    }

    @Test
    fun testStableConsumption_YieldsNoAnomalies() {
        val stableEntries = listOf(
            Entry(id = 1, description = "[Electricity] Reading 1", unitsConsumedSinceLastVisit = 14.0),
            Entry(id = 2, description = "[Electricity] Reading 2", unitsConsumedSinceLastVisit = 14.5),
            Entry(id = 3, description = "[Electricity] Reading 3", unitsConsumedSinceLastVisit = 13.8),
            Entry(id = 4, description = "[Electricity] Reading 4", unitsConsumedSinceLastVisit = 14.2),
            Entry(id = 5, description = "[Electricity] Reading 5", unitsConsumedSinceLastVisit = 14.6)
        )

        val anomalies = AnomalyDetector.detectAnomalies(stableEntries)

        assertTrue(anomalies.isEmpty())
    }
}
