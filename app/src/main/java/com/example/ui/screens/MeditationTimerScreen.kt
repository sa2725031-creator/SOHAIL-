package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.AmbientSound
import com.example.ui.SereneViewModel
import com.example.ui.components.MindfulProgressRing

@Composable
fun MeditationTimerScreen(
    viewModel: SereneViewModel,
    modifier: Modifier = Modifier
) {
    val isRunning = viewModel.isTimerRunning.value
    val isPaused = viewModel.isTimerPaused.value
    val isWarmup = viewModel.isWarmup.value
    val warmupSec = viewModel.warmupSeconds.value
    val secondsRemaining = viewModel.secondsRemaining.value
    val totalSeconds = viewModel.totalDurationSeconds.value
    val selectedMinutes = viewModel.selectedPresetMinutes.value
    val sessionType = viewModel.selectedSessionType.value
    val selectedAmbient = viewModel.selectedAmbient.value
    val ambientVolume = viewModel.ambientVolume.value
    val warmupSetting = viewModel.warmupSettingSec.value
    val playBell = viewModel.playStartEndChime.value

    val presets = listOf(1, 3, 5, 10, 15, 20, 30, 45, 60)
    val practiceStyles = listOf(
        "Daily Mindfulness",
        "Silent Zen",
        "Body Scan",
        "Loving Kindness",
        "Sleep Rest"
    )

    if (isRunning) {
        // Immersive Active Meditation View
        ActiveMeditationView(
            secondsRemaining = secondsRemaining,
            totalSeconds = totalSeconds,
            isWarmup = isWarmup,
            warmupSeconds = warmupSec,
            sessionType = sessionType,
            ambientSound = selectedAmbient,
            ambientVolume = ambientVolume,
            isPaused = isPaused,
            onPause = { viewModel.pauseTimer() },
            onResume = { viewModel.resumeTimer() },
            onEnd = { viewModel.cancelTimer() },
            onVolumeChange = { viewModel.setAmbientVolume(it) },
            modifier = modifier
        )
    } else {
        // Setup View
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("meditation_timer_screen"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "Meditation Timer",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Configure your stillness sanctuary",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Duration Presets Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Duration: $selectedMinutes Minutes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEach { mins ->
                                FilterChip(
                                    selected = selectedMinutes == mins,
                                    onClick = { viewModel.selectPresetMinutes(mins) },
                                    label = { Text("${mins}m") },
                                    modifier = Modifier.testTag("preset_${mins}m")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Fine Tune: ${selectedMinutes}m",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Slider(
                            value = selectedMinutes.toFloat(),
                            onValueChange = { viewModel.setCustomDurationMinutes(it.toInt()) },
                            valueRange = 1f..90f,
                            steps = 89,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("duration_slider")
                        )
                    }
                }
            }

            // Practice Style
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Intention & Focus",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            practiceStyles.forEach { style ->
                                FilterChip(
                                    selected = sessionType == style,
                                    onClick = { viewModel.setSessionType(style) },
                                    label = { Text(style) },
                                    modifier = Modifier.testTag("style_chip_$style")
                                )
                            }
                        }
                    }
                }
            }

            // Ambient Soundscapes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Ambient Soundscape",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AmbientSound.values().forEach { ambient ->
                                FilterChip(
                                    selected = selectedAmbient == ambient,
                                    onClick = { viewModel.setAmbientSound(ambient) },
                                    label = { Text(ambient.displayName) },
                                    modifier = Modifier.testTag("ambient_${ambient.name}")
                                )
                            }
                        }

                        if (selectedAmbient != AmbientSound.SILENT) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Sound volume",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Slider(
                                    value = ambientVolume,
                                    onValueChange = { viewModel.setAmbientVolume(it) },
                                    valueRange = 0f..1f,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("ambient_volume_slider")
                                )
                            }
                        }
                    }
                }
            }

            // Bell & Preparation Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Singing Bowl Bell",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Chimes at start & completion",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { viewModel.playSingingBowlSample() },
                                    modifier = Modifier.testTag("sample_chime_btn")
                                ) {
                                    Text("Preview")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = playBell,
                                    onCheckedChange = { viewModel.toggleStartEndChime(it) },
                                    modifier = Modifier.testTag("chime_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Warm-up Delay: ${warmupSetting}s",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 5, 10, 15).forEach { secs ->
                                FilterChip(
                                    selected = warmupSetting == secs,
                                    onClick = { viewModel.setWarmupSetting(secs) },
                                    label = { Text(if (secs == 0) "None" else "${secs}s") },
                                    modifier = Modifier.testTag("warmup_${secs}s")
                                )
                            }
                        }
                    }
                }
            }

            // Big Start Button
            item {
                Button(
                    onClick = { viewModel.startTimer() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_meditation_button"),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Begin Meditation (${selectedMinutes} min)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveMeditationView(
    secondsRemaining: Int,
    totalSeconds: Int,
    isWarmup: Boolean,
    warmupSeconds: Int,
    sessionType: String,
    ambientSound: AmbientSound,
    ambientVolume: Float,
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("active_meditation_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = if (isWarmup) "Gentle Preparation" else "Rest in Awareness",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Observe sensations without judgment",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Center Progress Ring
        MindfulProgressRing(
            secondsRemaining = secondsRemaining,
            totalSeconds = totalSeconds,
            isWarmup = isWarmup,
            warmupSeconds = warmupSeconds,
            sessionType = sessionType,
            ambientSound = ambientSound
        )

        // Bottom Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp)
        ) {
            if (ambientSound != AmbientSound.SILENT) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = ambientVolume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("active_ambient_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEnd,
                    modifier = Modifier.testTag("end_session_button"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "End Session",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("End Session")
                }

                Button(
                    onClick = { if (isPaused) onResume() else onPause() },
                    modifier = Modifier.testTag("pause_resume_button"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPaused) "Resume" else "Pause")
                }
            }
        }
    }
}
