package de.yanneckreiss.mlkittutorial.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import java.util.Locale

@Composable
fun UtilityTableRow(
    entry: Entry,
    isEvenRow: Boolean,
    onOpenFullDetail: (Entry) -> Unit,
    onDeleteEntry: (Entry) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val utility = entry.detectedUtilityType
    val rowBackground = if (isExpanded) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    } else if (isEvenRow) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surface
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
                // Column 1: Date (Weight 1.0)
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = entry.date.takeLast(5).ifEmpty { entry.date },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (entry.date.length >= 10) entry.date.take(4) else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Column 2: Utility (Weight 1.0)
                Row(
                    modifier = Modifier.weight(1.0f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = utility.emoji,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = when (utility.name) {
                            "ELECTRICITY" -> "Power"
                            "WATER" -> "Water"
                            "GAS" -> "Gas"
                            else -> utility.displayName
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = utility.composeColor,
                        maxLines = 1
                    )
                }

                // Column 3: Meter Reading (Weight 1.1)
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.meterReading),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "reading",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Column 4: Units Used (Weight 1.2)
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", entry.unitsUsed),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${entry.unitLabel} used",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Column 5: Weather Score Badge (Weight 0.9)
                Box(
                    modifier = Modifier.weight(0.9f),
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
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )
    }
}
