@file:OptIn(ExperimentalPermissionsApi::class)

package de.yanneckreiss.mlkittutorial.ui

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel.EntryViewModel
import de.yanneckreiss.mlkittutorial.ui.camera.CameraScreen
import de.yanneckreiss.mlkittutorial.ui.no_permission.NoPermissionScreen


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainScreen(
    viewModel: EntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        CameraScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    } else {
        NoPermissionScreen(cameraPermissionState::launchPermissionRequest)
    }
}
