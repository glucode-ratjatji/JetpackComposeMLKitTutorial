package de.yanneckreiss.mlkittutorial.gas.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yanneckreiss.mlkittutorial.gas.data.GasCylinderRepository
import de.yanneckreiss.mlkittutorial.gas.model.DefaultGasCylinderColors
import de.yanneckreiss.mlkittutorial.gas.model.GasCylinder
import de.yanneckreiss.mlkittutorial.gas.model.GasCylinderSizeOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

data class YearlyUsageStats(
    val totalGasKg: Int = 0,
    val cylinderCount: Int = 0,
    val activeDays: Int = 0,
    val currentYear: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
)

@HiltViewModel
class GasCylinderViewModel @Inject constructor(
    private val repository: GasCylinderRepository
) : ViewModel() {

    val activeCylinder: StateFlow<GasCylinder> = repository.activeCylinder
    val cylinderHistory: StateFlow<List<GasCylinder>> = repository.cylinderHistory

    // Cylinder currently being edited or configured in the UI form
    private val _editingCylinder = MutableStateFlow(repository.activeCylinder.value)
    val editingCylinder: StateFlow<GasCylinder> = _editingCylinder.asStateFlow()

    val availableColors: List<Color> = DefaultGasCylinderColors
    val sizeOptions: List<Int> = GasCylinderSizeOptions

    // Yearly statistics computed across all logged cylinders
    val yearlyStats: StateFlow<YearlyUsageStats> = combine(
        activeCylinder,
        cylinderHistory
    ) { active, history ->
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        val thisYearEntries = (listOf(active) + history).distinctBy { it.id }.filter {
            it.usageStartDate.year == currentYear || it.boughtDate.year == currentYear
        }
        val totalKg = thisYearEntries.sumOf { it.sizeKg }
        YearlyUsageStats(
            totalGasKg = totalKg,
            cylinderCount = thisYearEntries.size,
            activeDays = active.daysInUse,
            currentYear = currentYear
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = YearlyUsageStats()
    )

    init {
        viewModelScope.launch {
            // Keep editing cylinder in sync with active on first load
            _editingCylinder.value = repository.activeCylinder.value
        }
    }

    /**
     * Updates cylinder size (4, 9, 14, 48 kg).
     */
    fun onSizeSelected(sizeKg: Int) {
        _editingCylinder.value = _editingCylinder.value.copy(sizeKg = sizeKg)
    }

    /**
     * Dynamically changes color using a selected Color from the list.
     */
    fun onColorSelected(newColor: Color) {
        _editingCylinder.value = _editingCylinder.value.changeColor(newColor)
    }

    /**
     * Dynamically changes color by index from the available colors list.
     */
    fun onColorIndexSelected(index: Int) {
        _editingCylinder.value = _editingCylinder.value.changeColorFromList(availableColors, index)
    }

    /**
     * Dynamically cycles to next color.
     */
    fun onCycleColor() {
        _editingCylinder.value = _editingCylinder.value.cycleNextColor(availableColors)
    }

    /**
     * Updates bought date from DatePicker.
     */
    fun onBoughtDateSelected(date: LocalDate) {
        val current = _editingCylinder.value
        // If usage start date is earlier than new bought date, align it
        val newUsageDate = if (current.usageStartDate < date) date else current.usageStartDate
        _editingCylinder.value = current.copy(
            boughtDate = date,
            usageStartDate = newUsageDate
        )
    }

    /**
     * Updates usage start date from DatePicker.
     */
    fun onUsageStartDateSelected(date: LocalDate) {
        val current = _editingCylinder.value
        _editingCylinder.value = current.copy(usageStartDate = date)
    }

    fun onNotesChanged(notes: String) {
        _editingCylinder.value = _editingCylinder.value.copy(notes = notes)
    }

    fun onSupplierChanged(supplier: String) {
        _editingCylinder.value = _editingCylinder.value.copy(supplier = supplier)
    }

    /**
     * Saves the current editing cylinder as the active cylinder and logs the exchange.
     */
    fun saveActiveCylinder() {
        val current = _editingCylinder.value
        repository.logNewExchange(current)
    }

    /**
     * Select an existing cylinder from history to edit or view.
     */
    fun loadCylinderForEditing(cylinder: GasCylinder) {
        _editingCylinder.value = cylinder
    }

    /**
     * Reset editing state to a fresh new cylinder.
     */
    fun resetToNewCylinder() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        _editingCylinder.value = GasCylinder(
            sizeKg = 9,
            color = availableColors[0],
            boughtDate = today,
            usageStartDate = today,
            notes = "",
            supplier = "Gas Depot",
            isActive = true
        )
    }

    fun deleteCylinder(id: String) {
        repository.deleteCylinder(id)
    }
}
