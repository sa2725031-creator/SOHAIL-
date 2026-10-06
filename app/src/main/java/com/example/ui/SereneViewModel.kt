package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientSound
import com.example.audio.HapticHelper
import com.example.audio.SoundEngine
import com.example.data.MeditationSession
import com.example.data.MindfulReflection
import com.example.data.SereneDatabase
import com.example.data.SereneRepository
import com.example.model.BreathPhase
import com.example.model.BreathingPattern
import com.example.model.DailyMindfulDayStat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SereneViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SereneRepository
    val soundEngine = SoundEngine()
    private val haptics = HapticHelper(application)

    init {
        val db = SereneDatabase.getDatabase(application)
        repository = SereneRepository(db.sereneDao())
    }

    // Sessions and Reflections flows
    val allSessions: StateFlow<List<MeditationSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReflections: StateFlow<List<MindfulReflection>> = repository.allReflections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Timer state
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused = _isTimerPaused.asStateFlow()

    private val _isWarmup = MutableStateFlow(false)
    val isWarmup = _isWarmup.asStateFlow()

    private val _warmupSeconds = MutableStateFlow(0)
    val warmupSeconds = _warmupSeconds.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(600) // Default 10 min
    val totalDurationSeconds = _totalDurationSeconds.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(600)
    val secondsRemaining = _secondsRemaining.asStateFlow()

    private val _selectedPresetMinutes = MutableStateFlow(10)
    val selectedPresetMinutes = _selectedPresetMinutes.asStateFlow()

    private val _selectedSessionType = MutableStateFlow("Daily Mindfulness")
    val selectedSessionType = _selectedSessionType.asStateFlow()

    private val _selectedAmbient = MutableStateFlow(AmbientSound.SILENT)
    val selectedAmbient = _selectedAmbient.asStateFlow()

    private val _ambientVolume = MutableStateFlow(0.7f)
    val ambientVolume = _ambientVolume.asStateFlow()

    private val _warmupSettingSec = MutableStateFlow(5)
    val warmupSettingSec = _warmupSettingSec.asStateFlow()

    private val _playStartEndChime = MutableStateFlow(true)
    val playStartEndChime = _playStartEndChime.asStateFlow()

    private val _showPostSessionDialog = MutableStateFlow(false)
    val showPostSessionDialog = _showPostSessionDialog.asStateFlow()

    private val _completedDurationSec = MutableStateFlow(0)
    val completedDurationSec = _completedDurationSec.asStateFlow()

    // Breathing Studio state
    private val _selectedBreathingPattern = MutableStateFlow(BreathingPattern.BOX)
    val selectedBreathingPattern = _selectedBreathingPattern.asStateFlow()

    private val _isBreathingActive = MutableStateFlow(false)
    val isBreathingActive = _isBreathingActive.asStateFlow()

    private val _currentBreathPhase = MutableStateFlow(BreathPhase.PREPARE)
    val currentBreathPhase = _currentBreathPhase.asStateFlow()

    private val _phaseSecondsLeft = MutableStateFlow(0)
    val phaseSecondsLeft = _phaseSecondsLeft.asStateFlow()

    private val _currentBreathCycle = MutableStateFlow(1)
    val currentBreathCycle = _currentBreathCycle.asStateFlow()

    private val _targetBreathCycles = MutableStateFlow(4)
    val targetBreathCycles = _targetBreathCycles.asStateFlow()

    private val _totalBreathingElapsedSec = MutableStateFlow(0)
    val totalBreathingElapsedSec = _totalBreathingElapsedSec.asStateFlow()

    private val _isBreathingCompleteDialog = MutableStateFlow(false)
    val isBreathingCompleteDialog = _isBreathingCompleteDialog.asStateFlow()

    // Daily Goals
    private val _dailyGoalMinutes = MutableStateFlow(15)
    val dailyGoalMinutes = _dailyGoalMinutes.asStateFlow()

    private var timerJob: Job? = null
    private var breathingJob: Job? = null

    fun selectPresetMinutes(minutes: Int) {
        if (_isTimerRunning.value) return
        _selectedPresetMinutes.value = minutes
        val totalSec = minutes * 60
        _totalDurationSeconds.value = totalSec
        _secondsRemaining.value = totalSec
    }

    fun setCustomDurationMinutes(minutes: Int) {
        if (_isTimerRunning.value) return
        val clamped = minutes.coerceIn(1, 120)
        _selectedPresetMinutes.value = clamped
        val totalSec = clamped * 60
        _totalDurationSeconds.value = totalSec
        _secondsRemaining.value = totalSec
    }

    fun setSessionType(type: String) {
        _selectedSessionType.value = type
    }

    fun setWarmupSetting(seconds: Int) {
        _warmupSettingSec.value = seconds
    }

    fun toggleStartEndChime(enable: Boolean) {
        _playStartEndChime.value = enable
    }

    fun setAmbientSound(ambient: AmbientSound) {
        _selectedAmbient.value = ambient
        if (_isTimerRunning.value && !_isTimerPaused.value && !_isWarmup.value) {
            soundEngine.setAmbient(ambient)
        }
    }

    fun setAmbientVolume(vol: Float) {
        _ambientVolume.value = vol
        soundEngine.setVolume(vol)
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        _isTimerPaused.value = false
        _secondsRemaining.value = _totalDurationSeconds.value

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            if (_warmupSettingSec.value > 0) {
                _isWarmup.value = true
                _warmupSeconds.value = _warmupSettingSec.value
                while (_warmupSeconds.value > 0) {
                    haptics.pulseLight()
                    delay(1000)
                    _warmupSeconds.value -= 1
                }
                _isWarmup.value = false
            }

            // Begin meditation
            if (_playStartEndChime.value) {
                soundEngine.playBellChime()
                haptics.pulseCompletion()
            }
            if (_selectedAmbient.value != AmbientSound.SILENT) {
                soundEngine.setAmbient(_selectedAmbient.value)
            }

            while (_secondsRemaining.value > 0) {
                delay(1000)
                if (!_isTimerPaused.value) {
                    _secondsRemaining.value -= 1
                }
            }

            // Session completed
            soundEngine.stopAmbient()
            if (_playStartEndChime.value) {
                soundEngine.playBellChime()
            }
            haptics.pulseCompletion()

            _completedDurationSec.value = _totalDurationSeconds.value
            _isTimerRunning.value = false
            _isTimerPaused.value = false
            _showPostSessionDialog.value = true
        }
    }

    fun pauseTimer() {
        if (!_isTimerRunning.value || _isTimerPaused.value) return
        _isTimerPaused.value = true
        soundEngine.stopAmbient()
        haptics.pulseLight()
    }

    fun resumeTimer() {
        if (!_isTimerRunning.value || !_isTimerPaused.value) return
        _isTimerPaused.value = false
        if (_selectedAmbient.value != AmbientSound.SILENT) {
            soundEngine.setAmbient(_selectedAmbient.value)
        }
        haptics.pulseLight()
    }

    fun cancelTimer() {
        timerJob?.cancel()
        soundEngine.stopAmbient()
        val elapsed = _totalDurationSeconds.value - _secondsRemaining.value
        _isTimerRunning.value = false
        _isTimerPaused.value = false
        _isWarmup.value = false
        _secondsRemaining.value = _totalDurationSeconds.value

        // If user meditated for at least 60 seconds before ending, allow them to log it
        if (elapsed >= 60) {
            _completedDurationSec.value = elapsed
            _showPostSessionDialog.value = true
        }
    }

    fun saveCompletedSession(mood: String, note: String) {
        val duration = _completedDurationSec.value
        if (duration <= 0) {
            _showPostSessionDialog.value = false
            return
        }

        viewModelScope.launch {
            val session = MeditationSession(
                sessionTitle = _selectedSessionType.value,
                sessionType = "Meditation",
                durationSeconds = duration,
                timestamp = System.currentTimeMillis(),
                moodAfter = mood,
                notes = note,
                ambientSound = _selectedAmbient.value.displayName
            )
            repository.insertSession(session)
            _showPostSessionDialog.value = false
            _completedDurationSec.value = 0
            _secondsRemaining.value = _totalDurationSeconds.value
        }
    }

    fun dismissPostSessionDialog() {
        _showPostSessionDialog.value = false
        _completedDurationSec.value = 0
        _secondsRemaining.value = _totalDurationSeconds.value
    }

    // Breathing Logic
    fun selectBreathingPattern(pattern: BreathingPattern) {
        if (_isBreathingActive.value) return
        _selectedBreathingPattern.value = pattern
        _targetBreathCycles.value = pattern.recommendedRounds
    }

    fun setTargetCycles(cycles: Int) {
        if (_isBreathingActive.value) return
        _targetBreathCycles.value = cycles.coerceIn(1, 20)
    }

    fun startBreathing() {
        if (_isBreathingActive.value) return
        _isBreathingActive.value = true
        _currentBreathCycle.value = 1
        _totalBreathingElapsedSec.value = 0
        val pattern = _selectedBreathingPattern.value

        breathingJob?.cancel()
        breathingJob = viewModelScope.launch {
            // Preparation phase (3 seconds)
            _currentBreathPhase.value = BreathPhase.PREPARE
            _phaseSecondsLeft.value = 3
            haptics.pulsePhase()
            while (_phaseSecondsLeft.value > 0) {
                delay(1000)
                _phaseSecondsLeft.value -= 1
                _totalBreathingElapsedSec.value += 1
            }

            for (cycle in 1.._targetBreathCycles.value) {
                _currentBreathCycle.value = cycle

                // Inhale
                _currentBreathPhase.value = BreathPhase.INHALE
                _phaseSecondsLeft.value = pattern.inhaleSec
                haptics.pulsePhase()
                while (_phaseSecondsLeft.value > 0) {
                    delay(1000)
                    _phaseSecondsLeft.value -= 1
                    _totalBreathingElapsedSec.value += 1
                }

                // Hold In
                if (pattern.holdInhaleSec > 0) {
                    _currentBreathPhase.value = BreathPhase.HOLD_IN
                    _phaseSecondsLeft.value = pattern.holdInhaleSec
                    haptics.pulseLight()
                    while (_phaseSecondsLeft.value > 0) {
                        delay(1000)
                        _phaseSecondsLeft.value -= 1
                        _totalBreathingElapsedSec.value += 1
                    }
                }

                // Exhale
                _currentBreathPhase.value = BreathPhase.EXHALE
                _phaseSecondsLeft.value = pattern.exhaleSec
                haptics.pulsePhase()
                while (_phaseSecondsLeft.value > 0) {
                    delay(1000)
                    _phaseSecondsLeft.value -= 1
                    _totalBreathingElapsedSec.value += 1
                }

                // Hold Out
                if (pattern.holdExhaleSec > 0) {
                    _currentBreathPhase.value = BreathPhase.HOLD_OUT
                    _phaseSecondsLeft.value = pattern.holdExhaleSec
                    haptics.pulseLight()
                    while (_phaseSecondsLeft.value > 0) {
                        delay(1000)
                        _phaseSecondsLeft.value -= 1
                        _totalBreathingElapsedSec.value += 1
                    }
                }
            }

            _currentBreathPhase.value = BreathPhase.COMPLETE
            haptics.pulseCompletion()
            soundEngine.playBellChime()
            _isBreathingActive.value = false
            _isBreathingCompleteDialog.value = true
        }
    }

    fun stopBreathing() {
        breathingJob?.cancel()
        _isBreathingActive.value = false
        _currentBreathPhase.value = BreathPhase.PREPARE
    }

    fun logBreathingSession(mood: String, note: String) {
        val totalSec = _totalBreathingElapsedSec.value
        viewModelScope.launch {
            if (totalSec >= 30) {
                val session = MeditationSession(
                    sessionTitle = "${_selectedBreathingPattern.value.title} Breathwork",
                    sessionType = "Breathing",
                    durationSeconds = totalSec,
                    timestamp = System.currentTimeMillis(),
                    moodAfter = mood,
                    notes = note.ifEmpty { "${_targetBreathCycles.value} cycles completed." },
                    ambientSound = "Silent"
                )
                repository.insertSession(session)
            }
            _isBreathingCompleteDialog.value = false
            _totalBreathingElapsedSec.value = 0
        }
    }

    fun dismissBreathingDialog() {
        _isBreathingCompleteDialog.value = false
        _totalBreathingElapsedSec.value = 0
    }

    // Mindful Reflection / Daily Journal
    fun saveDailyReflection(mood: String, gratitude: String, intention: String) {
        viewModelScope.launch {
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val reflection = MindfulReflection(
                dateKey = dateKey,
                timestamp = System.currentTimeMillis(),
                mood = mood,
                gratitudeNote = gratitude,
                intentionText = intention
            )
            repository.insertReflection(reflection)
            haptics.pulseCompletion()
        }
    }

    fun deleteSession(session: MeditationSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun deleteReflection(reflection: MindfulReflection) {
        viewModelScope.launch {
            repository.deleteReflection(reflection)
        }
    }

    fun setDailyGoal(minutes: Int) {
        _dailyGoalMinutes.value = minutes.coerceIn(1, 120)
    }

    fun playSingingBowlSample() {
        soundEngine.playBellChime()
        haptics.pulseLight()
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}
