package de.yanneckreiss.mlkittutorial.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import java.util.Locale

@SuppressLint("ComposeParameterOrder")
@Composable
fun UtilityTableRow(
    entry: Entry,
    isEvenRow: Boolean,
    baselineAverage: Double = 0.0,
    onOpenFullDetail: (Entry) -> Unit,
    onDeleteEntry: (Entry) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val utility = entry.detectedUtilityType
    val isSpike = baselineAverage > 0.0 && entry.unitsUsed > (baselineAverage * 1.25)
    val spikePercentage = if (baselineAverage > 0.0) {
        (((entry.unitsUsed - baselineAverage) / baselineAverage) * 100).toInt()
    } else 0

    val rowBackground = if (isExpanded) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    } else if (isEvenRow) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val utilityIcon = when (utility) {
        UtilityType.ELECTRICITY -> Icons.Default.FlashOn
        UtilityType.WATER -> Icons.Default.WaterDrop
        UtilityType.GAS -> Icons.Default.LocalFireDepartment
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = rowBackground,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Date & Status Tag (Weight 1.1)
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = entry.date.takeLast(5).ifEmpty { entry.date },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (entry.isActual) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = if (entry.isActual) "Actual" else "Est. Avg",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (entry.isActual) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Column 2: Utility (Weight 0.95)
                Row(
                    modifier = Modifier.weight(0.95f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = utilityIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = when (utility) {
                            UtilityType.ELECTRICITY -> "Power"
                            UtilityType.WATER -> "Water"
                            UtilityType.GAS -> "Gas"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                // Column 3: Meter Reading (Weight 1.05)
                Column(
                    modifier = Modifier.weight(1.05f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.meterReading),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "rdg",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                // Column 4: Units Used & Anomaly Badge (Weight 1.25)
                Column(
                    modifier = Modifier.weight(1.25f),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", entry.unitsUsed),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = entry.unitLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Yellow/Red anomaly warning badge ONLY for consumption spike (>25% above average)
                    if (isSpike) {
                        val isHighError = spikePercentage > 40
                        val badgeBg = if (isHighError) MaterialTheme.colorScheme.errorContainer else Color(0xFFFFF8E1)
                        val badgeText = if (isHighError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFFF57F17)

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeBg,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "+$spikePercentage%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = badgeText,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Column 5: Expandable Weather Score Badge (Weight 0.85)
                Box(
                    modifier = Modifier.weight(0.85f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    WeatherScoreBadge(
                        score = entry.weatherScore,
                        onClick = { isExpanded = !isExpanded }
                    )
                }
            }
        }

        // Animated Inline Weather Score Breakdown Expansion Panel
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            WeatherBreakdownCard(
                entry = entry,
                onOpenFullDetail = { onOpenFullDetail(entry) },
                onDelete = { onDeleteEntry(entry) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        Divider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            thickness = 0.5.dp
        )
    }
}
