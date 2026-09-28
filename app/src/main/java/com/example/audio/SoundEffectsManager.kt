package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.data.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-performance sound effects engine for Battle Clash.
 * Directly renders synthesized 16-bit PCM audio into native AudioTracks without
 * relying on MediaCodec or external files, avoiding device codec errors.
 */
class SoundEffectsManager(private val context: Context) {

    private val sampleRate = 22050

    var isSoundEnabled: Boolean = true

    // Pre-computed PCM waveforms in memory
    private val buttonClickSamples by lazy { generateButtonClick() }
    private val rollTickSamples by lazy { generateRollTick() }
    private val turnP1Samples by lazy { generateTurnChangeP1() }
    private val turnP2Samples by lazy { generateTurnChangeP2() }
    private val scoreAddSamples by lazy { generateScoreAdd() }
    private val victorySamples by lazy { generateVictoryFanfare() }
    private val tieSamples by lazy { generateTieSound() }

    // Static AudioTrack instances for instant, zero-latency playback
    private var clickTrack: AudioTrack? = null
    private var rollTickTrack: AudioTrack? = null
    private var turnP1Track: AudioTrack? = null
    private var turnP2Track: AudioTrack? = null
    private var scoreAddTrack: AudioTrack? = null
    private var victoryTrack: AudioTrack? = null
    private var tieTrack: AudioTrack? = null

    private val audioScope = CoroutineScope(Dispatchers.Default)

    init {
        // Clean up any legacy cache files
        try {
            val legacyDir = File(context.cacheDir, "sounds")
            if (legacyDir.exists()) {
                legacyDir.deleteRecursively()
            }
        } catch (_: Throwable) {}

        audioScope.launch {
            try {
                clickTrack = createStaticTrack(buttonClickSamples)
                rollTickTrack = createStaticTrack(rollTickSamples)
                turnP1Track = createStaticTrack(turnP1Samples)
                turnP2Track = createStaticTrack(turnP2Samples)
                scoreAddTrack = createStaticTrack(scoreAddSamples)
                victoryTrack = createStaticTrack(victorySamples)
                tieTrack = createStaticTrack(tieSamples)
            } catch (e: Throwable) {
                Log.w("SoundEffectsManager", "AudioTrack init error: ${e.message}")
            }
        }
    }

    private fun createStaticTrack(samples: ShortArray): AudioTrack? {
        return try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.write(samples, 0, samples.size)
                track
            } else {
                track.release()
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun playTrack(track: AudioTrack?, volume: Float = 1.0f) {
        if (!isSoundEnabled || track == null) return
        try {
            synchronized(track) {
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.setVolume(volume)
                    track.stop()
                    track.reloadStaticData()
                    track.play()
                }
            }
        } catch (_: Throwable) {}
    }

    fun playButtonClick() {
        playTrack(clickTrack, 1.0f)
    }

    fun playRollTick() {
        playTrack(rollTickTrack, 0.7f)
    }

    fun playTurnChange(player: Player) {
        val track = if (player == Player.PLAYER_1) turnP1Track else turnP2Track
        playTrack(track, 0.95f)
    }

    fun playScoreAdd() {
        playTrack(scoreAddTrack, 0.9f)
    }

    fun playVictoryFanfare() {
        playTrack(victoryTrack, 1.0f)
    }

    fun playTieSound() {
        playTrack(tieTrack, 0.9f)
    }

    fun release() {
        listOf(
            clickTrack,
            rollTickTrack,
            turnP1Track,
            turnP2Track,
            scoreAddTrack,
            victoryTrack,
            tieTrack
        ).forEach { track ->
            try {
                track?.stop()
                track?.release()
            } catch (_: Throwable) {}
        }
        clickTrack = null
        rollTickTrack = null
        turnP1Track = null
        turnP2Track = null
        scoreAddTrack = null
        victoryTrack = null
        tieTrack = null
    }

    // --- Audio Waveform Synthesizers ---

    private fun generateButtonClick(): ShortArray {
        val durationMs = 45
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = 900.0 - (progress * 550.0)
            val envelope = exp(-progress * 9.0)
            val sampleVal = sin(2.0 * PI * freq * t) * envelope * 0.9
            samples[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun generateRollTick(): ShortArray {
        val durationMs = 28
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = 1400.0
            val envelope = (1.0 - progress) * (1.0 - progress)
            val sampleVal = (sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * freq * 2.0 * t)) * envelope * 0.75
            samples[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun generateTurnChangeP1(): ShortArray {
        val durationMs = 260
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = when {
                progress < 0.33 -> 523.25 // C5
                progress < 0.66 -> 659.25 // E5
                else -> 783.99           // G5
            }
            val noteProgress = (progress % 0.33) / 0.33
            val envelope = sin(PI * noteProgress) * exp(-progress * 1.5)
            val sampleVal = (sin(2.0 * PI * freq * t) + 0.25 * sin(2.0 * PI * freq * 2.0 * t)) * envelope * 0.85
            samples[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun generateTurnChangeP2(): ShortArray {
        val durationMs = 260
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = when {
                progress < 0.33 -> 587.33 // D5
                progress < 0.66 -> 739.99 // F#5
                else -> 880.00           // A5
            }
            val noteProgress = (progress % 0.33) / 0.33
            val envelope = sin(PI * noteProgress) * exp(-progress * 1.5)
            val sampleVal = (sin(2.0 * PI * freq * t) + 0.35 * sin(2.0 * PI * freq * 3.0 * t)) * envelope * 0.85
            samples[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun generateScoreAdd(): ShortArray {
        val durationMs = 250
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq1 = 987.77  // B5
            val freq2 = 1318.51 // E6
            val envelope = exp(-progress * 4.5)
            val sampleVal = (0.65 * sin(2.0 * PI * freq1 * t) + 0.45 * sin(2.0 * PI * freq2 * t)) * envelope * 0.85
            samples[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun generateVictoryFanfare(): ShortArray {
        val durationMs = 1300
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)
        val sampleRateD = sampleRate.toDouble()
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRateD
            val ms = (t * 1000.0).toInt()
            val sampleVal: Double = when {
                ms < 140 -> {
                    // G4
                    val env = minOf(1.0, (140 - ms) / 40.0)
                    sin(2.0 * PI * 392.00 * t) * 0.65 * env
                }
                ms < 280 -> {
                    // C5
                    val env = minOf(1.0, (280 - ms) / 40.0)
                    sin(2.0 * PI * 523.25 * t) * 0.70 * env
                }
                ms < 420 -> {
                    // E5
                    val env = minOf(1.0, (420 - ms) / 40.0)
                    sin(2.0 * PI * 659.25 * t) * 0.75 * env
                }
                ms < 620 -> {
                    // G5
                    val env = minOf(1.0, (620 - ms) / 50.0)
                    sin(2.0 * PI * 783.99 * t) * 0.80 * env
                }
                else -> {
                    // Grand Victory Chord: C5 + G5 + C6 + E6 held with brass vibrato & slow decay
                    val chordProgress = (ms - 620).toDouble() / 680.0
                    val chordEnv = exp(-chordProgress * 2.2) * (1.0 - chordProgress * 0.85).coerceAtLeast(0.0)
                    val vibrato = 1.0 + 0.005 * sin(2.0 * PI * 6.0 * t)
                    (
                        0.35 * sin(2.0 * PI * 523.25 * vibrato * t) +
                        0.30 * sin(2.0 * PI * 783.99 * vibrato * t) +
                        0.40 * sin(2.0 * PI * 1046.50 * vibrato * t) +
                        0.25 * sin(2.0 * PI * 1318.51 * vibrato * t)
                    ) * chordEnv * 0.90
                }
            }
            samples[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun generateTieSound(): ShortArray {
        val durationMs = 700
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val env = exp(-progress * 3.0)
            val sampleVal = (0.5 * sin(2.0 * PI * 440.0 * t) + 0.5 * sin(2.0 * PI * 554.37 * t)) * env * 0.75
            samples[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }
}
