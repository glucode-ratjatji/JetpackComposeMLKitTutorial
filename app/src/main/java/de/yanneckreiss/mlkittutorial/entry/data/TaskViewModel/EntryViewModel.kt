package de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import de.yanneckreiss.mlkittutorial.entry.data.local.toStartOfWeek
import de.yanneckreiss.mlkittutorial.entry.domain.model.*
import de.yanneckreiss.mlkittutorial.entry.domain.repository.IEntryRepository
import de.yanneckreiss.mlkittutorial.entry.domain.util.AnomalyDetector
import de.yanneckreiss.mlkittutorial.export.UtilityExportManager
import de.yanneckreiss.mlkittutorial.worker.UtilityAnomalyWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val repository: IEntryRepository
) : ViewModel() {

    // Current date helpers using kotlinx.datetime
    val currentDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val currentWeekStart: LocalDate = currentDate.toStartOfWeek()
    val currentMonthNumber: Int = currentDate.monthNumber

    // Base database stream reading directly from existing Room database records
    val visibleEntries: StateFlow<List<Entry>> = repository.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI Controls state
    private val _selectedUtilityFilter = MutableStateFlow<UtilityType?>(null)
    val selectedUtilityFilter: StateFlow<UtilityType?> = _selectedUtilityFilter.asStateFlow()

    private val _selectedTimeHorizon = MutableStateFlow(TimeHorizon.DAILY)
    val selectedTimeHorizon: StateFlow<TimeHorizon> = _selectedTimeHorizon.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(SortOption.DATE)
    val selectedSortOption: StateFlow<SortOption> = _selectedSortOption.asStateFlow()

    private val _sortDirection = MutableStateFlow(SortDirection.DESCENDING)
    val sortDirection: StateFlow<SortDirection> = _sortDirection.asStateFlow()

    private val _selectedEntryForDetail = MutableStateFlow<Entry?>(null)
    val selectedEntryForDetail: StateFlow<Entry?> = _selectedEntryForDetail.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _dismissedAnomalyIds = MutableStateFlow<Set<String>>(emptySet())

    // Filtered and Sorted entries reading directly from existing Room database records
    val filteredAndSortedEntries: StateFlow<List<Entry>> = combine(
        visibleEntries,
        _selectedUtilityFilter,
        _selectedSortOption,
        _sortDirection
    ) { entries, utilityFilter, sortOption, direction ->
        val filtered = if (utilityFilter == null) {
            entries
        } else {
            entries.filter { it.detectedUtilityType == utilityFilter }
        }

        val comparator: Comparator<Entry> = when (sortOption) {
            SortOption.DATE -> compareBy { it.id }
            SortOption.UTILITY -> compareBy { it.detectedUtilityType.displayName }
            SortOption.METER_READING -> compareBy { it.meterReading }
            SortOption.UNITS_USED -> compareBy { it.unitsUsed }
            SortOption.WEATHER_SCORE -> compareBy { it.weatherScore }
        }

        if (direction == SortDirection.DESCENDING) {
            filtered.sortedWith(comparator.reversed())
        } else {
            filtered.sortedWith(comparator)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Chart Data Points calculated based on units used across time horizons
    val chartData: StateFlow<List<ChartDataPoint>> = combine(
        visibleEntries,
        _selectedUtilityFilter,
        _selectedTimeHorizon
    ) { entries, utilityFilter, horizon ->
        val targetType = utilityFilter ?: UtilityType.ELECTRICITY
        val matchingEntries = entries.filter { it.detectedUtilityType == targetType }
        val sortedChronological = matchingEntries.sortedBy { it.id }

        buildChartDataPoints(sortedChronological, horizon)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trend Summary calculations based on Units Used (cost removed)
    val trendSummary: StateFlow<TrendSummary?> = combine(
        chartData,
        _selectedUtilityFilter,
        _selectedTimeHorizon
    ) { dataPoints, utilityFilter, horizon ->
        val targetType = utilityFilter ?: UtilityType.ELECTRICITY
        if (dataPoints.size < 2) return@combine null

        val values = dataPoints.map { it.value }
        val total = values.sum()
        val avg = values.average()
        val peak = values.maxOrNull() ?: 0.0
        val lowest = values.minOrNull() ?: 0.0

        val half = dataPoints.size / 2
        val firstHalfAvg = values.take(half).average()
        val secondHalfAvg = values.drop(half).average()

        val pctChange = if (firstHalfAvg > 0.0) {
            ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100.0
        } else 0.0

        val direction = when {
            pctChange > 2.5 -> TrendDirection.INCREASING
            pctChange < -2.5 -> TrendDirection.DECREASING
            else -> TrendDirection.STABLE
        }

        val comparisonLabel = when (horizon) {
            TimeHorizon.DAILY -> "vs previous 7 days"
            TimeHorizon.WEEKLY -> "vs prior 4 weeks"
            TimeHorizon.MONTHLY -> "vs previous quarter"
        }

        TrendSummary(
            percentageChange = ((pctChange * 10).roundToInt() / 10.0),
            direction = direction,
            comparisonPeriodLabel = comparisonLabel,
            averageUnitsUsed = ((avg * 10).roundToInt() / 10.0),
            totalPeriodUnits = ((total * 10).roundToInt() / 10.0),
            peakUnits = peak,
            lowestUnits = lowest,
            unit = targetType.defaultUnit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Consumption Velocity & Burn Rate (cost removed)
    val consumptionVelocity: StateFlow<ConsumptionVelocity> = combine(
        visibleEntries,
        _selectedUtilityFilter
    ) { entries, utilityFilter ->
        val targetType = utilityFilter ?: UtilityType.ELECTRICITY
        val matchingEntries = entries.filter { it.detectedUtilityType == targetType }

        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = (maxDays - currentDay).coerceAtLeast(1)

        val totalUnitsMonthToDate = matchingEntries.sumOf { it.unitsUsed }
        val dailyUnitsBurnRate = if (currentDay > 0) totalUnitsMonthToDate / currentDay else 14.0
        val projectedTotalUnits = totalUnitsMonthToDate + (dailyUnitsBurnRate * daysRemaining)

        ConsumptionVelocity(
            totalUnitsUsedMonthToDate = totalUnitsMonthToDate,
            dailyUnitsBurnRate = dailyUnitsBurnRate,
            projectedMonthEndUnits = projectedTotalUnits,
            daysRemainingInMonth = daysRemaining,
            daysPassedInMonth = currentDay,
            unit = targetType.defaultUnit,
            utilityDisplayName = targetType.displayName
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ConsumptionVelocity(0.0, 0.0, 0.0, 15, 15, "kWh", "Electricity")
    )

    // Active Anomaly Alerts based on units used
    val activeAnomalies: StateFlow<List<AnomalyAlert>> = combine(
        visibleEntries,
        _dismissedAnomalyIds
    ) { entries, dismissed ->
        val detected = AnomalyDetector.detectAnomalies(entries)
        detected.filterNot { dismissed.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Action Handlers ---

    fun setUtilityFilter(type: UtilityType?) {
        _selectedUtilityFilter.value = type
    }

    fun setTimeHorizon(horizon: TimeHorizon) {
        _selectedTimeHorizon.value = horizon
    }

    fun setSortOption(option: SortOption) {
        if (_selectedSortOption.value == option) {
            _sortDirection.value = _sortDirection.value.toggle()
        } else {
            _selectedSortOption.value = option
            _sortDirection.value = SortDirection.DESCENDING
        }
    }

    fun toggleSortDirection() {
        _sortDirection.value = _sortDirection.value.toggle()
    }

    fun openDetailSheet(entry: Entry) {
        _selectedEntryForDetail.value = entry
    }

    fun closeDetailSheet() {
        _selectedEntryForDetail.value = null
    }

    fun setShowAddDialog(show: Boolean) {
        _showAddDialog.value = show
    }

    fun dismissAnomaly(alert: AnomalyAlert) {
        _dismissedAnomalyIds.value = _dismissedAnomalyIds.value + alert.id
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedSampleData()
        }
    }

    fun deleteEntry(entry: Entry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun triggerAnomalyWorker(context: Context) {
        UtilityAnomalyWorker.triggerImmediateCheck(context)
    }

    fun exportData(context: Context) {
        val currentList = filteredAndSortedEntries.value.ifEmpty { visibleEntries.value }
        UtilityExportManager.exportToCsvAndShare(context, currentList)
    }

    fun addManualReading(
        type: UtilityType,
        reading: Double,
        description: String,
        temperature: Double,
        weatherScore: Double
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date(now))
            val cal = Calendar.getInstance()

            val taggedDescription = "[${type.displayName}] $description".trim()

            val newEntry = Entry(
                timestamp = now.toString(),
                date = dateStr,
                weekNumber = cal.get(Calendar.WEEK_OF_YEAR),
                monthKey = (cal.get(Calendar.MONTH) + 1).toString(),
                meterReading = reading,
                previousReadingValue = 0.0,
                unitsConsumedSinceLastVisit = reading,
                daysElapsedSinceLastReading = 1,
                weatherScore = weatherScore,
                temperatureCelsius = temperature,
                cloudCoverage = 20.0,
                precipitation = 0.0,
                windSpeedKms = 10.0,
                visibilityKms = 10.0,
                description = taggedDescription
            )
            repository.insertEntry(newEntry)
            _showAddDialog.value = false
        }
    }

    /**
     * Backward-compatible method used by ML Kit Camera Screen
     */
    fun addScannedReading(kwhStr: String, description: String = "Electricity Meter Scan") {
        viewModelScope.launch {
            val readingValue = kwhStr.toDoubleOrNull() ?: return@launch
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date(now))
            val cal = Calendar.getInstance()

            val taggedDescription = if (description.contains("[Electricity]")) description else "[Electricity] $description"

            val newEntry = Entry(
                timestamp = now.toString(),
                date = dateStr,
                weekNumber = cal.get(Calendar.WEEK_OF_YEAR),
                monthKey = (cal.get(Calendar.MONTH) + 1).toString(),
                meterReading = readingValue,
                previousReadingValue = 0.0,
                unitsConsumedSinceLastVisit = readingValue,
                daysElapsedSinceLastReading = 1,
                weatherScore = 4.2,
                temperatureCelsius = 22.0,
                cloudCoverage = 15.0,
                precipitation = 0.0,
                windSpeedKms = 8.0,
                visibilityKms = 10.0,
                description = taggedDescription
            )
            repository.insertEntry(newEntry)
        }
    }

    private fun buildChartDataPoints(
        entries: List<Entry>,
        horizon: TimeHorizon
    ): List<ChartDataPoint> {
        if (entries.isEmpty()) return emptyList()

        return when (horizon) {
            TimeHorizon.DAILY -> {
                val byDate = entries.groupBy { it.date }
                byDate.entries.toList().takeLast(14).map { (date, items) ->
                    val totalUnitsUsed = items.sumOf { it.unitsUsed }
                    val latestReading = items.lastOrNull()?.meterReading ?: 0.0
                    val avgScore = items.map { it.weatherScore }.average()
                    val temp = items.firstOrNull()?.temperatureCelsius ?: 20.0
                    val isSpike = totalUnitsUsed > (entries.map { it.unitsUsed }.average() * 1.6)

                    ChartDataPoint(
                        label = date.takeLast(5),
                        value = totalUnitsUsed,
                        meterReading = latestReading,
                        weatherScore = avgScore,
                        temperatureCelsius = temp,
                        isAnomaly = isSpike,
                        dateString = date
                    )
                }
            }
            TimeHorizon.WEEKLY -> {
                val byWeek = entries.groupBy { it.weekNumber }
                byWeek.entries.toList().takeLast(8).map { (weekNum, items) ->
                    val totalUnitsUsed = items.sumOf { it.unitsUsed }
                    val latestReading = items.lastOrNull()?.meterReading ?: 0.0
                    val avgScore = items.map { it.weatherScore }.average()
                    val avgTemp = items.map { it.temperatureCelsius }.average()

                    ChartDataPoint(
                        label = "W$weekNum",
                        value = totalUnitsUsed,
                        meterReading = latestReading,
                        weatherScore = avgScore,
                        temperatureCelsius = avgTemp,
                        isAnomaly = false,
                        dateString = "Week $weekNum (${items.firstOrNull()?.date ?: ""})"
                    )
                }
            }
            TimeHorizon.MONTHLY -> {
                val byMonth = entries.groupBy { it.monthKey }
                byMonth.entries.toList().takeLast(6).map { (monthKey, items) ->
                    val totalUnitsUsed = items.sumOf { it.unitsUsed }
                    val latestReading = items.lastOrNull()?.meterReading ?: 0.0
                    val avgScore = items.map { it.weatherScore }.average()
                    val avgTemp = items.map { it.temperatureCelsius }.average()

                    val monthName = when (monthKey.toIntOrNull()) {
                        1 -> "Jan"
                        2 -> "Feb"
                        3 -> "Mar"
                        4 -> "Apr"
                        5 -> "May"
                        6 -> "Jun"
                        7 -> "Jul"
                        8 -> "Aug"
                        9 -> "Sep"
                        10 -> "Oct"
                        11 -> "Nov"
                        12 -> "Dec"
                        else -> "M$monthKey"
                    }

                    ChartDataPoint(
                        label = monthName,
                        value = totalUnitsUsed,
                        meterReading = latestReading,
                        weatherScore = avgScore,
                        temperatureCelsius = avgTemp,
                        isAnomaly = false,
                        dateString = "$monthName Aggregated"
                    )
                }
            }
        }
    }
}
