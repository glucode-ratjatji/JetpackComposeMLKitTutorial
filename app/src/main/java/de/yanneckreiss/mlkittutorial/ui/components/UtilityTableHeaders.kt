package de.yanneckreiss.mlkittutorial.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.yanneckreiss.mlkittutorial.entry.domain.model.SortDirection
import de.yanneckreiss.mlkittutorial.entry.domain.model.SortOption

@Composable
fun UtilityTableHeaders(
    activeSort: SortOption,
    sortDirection: SortDirection,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column 1: Date (Weight 1.0)
            HeaderCell(
                title = "Date",
                sortOption = SortOption.DATE,
                activeSort = activeSort,
                sortDirection = sortDirection,
                onSortChange = onSortChange,
                modifier = Modifier.weight(1.0f),
                alignment = Alignment.Start
            )

            // Column 2: Utility (Weight 1.0)
            HeaderCell(
                title = "Utility",
                sortOption = SortOption.UTILITY,
                activeSort = activeSort,
                sortDirection = sortDirection,
                onSortChange = onSortChange,
                modifier = Modifier.weight(1.0f),
                alignment = Alignment.Start
            )

            // Column 3: Meter Reading (Weight 1.1)
            HeaderCell(
                title = "Meter Rdg",
                sortOption = SortOption.METER_READING,
                activeSort = activeSort,
                sortDirection = sortDirection,
                onSortChange = onSortChange,
                modifier = Modifier.weight(1.1f),
                alignment = Alignment.End
            )

            // Column 4: Units Used (Weight 1.2)
            HeaderCell(
                title = "Units Used",
                sortOption = SortOption.UNITS_USED,
                activeSort = activeSort,
                sortDirection = sortDirection,
                onSortChange = onSortChange,
                modifier = Modifier.weight(1.2f),
                alignment = Alignment.End
            )

            // Column 5: Weather Score (Weight 0.9)
            HeaderCell(
                title = "Weather",
                sortOption = SortOption.WEATHER_SCORE,
                activeSort = activeSort,
                sortDirection = sortDirection,
                onSortChange = onSortChange,
                modifier = Modifier.weight(0.9f),
                alignment = Alignment.End
            )
        }
    }
}

@Composable
private fun HeaderCell(
    title: String,
    sortOption: SortOption,
    activeSort: SortOption,
    sortDirection: SortDirection,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    val isSelected = activeSort == sortOption
    val arrow = if (isSelected) {
        if (sortDirection == SortDirection.DESCENDING) " ▼" else " ▲"
    } else ""

    Box(
        modifier = modifier.clickable { onSortChange(sortOption) },
        contentAlignment = if (alignment == Alignment.End) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = "$title$arrow",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
