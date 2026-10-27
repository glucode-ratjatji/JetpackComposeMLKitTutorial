package de.yanneckreiss.mlkittutorial

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel.EntryViewModel
import de.yanneckreiss.mlkittutorial.ui.MainScreen
import de.yanneckreiss.mlkittutorial.view.EntriesHistoryScreen

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
        // Selection Screen (Home Hub)
        composable(AppRoutes.METER_SELECTION) {
            MeterSelectionScreen(
                onNavigateToElectricity = {
                    navController.navigate(AppRoutes.ELECTRICITY_CAMERA)
                },
                onNavigateToWater = {
                    navController.navigate(AppRoutes.WATER_METER)
                },
                onNavigateToGas = {
                    navController.navigate(AppRoutes.GAS_METER)
                },
                onNavigateToHistory = {
                    navController.navigate(AppRoutes.ENTRIES_HISTORY)
                }
            )
        }

        // Electricity Screen (Triggers your ML Kit Camera View)
        composable(AppRoutes.ELECTRICITY_CAMERA) {
            MainScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Placeholder for Water Screen
        composable(AppRoutes.WATER_METER) {
            // TODO: Add your Water Meter logging screen here
        }

        // Placeholder for Gas Screen
        composable(AppRoutes.GAS_METER) {
            // TODO: Add your Gas Meter logging screen here
        }
        composable(AppRoutes.ENTRIES_HISTORY) {
            EntriesHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
