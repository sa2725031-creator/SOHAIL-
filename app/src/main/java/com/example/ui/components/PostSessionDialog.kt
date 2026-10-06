package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PostSessionDialog(
    durationSeconds: Int,
    sessionTitle: String,
    onSave: (mood: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val moods = listOf("Peaceful", "Grounded", "Calm", "Grateful", "Focused", "Rested")
    var selectedMood by remember { mutableStateOf("Peaceful") }
    var notes by remember { mutableStateOf("") }

    val minutes = durationSeconds / 60
    val secs = durationSeconds % 60
    val timeLabel = if (minutes > 0) "${minutes}m ${secs}s" else "${secs}s"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Session Completed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$sessionTitle • $timeLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "How are you feeling right now?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood },
                            label = { Text(mood) },
                            modifier = Modifier.testTag("mood_chip_$mood")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Mindful Reflection (Optional)") },
                    placeholder = { Text("What did you notice during this practice?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_reflection_input"),
                    minLines = 3,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedMood, notes) },
                modifier = Modifier.testTag("save_session_button")
            ) {
                Text("Log to Mindful Minutes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("discard_session_button")
            ) {
                Text("Discard")
            }
        }
    )
}
