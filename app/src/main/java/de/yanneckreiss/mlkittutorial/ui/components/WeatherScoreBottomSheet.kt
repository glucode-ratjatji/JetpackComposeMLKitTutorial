package de.yanneckreiss.mlkittutorial.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.domain.util.WeatherCorrelationEngine
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScoreBottomSheet(
    entry: Entry,
    onDismiss: () -> Unit
) {
    val impact = remember(entry) { WeatherCorrelationEngine.analyzeImpact(entry) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weather Score Diagnostics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Logged on ${entry.date} for ${entry.detectedUtilityType.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Score: ${String.format(Locale.US, "%.1f", entry.weatherScore)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Divider()

            // Weather Impact Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Atmospheric Load Factor: ${impact.heatingCoolingDemand}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = impact.correlationExplanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Detailed Environmental Matrix
            Text(
                text = "Recorded Atmospheric Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TelemetryRow("🌡️ Ambient Temperature", "${entry.temperatureCelsius} °C", "Baseline room target: 21°C")
                    TelemetryRow("💧 Relative Humidity", "${impact.humidity.toInt()}%", "Comfort band: 30%–60%")
                    TelemetryRow("☁️ Cloud Coverage", "${entry.cloudCoverage.toInt()}%", "Solar irradiance yield factor")
                    TelemetryRow("🌧️ Precipitation", "${entry.precipitation} mm", "Suppresses outdoor water demand")
                    TelemetryRow("💨 Wind Velocity", "${entry.windSpeedKms.toInt()} km/h", "Accelerates surface heat loss")
                    TelemetryRow("👁️ Atmospheric Visibility", "${entry.visibilityKms.toInt()} km", "Standard clear-sky index")
                }
            }

            // Proactive Mitigation Recommendations
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "💡 Efficiency Recommendation",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val tip = when {
                        entry.temperatureCelsius >= 28.0 -> "Set thermostat to 24°C rather than 19°C. Each 1°C increase saves ~7% of cooling power consumption."
                        entry.temperatureCelsius <= 14.0 -> "Insulate geyser hot water tank and pipes. Turn geyser thermostat to 55°C to reduce continuous thermal loss."
                        entry.precipitation > 5.0 -> "Rain sensor active: Pause automated garden irrigation cycles for 48 hours to conserve water."
                        else -> "Current weather is nominal. Operating at optimal thermal and hydraulic baselines."
                    }
                    Text(text = tip, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text("Close Diagnostics")
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
