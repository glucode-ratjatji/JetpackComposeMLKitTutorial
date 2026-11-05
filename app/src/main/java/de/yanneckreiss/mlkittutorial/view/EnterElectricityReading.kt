package de.yanneckreiss.mlkittutorial.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel.EntryViewModel
import de.yanneckreiss.mlkittutorial.entry.data.local.Entry

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ComposeModifierMissing")
@Composable
fun EnterToDoDetails(
    viewModel: EntryViewModel,
    scannedKwhValue: String = "",
    entryToEdit: Entry? = null,
    dismissBottomSheet: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // State for user confirmation / editing of the scanned kWh value
    var extractedKwhValue by rememberSaveable(scannedKwhValue) {
        mutableStateOf(entryToEdit?.meterReading?.toString() ?: scannedKwhValue)
    }

    var description by rememberSaveable(entryToEdit) {
        mutableStateOf(entryToEdit?.description ?: "")
    }

    val isButtonEnabled = extractedKwhValue.isNotBlank() && extractedKwhValue.toDoubleOrNull() != null
    val isTaskBeingEdited = entryToEdit != null
    val headerText = if (isTaskBeingEdited) "Edit Entry" else "Confirm Scanned Reading"
    val buttonText = if (isTaskBeingEdited) "Save changes" else "Confirm & Create Entry"

    Surface(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .clickable(onClick = dismissBottomSheet)
                        .align(alignment = Alignment.CenterStart),
                    color = Color.Blue
                )

                Text(
                    headerText,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
            }

            // Display Current Date & Extracted Metadata badges
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "📅 Current Date: ${viewModel.currentDate}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "📊 Week Starting: ${viewModel.currentWeekStart}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "🗓️ Month Number: ${viewModel.currentMonthNumber}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Confirmed Scanned kWh TextField
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = extractedKwhValue,
                onValueChange = { extractedKwhValue = it },
                label = { Text("Confirmed kWh Value") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                )
            )

            // Optional Description TextField
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // Action Button
            Button(
                onClick = {
                    if (isTaskBeingEdited && entryToEdit != null) {
                        val updatedEntry = entryToEdit.copy(
                            meterReading = extractedKwhValue.toDoubleOrNull() ?: entryToEdit.meterReading,
                            description = description
                        )
                        //viewModel.(updatedEntry)
                    } else {
                        viewModel.addScannedReading(extractedKwhValue, description)
                    }
                    dismissBottomSheet()
                },
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    disabledContainerColor = Color.LightGray
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}