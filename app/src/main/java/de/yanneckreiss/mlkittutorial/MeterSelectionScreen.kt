package de.yanneckreiss.mlkittutorial

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ComposeModifierMissing", "ComposeViewModelInjection")
@Composable
fun MeterSelectionScreen(
    onNavigateToElectricity: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToGas: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onQuickLog: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text("House Check", fontWeight = FontWeight.Bold)
                        Text(
                            "Utility Tracking & Analytics",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Rapid Daily Logging Quick-Action Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Quick Daily Log",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Record readings rapidly without navigating deep menus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onQuickLog,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Now", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Electricity Card
            MeterNavigationCard(
                icon = Icons.Default.FlashOn,
                header = "Log Electricity Meter",
                description = "Scan meter camera view or enter kilowatt-hour (kWh) readings.",
                onClick = onNavigateToElectricity
            )

            // Water Card
            MeterNavigationCard(
                icon = Icons.Default.WaterDrop,
                header = "Log Water Meter",
                description = "Snap water meter photo & input reading to estimate usage.",
                onClick = onNavigateToWater
            )
            // Gas Cylinder Card
            MeterNavigationCard(
                emoji = "♨️",
                header = "Gas Cylinder & Usage Tracker",
                description = "Track propane cylinder exchanges (4, 9, 14, 48kg) & annual gas usage.",
                onClick = onNavigateToGas
            )
        }
    }
}
