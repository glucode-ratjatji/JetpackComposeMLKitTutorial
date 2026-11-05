package de.yanneckreiss.mlkittutorial.entry.domain.model

import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType

enum class TimeHorizon(val label: String, val subtitle: String) {
    DAILY("Daily", "Day-by-Day (17d)"),
    WEEKLY("Weekly", "Weekly Averages"),
    MONTHLY("Monthly", "Monthly Totals")
}

enum class SortOption(val displayName: String) {
    DATE("Date"),
    UTILITY("Utility"),
    METER_READING("Meter Reading"),
    UNITS_USED("Units Used"),
    WEATHER_SCORE("Weather Score")
}

enum class SortDirection {
    ASCENDING,
    DESCENDING;

    fun toggle(): SortDirection = if (this == ASCENDING) DESCENDING else ASCENDING
}

data class ChartDataPoint(
    val label: String,
    val value: Double, // Units used
    val meterReading: Double = 0.0,
    val isEstimated: Boolean = false, // Visual indicator: true = averaged/estimated, false = actual recorded
    val secondaryValue: Double? = null,
    val weatherScore: Double = 0.0,
    val temperatureCelsius: Double = 20.0,
    val isAnomaly: Boolean = false, // Exceeds baseline average by >25%
    val spikePercentage: Int = 0,
    val dateString: String = ""
) {
    val isActual: Boolean
        get() = !isEstimated
}

enum class TrendDirection {
    INCREASING, // Usage up
    DECREASING, // Usage down (conserving units)
    STABLE
}

data class TrendSummary(
    val percentageChange: Double,
    val direction: TrendDirection,
    val comparisonPeriodLabel: String,
    val averageUnitsUsed: Double,
    val totalPeriodUnits: Double,
    val peakUnits: Double,
    val lowestUnits: Double,
    val unit: String
)

data class ConsumptionVelocity(
    val totalUnitsUsedMonthToDate: Double,
    val dailyUnitsBurnRate: Double,
    val projectedMonthEndUnits: Double,
    val daysRemainingInMonth: Int,
    val daysPassedInMonth: Int,
    val unit: String,
    val utilityDisplayName: String
)

data class WeatherImpactBreakdown(
    val weatherScore: Double,
    val temperatureCelsius: Double,
    val humidity: Double = 50.0,
    val cloudCoverage: Double,
    val precipitation: Double,
    val windSpeedKms: Double,
    val visibilityKms: Double,
    val impactHeadline: String,
    val correlationExplanation: String,
    val heatingCoolingDemand: String,
    val loadSeverityPercentage: Int
)

enum class AnomalySeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class AnomalyAlert(
    val id: String,
    val utilityType: UtilityType,
    val date: String,
    val observedUnits: Double,
    val expectedBaselineUnits: Double,
    val spikePercentage: Double,
    val severity: AnomalySeverity,
    val title: String,
    val message: String,
    val actionableAdvice: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
