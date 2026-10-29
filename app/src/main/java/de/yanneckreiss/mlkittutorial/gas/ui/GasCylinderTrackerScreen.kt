package de.yanneckreiss.mlkittutorial.gas.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel.EntryViewModel
import de.yanneckreiss.mlkittutorial.gas.model.CylinderSizeInfoList
import de.yanneckreiss.mlkittutorial.gas.model.GasCylinder
import de.yanneckreiss.mlkittutorial.gas.viewmodel.GasCylinderViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@SuppressLint("ComposeModifierMissing")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GasCylinderTrackerScreen(
    onNavigateBack: () -> Unit,
    gasViewModel: GasCylinderViewModel = hiltViewModel(),
    entryViewModel: EntryViewModel = hiltViewModel()
) {
    val editingCylinder by gasViewModel.editingCylinder.collectAsStateWithLifecycle()
    val activeCylinder by gasViewModel.activeCylinder.collectAsStateWithLifecycle()
    val cylinderHistory by gasViewModel.cylinderHistory.collectAsStateWithLifecycle()
    val yearlyStats by gasViewModel.yearlyStats.collectAsStateWithLifecycle()
    val electricityEntries by entryViewModel.visibleEntries.collectAsStateWithLifecycle()

    // Date picker dialog states
    var showBoughtDatePicker by remember { mutableStateOf(false) }
    var showUsageDatePicker by remember { mutableStateOf(false) }

    // Dropdown state for cylinder size selection
    var sizeDropdownExpanded by remember { mutableStateOf(false) }

    // Snackbar host state for feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Calculate electricity total logged kWh
    val totalElectricityKwh = remember(electricityEntries) {
        electricityEntries.sumOf { it.meterReading }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gas Cylinder Tracker",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Propane & Utility Tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Home"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { gasViewModel.onCycleColor() }) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Cycle Cylinder Color",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========================================================
            // 1. YEARLY TRACKING OVERVIEW (GAS & ELECTRICITY)
            // ========================================================
            item {
                YearlyUsageOverviewCard(
                    currentYear = yearlyStats.currentYear,
                    totalGasKg = yearlyStats.totalGasKg,
                    exchangesCount = yearlyStats.cylinderCount,
                    totalElectricityKwh = totalElectricityKwh,
                    electricityReadingsCount = electricityEntries.size
                )
            }

            // ========================================================
            // 2. HERO PROPANE GAS CYLINDER COMPOSABLE PREVIEW
            // ========================================================
            item {
                CylinderHeroVisualCard(
                    cylinder = editingCylinder,
                    onTapCycleColor = { gasViewModel.onCycleColor() }
                )
            }

            // ========================================================
            // 3. DYNAMIC COLOR PICKER SECTION
            // ========================================================
            item {
                DynamicColorPaletteSection(
                    colors = gasViewModel.availableColors,
                    selectedColor = editingCylinder.color,
                    onColorSelected = { color -> gasViewModel.onColorSelected(color) }
                )
            }

            // ========================================================
            // 4. CYLINDER CONFIGURATION FORM (SIZE & DATES)
            // ========================================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Cylinder Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // 4.1 Size Dropdown Selector (4, 9, 14, 48 kg)
                        ExposedDropdownMenuBox(
                            expanded = sizeDropdownExpanded,
                            onExpandedChange = { sizeDropdownExpanded = !sizeDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "${editingCylinder.sizeKg} kg Gas Cylinder",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Cylinder Size (kg)") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Scale, contentDescription = null)
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = sizeDropdownExpanded,
                                onDismissRequest = { sizeDropdownExpanded = false }
                            ) {
                                CylinderSizeInfoList.forEach { sizeInfo ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = "${sizeInfo.title} Cylinder",
                                                    fontWeight = if (sizeInfo.sizeKg == editingCylinder.sizeKg) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Text(
                                                    text = sizeInfo.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Text(
                                                text = if (sizeInfo.sizeKg == editingCylinder.sizeKg) "✓" else "•",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            gasViewModel.onSizeSelected(sizeInfo.sizeKg)
                                            sizeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 4.2 Date Pickers: Bought Date & Usage Start Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Bought Date Picker Card
                            DatePickerDisplayCard(
                                modifier = Modifier.weight(1f),
                                label = "Bought Date",
                                emoji = "🛒",
                                date = editingCylinder.boughtDate,
                                onClick = { showBoughtDatePicker = true }
                            )

                            // Usage Start Date Picker Card
                            DatePickerDisplayCard(
                                modifier = Modifier.weight(1f),
                                label = "Usage Start",
                                emoji = "🔥",
                                date = editingCylinder.usageStartDate,
                                onClick = { showUsageDatePicker = true }
                            )
                        }

                        // Usage Insights Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Duration in Service",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${editingCylinder.daysInUse} days active",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Stored Prior",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${editingCylinder.daysInStorageBeforeUse} days storage",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Supplier / Depot text field
                        OutlinedTextField(
                            value = editingCylinder.supplier,
                            onValueChange = { gasViewModel.onSupplierChanged(it) },
                            label = { Text("Supplier / Brand (Optional)") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Storefront, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Notes text field
                        OutlinedTextField(
                            value = editingCylinder.notes,
                            onValueChange = { gasViewModel.onNotesChanged(it) },
                            label = { Text("Notes / Location (e.g. Kitchen Stove)") },
                            leadingIcon = {
                                Icon(Icons.Outlined.EditNote, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 2
                        )

                        // Save / Log Cylinder Exchange Button
                        Button(
                            onClick = {
                                gasViewModel.saveActiveCylinder()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Recorded ${editingCylinder.sizeKg} kg cylinder exchange!"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save & Log Cylinder Exchange",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ========================================================
            // 5. YEARLY CYLINDER LOG HISTORY
            // ========================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Yearly Gas Cylinder History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${cylinderHistory.size} logged",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (cylinderHistory.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No past cylinder exchanges recorded yet. Click 'Save & Log' to record your first cylinder.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(cylinderHistory) { cylinder ->
                    CylinderHistoryItemCard(
                        cylinder = cylinder,
                        isActive = cylinder.id == activeCylinder.id,
                        onSelect = { gasViewModel.loadCylinderForEditing(cylinder) },
                        onDelete = { gasViewModel.deleteCylinder(cylinder.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Material 3 Date Picker Dialog for Bought Date
    if (showBoughtDatePicker) {
        Material3DatePickerModal(
            initialDate = editingCylinder.boughtDate,
            title = "Select Bought Date",
            onDateSelected = { selectedDate ->
                gasViewModel.onBoughtDateSelected(selectedDate)
                showBoughtDatePicker = false
            },
            onDismiss = { showBoughtDatePicker = false }
        )
    }

    // Material 3 Date Picker Dialog for Usage Start Date
    if (showUsageDatePicker) {
        Material3DatePickerModal(
            initialDate = editingCylinder.usageStartDate,
            title = "Select Usage Start Date",
            onDateSelected = { selectedDate ->
                gasViewModel.onUsageStartDateSelected(selectedDate)
                showUsageDatePicker = false
            },
            onDismiss = { showUsageDatePicker = false }
        )
    }
}

/**
 * Modern Combined Energy & Utility Overview Card.
 * Displays both Electricity (kWh) and Gas (kg) tracked throughout the year.
 */
@Composable
private fun YearlyUsageOverviewCard(
    currentYear: Int,
    totalGasKg: Int,
    exchangesCount: Int,
    totalElectricityKwh: Double,
    electricityReadingsCount: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📊", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Yearly Utility Tracker ($currentYear)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Annual Stats",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gas Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("♨️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Gas Consumed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$totalGasKg kg",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$exchangesCount cylinders logged",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Electricity Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Electricity",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = String.format("%.1f", totalElectricityKwh),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$electricityReadingsCount meter scans",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hero Card showcasing the custom Propane Gas Cylinder Composable.
 */
@Composable
private fun CylinderHeroVisualCard(
    cylinder: GasCylinder,
    onTapCycleColor: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row with active status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cylinder.isActive) "ACTIVE TANK" else "LOGGED BOTTLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Text(
                    text = "Tap cylinder to cycle color 🎨",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RENDER THE PROPANE GAS CYLINDER COMPOSABLE
            PropaneGasCylinderComposable(
                cylinder = cylinder,
                isInteractive = true,
                onClick = onTapCycleColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Specs badge row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickSpecBadge(label = "Capacity", value = "${cylinder.sizeKg} kg")
                QuickSpecBadge(label = "In Use", value = "${cylinder.daysInUse} days")
                QuickSpecBadge(label = "Est. Left", value = "~${cylinder.estimatedRemainingDays} days")
            }
        }
    }
}

@Composable
private fun QuickSpecBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Dynamic Color Palette Row with interactive swatches.
 */
@Composable
private fun DynamicColorPaletteSection(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dynamic Cylinder Color",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${colors.size} color options",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(colors) { color ->
                    val isSelected = color == selectedColor
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(color) }
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                } else {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = Color.Black.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tappable card displaying a picked date and triggering the date picker modal.
 */
@Composable
private fun DatePickerDisplayCard(
    label: String,
    emoji: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = date.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Tap to change 📅",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * History item card representing a past logged gas cylinder.
 */
@Composable
private fun CylinderHistoryItemCard(
    cylinder: GasCylinder,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color tag dot
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(cylinder.color)
                    .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
            )

            // Info stack
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${cylinder.sizeKg} kg Propane Cylinder",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Bought: ${cylinder.boughtDate} • In use since: ${cylinder.usageStartDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (cylinder.notes.isNotBlank()) {
                    Text(
                        text = cylinder.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete entry",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Material 3 Date Picker Modal Dialog with LocalDate bridging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Material3DatePickerModal(
    initialDate: LocalDate,
    title: String,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = remember(initialDate) {
        val javaDate = java.time.LocalDate.of(initialDate.year, initialDate.monthNumber, initialDate.dayOfMonth)
        javaDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val javaLocalDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        val result = LocalDate(
                            javaLocalDate.year,
                            javaLocalDate.monthValue,
                            javaLocalDate.dayOfMonth
                        )
                        onDateSelected(result)
                    } ?: onDismiss()
                }
            ) {
                Text("Select", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            }
        )
    }
}
