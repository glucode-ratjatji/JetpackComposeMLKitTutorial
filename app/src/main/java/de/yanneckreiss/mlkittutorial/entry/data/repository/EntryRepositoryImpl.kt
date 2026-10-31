package de.yanneckreiss.mlkittutorial.entry.data.repository

import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.EntryDao
import de.yanneckreiss.mlkittutorial.entry.domain.repository.IEntryRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class EntryRepositoryImpl @Inject constructor(
    private val entryDao: EntryDao
) : IEntryRepository {

    override fun getAllEntries(): Flow<List<Entry>> {
        return entryDao.getAll()
    }

    override suspend fun insertEntry(newEntry: Entry): Flow<List<Entry>> {
        entryDao.insertAll(newEntry)
        return getAllEntries()
    }

    override suspend fun insertList(entries: List<Entry>) {
        entryDao.insertList(entries)
    }

    override suspend fun deleteEntry(entry: Entry): Flow<List<Entry>> {
        entryDao.delete(entry)
        return getAllEntries()
    }

    override suspend fun deleteById(id: Int) {
        entryDao.deleteById(id)
    }

    override suspend fun updateEntry(entry: Entry) {
        entryDao.updateEntry(entry)
    }

    override suspend fun seedSampleData() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        val sampleEntries = mutableListOf<Entry>()

        for (i in 13 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(cal.time)
            val timestampStr = cal.timeInMillis.toString()
            val weekNum = cal.get(Calendar.WEEK_OF_YEAR)
            val monthKey = (cal.get(Calendar.MONTH) + 1).toString()

            val temp = when (i) {
                0, 1 -> 31.5
                2, 3 -> 29.0
                4, 5 -> 18.0
                6, 7 -> 12.0
                8, 9 -> 13.5
                else -> 22.0
            }
            val humidity = when (i) {
                4, 5 -> 88.0
                0, 1 -> 42.0
                else -> 55.0
            }
            val rain = when (i) {
                4 -> 14.2
                5 -> 6.8
                else -> 0.0
            }
            val cloud = when (i) {
                4, 5 -> 90.0
                else -> 25.0
            }

            // 1. Electricity (kWh)
            val electricUnitsUsed = when {
                temp >= 30.0 -> 24.8
                temp <= 14.0 -> 22.4
                else -> 13.5 + ((i % 3) * 0.8)
            }
            val electricWeatherScore = when {
                temp >= 30.0 -> 8.8
                temp <= 14.0 -> 8.2
                else -> 3.5
            }
            val electricReading = 14200.0 + (14 - i) * 18.0
            sampleEntries.add(
                Entry(
                    timestamp = timestampStr,
                    date = dateStr,
                    weekNumber = weekNum,
                    monthKey = monthKey,
                    meterReading = electricReading,
                    previousReadingValue = electricReading - electricUnitsUsed,
                    unitsConsumedSinceLastVisit = electricUnitsUsed,
                    daysElapsedSinceLastReading = 1,
                    weatherScore = electricWeatherScore,
                    temperatureCelsius = temp,
                    cloudCoverage = cloud,
                    precipitation = rain,
                    windSpeedKms = 11.0,
                    description = "[Electricity] " + if (temp >= 30.0) "Heatwave HVAC cooling" else if (temp <= 14.0) "Cold snap geyser heating" else "Normal daily consumption"
                )
            )

            // 2. Water (L)
            val waterUnitsUsed = when {
                i == 1 -> 780.0 // Simulated anomaly: unclosed valve/leak
                rain > 5.0 -> 160.0
                temp >= 30.0 -> 390.0
                else -> 290.0 + ((i % 4) * 15.0)
            }
            val waterWeatherScore = when {
                rain > 5.0 -> 2.1
                temp >= 30.0 -> 7.9
                else -> 4.0
            }
            val waterReading = 845000.0 + (14 - i) * 310.0
            sampleEntries.add(
                Entry(
                    timestamp = (cal.timeInMillis + 3600000).toString(),
                    date = dateStr,
                    weekNumber = weekNum,
                    monthKey = monthKey,
                    meterReading = waterReading,
                    previousReadingValue = waterReading - waterUnitsUsed,
                    unitsConsumedSinceLastVisit = waterUnitsUsed,
                    daysElapsedSinceLastReading = 1,
                    weatherScore = waterWeatherScore,
                    temperatureCelsius = temp,
                    cloudCoverage = cloud,
                    precipitation = rain,
                    windSpeedKms = 9.0,
                    description = "[Water] " + if (i == 1) "Unusual elevated flow rate (potential leak)" else if (rain > 5.0) "Rainy day - outdoor irrigation paused" else "Routine domestic usage"
                )
            )

            // 3. Propane Gas (kg)
            val gasUnitsUsed = when {
                temp <= 14.0 -> 1.85
                else -> 0.75 + ((i % 2) * 0.2)
            }
            val gasWeatherScore = if (temp <= 14.0) 7.5 else 3.0
            val gasReading = 38.0 + (14 - i) * 1.0
            sampleEntries.add(
                Entry(
                    timestamp = (cal.timeInMillis + 7200000).toString(),
                    date = dateStr,
                    weekNumber = weekNum,
                    monthKey = monthKey,
                    meterReading = gasReading,
                    previousReadingValue = gasReading - gasUnitsUsed,
                    unitsConsumedSinceLastVisit = gasUnitsUsed,
                    daysElapsedSinceLastReading = 1,
                    weatherScore = gasWeatherScore,
                    temperatureCelsius = temp,
                    cloudCoverage = cloud,
                    precipitation = rain,
                    windSpeedKms = 14.0,
                    description = "[Gas] " + if (temp <= 14.0) "Geyser water heating in cold weather" else "Gas stove cooking"
                )
            )
        }

        entryDao.insertList(sampleEntries)
    }
}
