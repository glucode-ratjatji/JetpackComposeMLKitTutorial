package de.yanneckreiss.mlkittutorial.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UtilityExportManager {

    fun exportToCsvAndShare(context: Context, entries: List<Entry>) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "utility_records_$timestamp.csv")

            FileWriter(file).use { writer ->
                // Write CSV Header without cost
                writer.append("ID,Date,Timestamp,Utility_Type,Meter_Reading,Units_Used,Unit,Weather_Score,Temperature_C,Cloud_Cover_Pct,Precipitation_mm,Wind_Speed_kmh,Description\n")

                for (entry in entries) {
                    val utility = entry.detectedUtilityType
                    writer.append("${entry.id},")
                    writer.append("\"${entry.date}\",")
                    writer.append("\"${entry.timestamp}\",")
                    writer.append("\"${utility.displayName}\",")
                    writer.append("${entry.meterReading},")
                    writer.append("${entry.unitsUsed},")
                    writer.append("\"${entry.unitLabel}\",")
                    writer.append("${entry.weatherScore},")
                    writer.append("${entry.temperatureCelsius},")
                    writer.append("${entry.cloudCoverage},")
                    writer.append("${entry.precipitation},")
                    writer.append("${entry.windSpeedKms},")
                    writer.append("\"${entry.description.replace("\"", "\"\"")}\"\n")
                }
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Utility Tracking Export ($timestamp)")
                putExtra(Intent.EXTRA_TEXT, "Attached is your exported utility records CSV log (${entries.size} entries).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export Utility Records (CSV)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            sharePlainTextReport(context, entries)
        }
    }

    fun sharePlainTextReport(context: Context, entries: List<Entry>) {
        val totalUnits = entries.sumOf { it.unitsUsed }
        val sb = StringBuilder()
        sb.append("=== UTILITY USAGE REPORT ===\n")
        sb.append("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
        sb.append("Total Logged Entries: ${entries.size}\n")
        sb.append("Total Units Used Across All Records: ${String.format(Locale.US, "%.1f", totalUnits)}\n\n")

        val grouped = entries.groupBy { it.detectedUtilityType }
        for ((type, list) in grouped) {
            val totalCons = list.sumOf { it.unitsUsed }
            sb.append("${type.emoji} ${type.displayName}:\n")
            sb.append("  - Total Units Used: ${String.format(Locale.US, "%.1f", totalCons)} ${type.defaultUnit}\n")
            sb.append("  - Average Units Per Record: ${String.format(Locale.US, "%.1f", list.map { it.unitsUsed }.average())} ${type.defaultUnit}\n")
            sb.append("  - Average Weather Score: ${String.format(Locale.US, "%.1f", list.map { it.weatherScore }.average())}\n\n")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Utility Tracking Summary Report")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }

        val chooser = Intent.createChooser(shareIntent, "Share Summary Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
