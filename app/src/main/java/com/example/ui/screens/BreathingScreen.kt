package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BreathPhase
import com.example.model.BreathingPattern
import com.example.ui.SereneViewModel
import com.example.ui.components.ZenBreathingSphere

@Composable
fun BreathingScreen(
    viewModel: SereneViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPattern = viewModel.selectedBreathingPattern.value
    val isActive = viewModel.isBreathingActive.value
    val currentPhase = viewModel.currentBreathPhase.value
    val phaseSecLeft = viewModel.phaseSecondsLeft.value
    val currentCycle = viewModel.currentBreathCycle.value
    val targetCycles = viewModel.targetBreathCycles.value
    val showCompleteDialog = viewModel.isBreathingCompleteDialog.value

    var completionMood by remember { mutableStateOf("Peaceful") }
    var completionNote by remember { mutableStateOf("") }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBreathingDialog() },
            title = {
                Text(
                    text = "Breathwork Complete",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Great job! You completed $targetCycles rounds of ${selectedPattern.title}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Feeling:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Calm", "Peaceful", "Clear", "Energized", "Relaxed").forEach { mood ->
                            FilterChip(
                                selected = completionMood == mood,
                                onClick = { completionMood = mood },
                                label = { Text(mood) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = completionNote,
                        onValueChange = { completionNote = it },
                        label = { Text("Reflection Note") },
                        placeholder = { Text("How do your lungs and mind feel?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logBreathingSession(completionMood, completionNote)
                    },
                    modifier = Modifier.testTag("log_breathwork_btn")
                ) {
                    Text("Save to Mindful Stats")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissBreathingDialog() }
                ) {
                    Text("Close")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("breathing_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mindful Breathing",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Pranayama rhythms to calm the nervous system",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Technique Selector Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BreathingPattern.values().forEach { pattern ->
                    FilterChip(
                        selected = selectedPattern == pattern,
                        onClick = { viewModel.selectBreathingPattern(pattern) },
                        label = { Text(pattern.title) },
                        enabled = !isActive,
                        modifier = Modifier.testTag("pattern_chip_${pattern.name}")
                    )
                }
            }
        }

        // Selected Pattern Explanation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = selectedPattern.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedPattern.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phase badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BreathTimingPill("Inhale", "${selectedPattern.inhaleSec}s", Modifier.weight(1f))
                        if (selectedPattern.holdInhaleSec > 0) {
                            BreathTimingPill("Hold", "${selectedPattern.holdInhaleSec}s", Modifier.weight(1f))
                        }
                        BreathTimingPill("Exhale", "${selectedPattern.exhaleSec}s", Modifier.weight(1f))
                        if (selectedPattern.holdExhaleSec > 0) {
                            BreathTimingPill("Hold", "${selectedPattern.holdExhaleSec}s", Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Target Rounds / Cycles setting
        if (!isActive) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Rounds: $targetCycles cycles",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 4, 6, 8).forEach { cycles ->
                            FilterChip(
                                selected = targetCycles == cycles,
                                onClick = { viewModel.setTargetCycles(cycles) },
                                label = { Text("$cycles") },
                                modifier = Modifier.testTag("cycles_$cycles")
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Round $currentCycle of $targetCycles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Animated Zen Sphere
        item {
            ZenBreathingSphere(
                isActive = isActive,
                currentPhase = currentPhase,
                secondsLeft = phaseSecLeft,
                pattern = selectedPattern,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Action Buttons
        item {
            if (!isActive) {
                Button(
                    onClick = { viewModel.startBreathing() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_breathing_btn"),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Begin Breathing Rhythm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.stopBreathing() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("stop_breathing_btn"),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pause / Conclude",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun BreathTimingPill(label: String, time: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
