package de.yanneckreiss.mlkittutorial.entry.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.isoDayNumber
@Entity(tableName = "entry")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "entry_timestamp") val timestamp: String = "",
    @ColumnInfo(name = "entry_date") val date: String = "",
    @ColumnInfo(name = "entry_weekNumber") val weekNumber: Int = 0,
    @ColumnInfo(name = "entry_monthKey") val monthKey: String = "",

    @ColumnInfo(name = "entry_meterReading") val meterReading: Double = 0.0,
    @ColumnInfo(name = "entry_image_uri") val imageUri: String = "",

    @ColumnInfo(name = "entry_previous_reading_value") val previousReadingValue: Double = 0.0,
    @ColumnInfo(name = "entry_units_consumed_since_last_visit") val unitsConsumedSinceLastVisit: Double = 0.0,
    @ColumnInfo(name = "entry_days_elapsed_since_last_reading") val daysElapsedSinceLastReading: Int = 0,

    @ColumnInfo(name = "entry_weather_score") val weatherScore: Double = 0.0,
    @ColumnInfo(name = "entry_temperature_celcius") val temperatureCelsius: Double = 0.0,
    @ColumnInfo(name = "entry_cloud_coverage") val cloudCoverage: Double = 0.0,
    @ColumnInfo(name = "entry_precipitation") val precipitation: Double = 0.0,
    @ColumnInfo(name = "entry_wind_speed_kms") val windSpeedKms: Double = 0.0,
    @ColumnInfo(name = "entry_visibility_kms") val visibilityKms: Double = 0.0,

    @ColumnInfo(name = "entry_description") val description: String = ""
)

fun LocalDate.toStartOfWeek(): LocalDate {
    val daysToSubtract = this.dayOfWeek.isoDayNumber - 1
    return this.minus(DatePeriod(days = daysToSubtract))
}
