package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class AmbientSound(val displayName: String, val description: String) {
    SILENT("Silent", "Pure quiet stillness"),
    SINGING_BOWL("Tibetan Bowl", "Warm harmonic resonance & drone"),
    OCEAN_WAVES("Ocean Waves", "Gentle rhythmic surf swelling & receding"),
    RAIN("Gentle Rain", "Soothing soft rainfall"),
    STREAM("Forest Brook", "Crisp bubbling natural mountain stream")
}

class SoundEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var ambientJob: Job? = null
    private var currentAmbient = AmbientSound.SILENT
    private var volume = 0.7f

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
    }

    fun playBellChime() {
        scope.launch {
            synthesizeSingingBowlBell()
        }
    }

    fun setAmbient(ambient: AmbientSound) {
        if (currentAmbient == ambient && ambientJob?.isActive == true) return
        stopAmbient()
        currentAmbient = ambient
        if (ambient == AmbientSound.SILENT) return

        ambientJob = scope.launch {
            runAmbientGenerator(ambient)
        }
    }

    fun stopAmbient() {
        ambientJob?.cancel()
        ambientJob = null
    }

    private fun synthesizeSingingBowlBell() {
        val sampleRate = 44100
        val durationSec = 4.5f
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        val fundamentalFreq = 432.0 // Solfeggio / peaceful frequency
        val harmonic1Freq = fundamentalFreq * 2.76
        val harmonic2Freq = fundamentalFreq * 4.95

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 1.1)

            val wave = (0.6 * sin(2.0 * PI * fundamentalFreq * t) +
                    0.25 * sin(2.0 * PI * harmonic1Freq * t) +
                    0.15 * sin(2.0 * PI * harmonic2Freq * t)) * decay

            val sample = (wave * Short.MAX_VALUE * 0.85f).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.setVolume(volume)
            audioTrack.play()
            Thread.sleep((durationSec * 1000).toLong())
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // AudioTrack fallback
        }
    }

    private fun runAmbientGenerator(type: AmbientSound) {
        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val audioTrack = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build().also {
                    it.setVolume(volume)
                    it.play()
                }
        } catch (_: Exception) {
            return
        }

        val buffer = ShortArray(bufferSize)
        val random = Random()
        var phase = 0.0
        var phaseLfo = 0.0
        var pinkB0 = 0.0
        var pinkB1 = 0.0
        var pinkB2 = 0.0

        try {
            while (ambientJob?.isActive == true) {
                audioTrack.setVolume(volume)
                for (i in buffer.indices) {
                    val white = random.nextDouble() * 2.0 - 1.0

                    // Pink noise filter approximation (Paul Kellet's algorithm)
                    pinkB0 = 0.99886 * pinkB0 + white * 0.0555179
                    pinkB1 = 0.99332 * pinkB1 + white * 0.0750759
                    pinkB2 = 0.96900 * pinkB2 + white * 0.1538520
                    val pink = (pinkB0 + pinkB1 + pinkB2 + white * 0.05) * 0.25

                    val sampleVal: Double = when (type) {
                        AmbientSound.OCEAN_WAVES -> {
                            // Swelling ocean waves with 10s period
                            phaseLfo += (2.0 * PI * 0.1) / sampleRate
                            val waveMod = (sin(phaseLfo) * 0.5 + 0.5) * 0.8 + 0.2
                            pink * waveMod * 0.8
                        }
                        AmbientSound.RAIN -> {
                            // Soft rain: pink noise plus randomized droplet spikes
                            val drop = if (random.nextDouble() < 0.008) (random.nextDouble() * 0.6) else 0.0
                            (pink * 0.45 + drop) * 0.7
                        }
                        AmbientSound.STREAM -> {
                            // Gentle stream with undulating subtle ripples
                            phaseLfo += (2.0 * PI * 0.7) / sampleRate
                            val ripple = sin(phaseLfo) * 0.2 + 0.8
                            pink * ripple * 0.5
                        }
                        AmbientSound.SINGING_BOWL -> {
                            // Continuous warm drone chord (108Hz + 216Hz + 324Hz subtle)
                            phase += (2.0 * PI * 108.0) / sampleRate
                            val tone1 = sin(phase)
                            val tone2 = sin(phase * 2.0) * 0.4
                            val tone3 = sin(phase * 3.0) * 0.15
                            (tone1 + tone2 + tone3) * 0.35 + pink * 0.05
                        }
                        AmbientSound.SILENT -> 0.0
                    }

                    buffer[i] = (sampleVal * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                }
                audioTrack.write(buffer, 0, buffer.size)
            }
        } catch (_: Exception) {
        } finally {
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    fun release() {
        stopAmbient()
    }
}
