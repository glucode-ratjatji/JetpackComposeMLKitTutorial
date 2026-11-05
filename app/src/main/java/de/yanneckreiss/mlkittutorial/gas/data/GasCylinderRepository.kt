package de.yanneckreiss.mlkittutorial.gas.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import de.yanneckreiss.mlkittutorial.gas.model.DefaultGasCylinderColors
import de.yanneckreiss.mlkittutorial.gas.model.GasCylinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data Transfer Object for JSON persistence of GasCylinder.
 */
data class GasCylinderDto(
    val id: String,
    val sizeKg: Int,
    val colorArgb: Long,
    val boughtDate: String,
    val usageStartDate: String,
    val notes: String,
    val supplier: String,
    val isActive: Boolean
)

@Singleton
class GasCylinderRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("gas_cylinder_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _activeCylinder = MutableStateFlow(loadActiveCylinder())
    val activeCylinder: StateFlow<GasCylinder> = _activeCylinder.asStateFlow()

    private val _cylinderHistory = MutableStateFlow(loadHistory())
    val cylinderHistory: StateFlow<List<GasCylinder>> = _cylinderHistory.asStateFlow()

    private fun loadActiveCylinder(): GasCylinder {
        val json = prefs.getString("active_cylinder_data", null)
        if (json != null) {
            try {
                val dto = gson.fromJson(json, GasCylinderDto::class.java)
                return dto.toDomain()
            } catch (e: Exception) {
                // fallback to default
            }
        }
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return GasCylinder(
            sizeKg = 9,
            color = DefaultGasCylinderColors[0],
            boughtDate = today,
            usageStartDate = today,
            notes = "Standard 9kg propane cylinder for cooking and heating",
            supplier = "Gas Depot",
            isActive = true
        )
    }

    private fun loadHistory(): List<GasCylinder> {
        val json = prefs.getString("cylinder_history_data", null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<GasCylinderDto>>() {}.type
                val dtoList: List<GasCylinderDto> = gson.fromJson(json, type)
                return dtoList.map { it.toDomain() }
            } catch (e: Exception) {
                // fallback to empty list
            }
        }
        return emptyList()
    }

    private fun saveActiveCylinderToPrefs(cylinder: GasCylinder) {
        val json = gson.toJson(cylinder.toDto())
        prefs.edit().putString("active_cylinder_data", json).apply()
    }

    private fun saveHistoryToPrefs(list: List<GasCylinder>) {
        val json = gson.toJson(list.map { it.toDto() })
        prefs.edit().putString("cylinder_history_data", json).apply()
    }

    fun saveActiveCylinder(cylinder: GasCylinder) {
        _activeCylinder.value = cylinder
        saveActiveCylinderToPrefs(cylinder)

        // Also add or update in history list
        val currentList = _cylinderHistory.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == cylinder.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = cylinder
        } else {
            currentList.add(0, cylinder)
        }
        _cylinderHistory.value = currentList
        saveHistoryToPrefs(currentList)
    }

    fun logNewExchange(newCylinder: GasCylinder) {
        // Mark previous active cylinder as inactive
        val currentActive = _activeCylinder.value.copy(isActive = false)
        val history = _cylinderHistory.value.toMutableList()

        val activeIdx = history.indexOfFirst { it.id == currentActive.id }
        if (activeIdx >= 0) {
            history[activeIdx] = currentActive
        } else {
            history.add(0, currentActive)
        }

        // Add new cylinder
        history.add(0, newCylinder.copy(isActive = true))
        _cylinderHistory.value = history
        saveHistoryToPrefs(history)

        _activeCylinder.value = newCylinder.copy(isActive = true)
        saveActiveCylinderToPrefs(newCylinder.copy(isActive = true))
    }

    fun deleteCylinder(cylinderId: String) {
        val history = _cylinderHistory.value.filter { it.id != cylinderId }
        _cylinderHistory.value = history
        saveHistoryToPrefs(history)

        if (_activeCylinder.value.id == cylinderId) {
            val nextActive = history.firstOrNull() ?: GasCylinder()
            _activeCylinder.value = nextActive
            saveActiveCylinderToPrefs(nextActive)
        }
    }

    private fun GasCylinder.toDto(): GasCylinderDto {
        return GasCylinderDto(
            id = id,
            sizeKg = sizeKg,
            colorArgb = color.toArgb().toLong(),
            boughtDate = boughtDate.toString(),
            usageStartDate = usageStartDate.toString(),
            notes = notes,
            supplier = supplier,
            isActive = isActive
        )
    }

    private fun GasCylinderDto.toDomain(): GasCylinder {
        val parsedBoughtDate = try {
            LocalDate.parse(boughtDate)
        } catch (e: Exception) {
            Clock.System.todayIn(TimeZone.currentSystemDefault())
        }
        val parsedUsageDate = try {
            LocalDate.parse(usageStartDate)
        } catch (e: Exception) {
            Clock.System.todayIn(TimeZone.currentSystemDefault())
        }

        return GasCylinder(
            id = id,
            sizeKg = sizeKg,
            color = Color(colorArgb.toInt()),
            boughtDate = parsedBoughtDate,
            usageStartDate = parsedUsageDate,
            notes = notes,
            supplier = supplier,
            isActive = isActive
        )
    }
}
