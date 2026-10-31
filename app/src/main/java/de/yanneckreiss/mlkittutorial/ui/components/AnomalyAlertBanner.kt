package de.yanneckreiss.mlkittutorial.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.yanneckreiss.mlkittutorial.entry.domain.model.AnomalyAlert
import de.yanneckreiss.mlkittutorial.entry.domain.model.AnomalySeverity

@Composable
fun AnomalyAlertBanner(
    anomalies: List<AnomalyAlert>,
    onDismissAlert: (AnomalyAlert) -> Unit,
    modifier: Modifier = Modifier
) {
    if (anomalies.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (anomaly in anomalies) {
            AnomalyItemCard(
                anomaly = anomaly,
                onDismiss = { onDismissAlert(anomaly) }
            )
        }
    }
}

@Composable
private fun AnomalyItemCard(
    anomaly: AnomalyAlert,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isCritical = anomaly.severity == AnomalySeverity.CRITICAL
    val containerColor = if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
    val onContainerColor = if (isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isCritical) "🚨" else "⚠️",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = anomaly.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onContainerColor
                    )
                }

                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = onContainerColor.copy(alpha = 0.7f)
                )
            }

            Text(
                text = anomaly.message,
                style = MaterialTheme.typography.bodySmall,
                color = onContainerColor
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 Actionable Mitigation Advice:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = anomaly.actionableAdvice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss Alert", color = onContainerColor)
                        }
                    }
                }
            }
        }
    }
}
