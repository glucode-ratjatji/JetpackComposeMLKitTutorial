package de.yanneckreiss.mlkittutorial

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel.EntryViewModel
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import de.yanneckreiss.mlkittutorial.ui.MainScreen
import de.yanneckreiss.mlkittutorial.view.EntriesHistoryScreen
import de.yanneckreiss.mlkittutorial.view.WaterTrackerScreen

object AppRoutes {
    const val METER_SELECTION = "meter_selection"
    const val ELECTRICITY_CAMERA = "electricity_camera"
    const val WATER_METER = "water_meter"
    const val GAS_METER = "gas_meter"
    const val ENTRIES_HISTORY = "entries_history"
}

@SuppressLint("ComposeViewModelInjection")
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavHost(viewModel: EntryViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppRoutes.METER_SELECTION
    ) {
        // Selection Screen (Home Dashboard)
        composable(AppRoutes.METER_SELECTION) {
            MeterSelectionScreen(
                onNavigateToElectricity = {
                    navController.navigate(AppRoutes.ELECTRICITY_CAMERA)
                },
                onNavigateToWater = {
                    navController.navigate(AppRoutes.WATER_METER)
                },
                onNavigateToGas = {
//                    viewModel.setUtilityFilter(UtilityType.GAS)
//                    viewModel.setShowAddDialog(true)
                    navController.navigate(AppRoutes.GAS_METER)
                },
                onNavigateToHistory = {
                    viewModel.setUtilityFilter(null)
                    navController.navigate(AppRoutes.ENTRIES_HISTORY)
                },
                onQuickLog = {
                    viewModel.setShowAddDialog(true)
                    navController.navigate(AppRoutes.ENTRIES_HISTORY)
                }
            )
        }

        // Electricity Camera Scanner Screen (CameraX + OCR)
        composable(AppRoutes.ELECTRICITY_CAMERA) {
            MainScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Water Tracker Screen (CameraX Photo + Manual Input + Estimation)
        composable(AppRoutes.WATER_METER) {
            WaterTrackerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Gas Screen: routes to ledger with Gas filter & dialog
        composable(AppRoutes.GAS_METER) {
            viewModel.setUtilityFilter(UtilityType.GAS)
            viewModel.setShowAddDialog(true)
            EntriesHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Tabular Usage Ledger, Graphs & Weather Correlation
        composable(AppRoutes.ENTRIES_HISTORY) {
            EntriesHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
