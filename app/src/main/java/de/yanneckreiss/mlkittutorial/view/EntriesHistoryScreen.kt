package de.yanneckreiss.mlkittutorial.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel.EntryViewModel
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import de.yanneckreiss.mlkittutorial.entry.domain.model.ChartDataPoint
import de.yanneckreiss.mlkittutorial.entry.domain.model.TrendDirection
import de.yanneckreiss.mlkittutorial.ui.components.*

@SuppressLint("ComposeModifierMissing")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriesHistoryScreen(
    viewModel: EntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Reactive StateFlows directly from Room database
    val entries by viewModel.filteredAndSortedEntries.collectAsStateWithLifecycle()
    val allRawEntries by viewModel.visibleEntries.collectAsStateWithLifecycle()
    val selectedUtility by viewModel.selectedUtilityFilter.collectAsStateWithLifecycle()
    val selectedHorizon by viewModel.selectedTimeHorizon.collectAsStateWithLifecycle()
    val selectedSortOption by viewModel.selectedSortOption.collectAsStateWithLifecycle()
    val sortDirection by viewModel.sortDirection.collectAsStateWithLifecycle()
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val trendSummary by viewModel.trendSummary.collectAsStateWithLifecycle()
    val consumptionVelocity by viewModel.consumptionVelocity.collectAsStateWithLifecycle()
    val activeAnomalies by viewModel.activeAnomalies.collectAsStateWithLifecycle()
    val selectedEntryForDetail by viewModel.selectedEntryForDetail.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()

    val currentUtilityType = selectedUtility ?: UtilityType.ELECTRICITY

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Utility Ledger & Records",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Units Consumed & Weather Correlation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    // Seed Sample Data Button
                    IconButton(
                        onClick = { viewModel.seedDemoData() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Dataset, contentDescription = "Seed Sample Data")
                    }

                    // Run Anomaly Scan
                    IconButton(
                        onClick = { viewModel.triggerAnomalyWorker(context) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(imageVector = Icons.Default.AutoGraph, contentDescription = "Run Anomaly Detection")
                    }

                    // Export CSV
                    IconButton(
                        onClick = { viewModel.exportData(context) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export CSV Report")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.setShowAddDialog(true) },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Log Utility Reading") },
                text = { Text("Log Reading", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (allRawEntries.isEmpty()) {
                EmptyTrackingState(
                    onSeedData = { viewModel.seedDemoData() },
                    onAddReading = { viewModel.setShowAddDialog(true) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                ) {
                    // 1. Proactive Anomaly Alerts (if any)
                    if (activeAnomalies.isNotEmpty()) {
                        item(key = "anomalies_banner") {
                            AnomalyAlertBanner(
                                anomalies = activeAnomalies,
                                onDismissAlert = { viewModel.dismissAnomaly(it) }
                            )
                        }
                    }

                    // 2. Units Consumption Velocity Card
                    item(key = "consumption_velocity_card") {
                        ConsumptionVelocityCard(velocity = consumptionVelocity)
                    }

                    // 3. Analytics & Interactive Chart Section
                    item(key = "chart_analytics_section") {
                        AnalyticsSection(
                            trendSummary = trendSummary,
                            chartData = chartData,
                            utilityType = currentUtilityType
                        )
                    }

                    // 4. Filters & Controls Bar
                    item(key = "utility_filters_bar") {
                        UtilityFilterBar(
                            selectedUtility = selectedUtility,
                            onSelectUtility = { viewModel.setUtilityFilter(it) },
                            selectedHorizon = selectedHorizon,
                            onSelectHorizon = { viewModel.setTimeHorizon(it) },
                            selectedSortOption = selectedSortOption,
                            onSelectSortOption = { viewModel.setSortOption(it) },
                            sortDirection = sortDirection,
                            onToggleSortDirection = { viewModel.toggleSortDirection() }
                        )
                    }

                    // 5. Sticky Table Headers (Date, Utility, Meter Reading, Units Used, Weather)
                    item(key = "table_headers") {
                        UtilityTableHeaders(
                            activeSort = selectedSortOption,
                            sortDirection = sortDirection,
                            onSortChange = { viewModel.setSortOption(it) }
                        )
                    }

                    // 6. Tabular Rows with Expandable Weather Breakdown
                    if (entries.isEmpty()) {
                        item(key = "no_filtered_entries") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No records match current filter criteria.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = entries,
                            key = { _, entry -> entry.id }
                        ) { index, entry ->
                            UtilityTableRow(
                                entry = entry,
                                isEvenRow = index % 2 == 0,
                                onOpenFullDetail = { viewModel.openDetailSheet(it) },
                                onDeleteEntry = { viewModel.deleteEntry(it) }
                            )
                        }
                    }
                }
            }
        }

        // Full Weather Score Detail Modal Bottom Sheet
        selectedEntryForDetail?.let { entry ->
            WeatherScoreBottomSheet(
                entry = entry,
                onDismiss = { viewModel.closeDetailSheet() }
            )
        }

        // Add Utility Reading Dialog
        if (showAddDialog) {
            AddUtilityReadingDialog(
                initialUtilityType = selectedUtility ?: UtilityType.ELECTRICITY,
                onDismiss = { viewModel.setShowAddDialog(false) },
                onConfirm = { type, reading, desc, temp, score ->
                    viewModel.addManualReading(type, reading, desc, temp, score)
                }
            )
        }
    }
}

@Composable
private fun AnalyticsSection(
    trendSummary: de.yanneckreiss.mlkittutorial.entry.domain.model.TrendSummary?,
    chartData: List<ChartDataPoint>,
    utilityType: UtilityType
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Trend Headline & Indicator Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = utilityType.emoji, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${utilityType.displayName} Usage Trend",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (trendSummary != null) {
                    val isIncreasing = trendSummary.direction == TrendDirection.INCREASING
                    val isDecreasing = trendSummary.direction == TrendDirection.DECREASING

                    val pillBg = when {
                        isIncreasing -> MaterialTheme.colorScheme.errorContainer
                        isDecreasing -> Color(0xFFE8F5E9)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val pillText = when {
                        isIncreasing -> MaterialTheme.colorScheme.onErrorContainer
                        isDecreasing -> Color(0xFF2E7D32)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val arrow = when {
                        isIncreasing -> "▲"
                        isDecreasing -> "▼"
                        else -> "■"
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = pillBg
                    ) {
                        Text(
                            text = "$arrow ${if (trendSummary.percentageChange >= 0) "+" else ""}${trendSummary.percentageChange}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = pillText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Summary Subtitle focused on Units Used
            if (trendSummary != null) {
                Text(
                    text = "Average: ${trendSummary.averageUnitsUsed} ${trendSummary.unit} • Total: ${trendSummary.totalPeriodUnits} ${trendSummary.unit} • Peak: ${trendSummary.peakUnits} ${trendSummary.unit} (${trendSummary.comparisonPeriodLabel})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Interactive Chart
            InteractiveUtilityChart(
                dataPoints = chartData,
                utilityType = utilityType,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyTrackingState(
    onSeedData: () -> Unit,
    onAddReading: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "📊", style = MaterialTheme.typography.displaySmall)

                Text(
                    text = "No Utility Entries Yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Track Electricity (kWh), Water (L), and Propane Gas (kg) in high-density tables with interactive charts and automated weather correlation analysis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onSeedData,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Load Realistic Demo Records (14 Days)")
                }

                OutlinedButton(
                    onClick = onAddReading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Record First Manual Entry")
                }
            }
        }
    }
}
