package de.yanneckreiss.mlkittutorial.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.yanneckreiss.mlkittutorial.entry.data.local.UtilityType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUtilityReadingDialog(
    initialUtilityType: UtilityType = UtilityType.ELECTRICITY,
    onDismiss: () -> Unit,
    onConfirm: (utilityType: UtilityType, reading: Double, description: String, temp: Double, score: Double) -> Unit
) {
    var selectedType by rememberSaveable { mutableStateOf(initialUtilityType) }
    var readingText by rememberSaveable { mutableStateOf("") }
    var descriptionText by rememberSaveable { mutableStateOf("") }
    var ambientTempText by rememberSaveable { mutableStateOf("22.5") }
    var weatherScoreText by rememberSaveable { mutableStateOf("4.2") }

    val isValid = readingText.toDoubleOrNull() != null && (readingText.toDoubleOrNull() ?: 0.0) >= 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Utility Reading",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Utility Type:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Utility Type Selection Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UtilityType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text("${type.emoji} ${type.displayName}") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Meter Reading Input
                OutlinedTextField(
                    value = readingText,
                    onValueChange = { readingText = it },
                    label = { Text("Consumption (${selectedType.defaultUnit})") },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                UtilityType.ELECTRICITY -> "e.g. 15.4 kWh"
                                UtilityType.WATER -> "e.g. 310.0 L"
                                UtilityType.GAS -> "e.g. 1.2 kg"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Weather and Temp Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ambientTempText,
                        onValueChange = { ambientTempText = it },
                        label = { Text("Ambient °C") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = weatherScoreText,
                        onValueChange = { weatherScoreText = it },
                        label = { Text("Weather Score") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Description Input
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Note / Description (Optional)") },
                    placeholder = { Text("e.g. Morning routine, heating geyser") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val reading = readingText.toDoubleOrNull() ?: 0.0
                    val temp = ambientTempText.toDoubleOrNull() ?: 22.0
                    val score = weatherScoreText.toDoubleOrNull() ?: 4.0
                    onConfirm(selectedType, reading, descriptionText, temp, score)
                },
                enabled = isValid
            ) {
                Text("Record Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
