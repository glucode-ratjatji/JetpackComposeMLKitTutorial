package de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.toStartOfWeek
import de.yanneckreiss.mlkittutorial.entry.domain.repository.IEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val repository: IEntryRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    // Current date helpers using kotlinx.datetime
    val currentDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val currentWeekStart: LocalDate = currentDate.toStartOfWeek()
    val currentMonthNumber: Int = currentDate.monthNumber

    val visibleEntries: StateFlow<List<Entry>> = repository.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun addScannedReading(kwhStr: String, description: String = "Electricity Meter Scan") {
        viewModelScope.launch {
            val readingValue = kwhStr.toDoubleOrNull() ?: return@launch

            val newEntry = Entry(
                timestamp = System.currentTimeMillis().toString(),
                date = currentDate.toString(),
                weekNumber = currentMonthNumber, // or custom week number logic
                monthKey = currentMonthNumber.toString(),
                meterReading = readingValue,
                // Default values for weather-related modules as requested
                weatherScore = 1.0,
                temperatureCelsius = 22.0,
                cloudCoverage = 0.0,
                precipitation = 0.0,
                windSpeedKms = 5.0,
                visibilityKms = 10.0,
                description = description
            )

            viewModelScope.launch {
                repository.insertEntry(newEntry)
            }
        }
    }
}
