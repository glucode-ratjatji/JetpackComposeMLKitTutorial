package de.yanneckreiss.mlkittutorial

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.yanneckreiss.mlkittutorial.ui.MainScreen

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ComposeModifierMissing")
@Composable
fun MeterSelectionScreen(
    onNavigateToElectricity: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToGas: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TopAppBar(
                title = { Text("House Keeper 🤵‍♂️") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Electricity Card
            MeterNavigationCard(
                emoji = "⚡",
                header = "Log Electricity Meter",
                description = "Record current kilowatt-hour (kWh) readings and track usage.",
                onClick =  onNavigateToElectricity
            )

            // Water Card
            MeterNavigationCard(
                emoji = "💧",
                header = "Log Water Meter",
                description = "Record current cubic meter (m³) readings to monitor consumption.",
                onClick =  onNavigateToWater
            )
            // Water Card
            MeterNavigationCard(
                emoji = "♨️",
                header = "Log Gas exchange",
                description = "Record when you last changed your gas cylinder.",
                onClick =  onNavigateToGas
            )
        }
    }
}