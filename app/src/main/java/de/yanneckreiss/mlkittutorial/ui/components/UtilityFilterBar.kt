package de.yanneckreiss.mlkittutorial.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import de.yanneckreiss.mlkittutorial.entry.domain.model.SortDirection
import de.yanneckreiss.mlkittutorial.entry.domain.model.SortOption
import de.yanneckreiss.mlkittutorial.entry.domain.model.TimeHorizon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityFilterBar(
    selectedUtility: UtilityType?,
    onSelectUtility: (UtilityType?) -> Unit,
    selectedHorizon: TimeHorizon,
    onSelectHorizon: (TimeHorizon) -> Unit,
    selectedSortOption: SortOption,
    onSelectSortOption: (SortOption) -> Unit,
    sortDirection: SortDirection,
    onToggleSortDirection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Horizon Selection Tabs
        SingleChoiceSegmentedControl(
            selectedHorizon = selectedHorizon,
            onSelectHorizon = onSelectHorizon
        )

        // Utility Filter Chips + Sort Direction Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All" filter chip
            FilterChip(
                selected = selectedUtility == null,
                onClick = { onSelectUtility(null) },
                label = { Text("All Utilities") }
            )

            // Utility Type specific chips
            UtilityType.values().forEach { type ->
                FilterChip(
                    selected = selectedUtility == type,
                    onClick = { onSelectUtility(type) },
                    label = { Text("${type.emoji} ${type.displayName}") }
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Sort direction button
            FilledTonalIconButton(
                onClick = onToggleSortDirection,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (sortDirection == SortDirection.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = "Toggle sort direction",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SingleChoiceSegmentedControl(
    selectedHorizon: TimeHorizon,
    onSelectHorizon: (TimeHorizon) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TimeHorizon.values().forEach { horizon ->
                val isSelected = selectedHorizon == horizon
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    onClick = { onSelectHorizon(horizon) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = horizon.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
