package de.yanneckreiss.mlkittutorial.ui

import android.annotation.SuppressLint
import android.os.Build.VERSION_CODES.Q
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@SuppressLint("ComposeModifierMissing")
@RequiresApi(Q)
@Composable
fun CustomTopAppBar(
    onBackClick: () -> Unit,
    title: String,
    content: () -> Unit = {}
) {
    // If you want to show a top bar with a back button before/around the Camera screen:
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Electricity Meter") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content
        }
    }
}