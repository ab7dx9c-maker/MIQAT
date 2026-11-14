package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.data.model.AzanPreset
import com.example.util.NetworkStatusHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

private const val TAG = "AzanAudioManager"

data class AzanPlaybackState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val activeTitle: String = "",
    val isSynthesizerActive: Boolean = false,
    val usingCustomAudio: Boolean = false
)

class AzanAudioManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _playbackState = MutableStateFlow(AzanPlaybackState())
    val playbackState: StateFlow<AzanPlaybackState> = _playbackState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var synthJob: Job? = null
    private var synthTrack: AudioTrack? = null
    @Volatile
    private var synthPaused = false

    fun playTasbihClick(isGoalReached: Boolean = false) {
        scope.launch {
            try {
                val sampleRate = 22050
                val durationSec = if (isGoalReached) 0.28 else 0.045
                val numSamples = (sampleRate * durationSec).toInt()
                val buffer = ShortArray(numSamples)
                val baseFreq = if (isGoalReached) 880.0 else 520.0
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = exp(-t * (if (isGoalReached) 12.0 else 95.0))
                    val wave = if (isGoalReached) {
                        0.65 * sin(2 * PI * baseFreq * t) + 0.35 * sin(2 * PI * 1318.5 * t)
                    } else {
                        0.7 * sin(2 * PI * baseFreq * t) + 0.3 * sin(2 * PI * 1040.0 * t)
                    }
                    buffer[i] = (wave * env * Short.MAX_VALUE * 0.45).toInt().coerceIn(
                        Short.MIN_VALUE.toInt(),
                        Short.MAX_VALUE.toInt()
                    ).toShort()
                }
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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
                track.write(buffer, 0, buffer.size)
                track.play()
                delay((durationSec * 1000).toLong() + 60L)
                track.release()
            } catch (e: Exception) {
                Log.w(TAG, "Tasbih audio click skipped: ${e.message}")
            }
        }
    }

    fun playAzan(
        preset: AzanPreset,
        customUriString: String?,
        customName: String?,
        preferCustom: Boolean
    ) {
        stop()

        if (preferCustom && !customUriString.isNullOrBlank()) {
            playCustomUri(customUriString, customName ?: "أذان مخصص")
            return
        }

        if (preset.isSynthesizer || preset.streamUrl.isNullOrBlank()) {
            startHijazSynthesizer(preset.arabicTitle)
            return
        }

        if (!NetworkStatusHelper.isOnline(context)) {
            startHijazSynthesizer("${preset.arabicTitle} (بدون إنترنت)")
            return
        }

        playStreamWithFallback(preset.streamUrl, preset.arabicTitle)
    }

    private fun playCustomUri(uriString: String, title: String) {
        _playbackState.value = AzanPlaybackState(
            isPlaying = false,
            isBuffering = true,
            activeTitle = title,
            usingCustomAudio = true
        )
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, Uri.parse(uriString))
                setOnPreparedListener { player ->
                    player.start()
                    _playbackState.value = AzanPlaybackState(
                        isPlaying = true,
                        isPaused = false,
                        isBuffering = false,
                        positionMs = 0L,
                        durationMs = player.duration.toLong().coerceAtLeast(1L),
                        activeTitle = title,
                        usingCustomAudio = true
                    )
                    startProgressPolling()
                }
                setOnCompletionListener { stop() }
                setOnErrorListener { _, _, _ ->
                    Log.w(TAG, "Custom audio failed, falling back to Hijaz synth")
                    startHijazSynthesizer("توليف حجازي (احتياطي)")
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.w(TAG, "Failed opening custom URI, falling back to Hijaz synth: ${e.message}")
            startHijazSynthesizer("توليف حجازي (احتياطي)")
        }
    }

    private fun playStreamWithFallback(url: String, title: String) {
        _playbackState.value = AzanPlaybackState(
            isPlaying = false,
            isBuffering = true,
            activeTitle = title
        )
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { player ->
                    player.start()
                    _playbackState.value = AzanPlaybackState(
                        isPlaying = true,
                        isPaused = false,
                        isBuffering = false,
                        positionMs = 0L,
                        durationMs = player.duration.toLong().coerceAtLeast(1L),
                        activeTitle = title
                    )
                    startProgressPolling()
                }
                setOnCompletionListener { stop() }
                setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "Stream error ($what/$extra), falling back to Hijaz synth")
                    startHijazSynthesizer("$title (توليف حجازي)")
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.w(TAG, "Stream failed, falling back to Hijaz synth: ${e.message}")
            startHijazSynthesizer("$title (توليف حجازي)")
        }
    }

    private fun startHijazSynthesizer(title: String) {
        stopMediaOnly()
        synthPaused = false

        // Authentic Hijaz Maqam melodic sequence (D4, Eb4, F#4, G4, A4, Bb4, C#5, D5)
        val notesHz = listOf(
            293.66 to 950L,  // D4 - الله
            311.13 to 700L,  // Eb4
            369.99 to 1350L, // F#4 - أكبر
            392.00 to 900L,  // G4
            440.00 to 1500L, // A4 - الله أكبر
            392.00 to 800L,  // G4
            369.99 to 1200L, // F#4
            311.13 to 750L,  // Eb4
            293.66 to 1600L, // D4 - أشهد أن لا إله إلا الله
            369.99 to 900L,  // F#4
            440.00 to 1100L, // A4
            466.16 to 950L,  // Bb4
            554.37 to 1200L, // C#5
            587.33 to 1500L, // D5 - حي على الصلاة
            440.00 to 1000L, // A4
            369.99 to 1100L, // F#4
            311.13 to 850L,  // Eb4
            293.66 to 1950L  // D4 - لا إله إلا الله
        )
        val totalDurationMs = notesHz.sumOf { it.second }

        _playbackState.value = AzanPlaybackState(
            isPlaying = true,
            isPaused = false,
            isBuffering = false,
            positionMs = 0L,
            durationMs = totalDurationMs,
            activeTitle = title,
            isSynthesizerActive = true
        )

        synthJob = scope.launch {
            val sampleRate = 22050
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack.Builder()
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
                .setBufferSizeInBytes(minBuf.coerceAtLeast(4096))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            synthTrack = track
            track.play()

            var elapsedMs = 0L
            for ((freq, durMs) in notesHz) {
                if (!isActive) break
                val totalSamples = ((sampleRate * durMs) / 1000L).toInt()
                val chunkSize = 1024
                var written = 0
                while (written < totalSamples && isActive) {
                    while (synthPaused && isActive) {
                        delay(80L)
                    }
                    if (!isActive) break
                    val count = minOf(chunkSize, totalSamples - written)
                    val chunk = ShortArray(count)
                    for (i in 0 until count) {
                        val idx = written + i
                        val t = idx.toDouble() / sampleRate
                        val progress = idx.toDouble() / totalSamples
                        val attack = (progress / 0.12).coerceIn(0.0, 1.0)
                        val release = ((1.0 - progress) / 0.25).coerceIn(0.0, 1.0)
                        val vibrato = 1.0 + 0.004 * sin(2 * PI * 4.5 * t)
                        val f = freq * vibrato
                        val harmonic = 0.65 * sin(2 * PI * f * t) +
                            0.25 * sin(2 * PI * f * 2.0 * t) +
                            0.10 * sin(2 * PI * f * 3.0 * t)
                        val sample = harmonic * attack * release * 0.42
                        chunk[i] = (sample * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                    }
                    track.write(chunk, 0, count)
                    written += count
                    val noteProgressMs = (written.toLong() * durMs) / totalSamples
                    _playbackState.value = _playbackState.value.copy(
                        positionMs = (elapsedMs + noteProgressMs).coerceAtMost(totalDurationMs)
                    )
                }
                elapsedMs += durMs
            }

            try {
                track.stop()
                track.release()
            } catch (_: Exception) {
            }
            if (isActive) {
                _playbackState.value = AzanPlaybackState()
            }
        }
    }

    fun pause() {
        val state = _playbackState.value
        if (!state.isPlaying || state.isPaused) return
        if (state.isSynthesizerActive) {
            synthPaused = true
            try {
                synthTrack?.pause()
            } catch (_: Exception) {
            }
            _playbackState.value = state.copy(isPaused = true, isPlaying = false)
        } else {
            try {
                mediaPlayer?.pause()
                _playbackState.value = state.copy(isPaused = true, isPlaying = false)
            } catch (_: Exception) {
            }
        }
    }

    fun resume() {
        val state = _playbackState.value
        if (!state.isPaused) return
        if (state.isSynthesizerActive) {
            synthPaused = false
            try {
                synthTrack?.play()
            } catch (_: Exception) {
            }
            _playbackState.value = state.copy(isPaused = false, isPlaying = true)
        } else {
            try {
                mediaPlayer?.start()
                _playbackState.value = state.copy(isPaused = false, isPlaying = true)
            } catch (_: Exception) {
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val state = _playbackState.value
        if (!state.isSynthesizerActive) {
            try {
                mediaPlayer?.seekTo(positionMs.toInt())
                _playbackState.value = state.copy(positionMs = positionMs)
            } catch (_: Exception) {
            }
        }
    }

    fun stop() {
        stopMediaOnly()
        stopSynthOnly()
        _playbackState.value = AzanPlaybackState()
    }

    private fun stopMediaOnly() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }

    private fun stopSynthOnly() {
        synthPaused = false
        synthJob?.cancel()
        synthJob = null
        try {
            synthTrack?.stop()
        } catch (_: Exception) {
        }
        try {
            synthTrack?.release()
        } catch (_: Exception) {
        }
        synthTrack = null
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val mp = mediaPlayer ?: break
                try {
                    if (mp.isPlaying) {
                        _playbackState.value = _playbackState.value.copy(
                            positionMs = mp.currentPosition.toLong(),
                            durationMs = mp.duration.toLong().coerceAtLeast(1L)
                        )
                    }
                } catch (_: Exception) {
                    break
                }
                delay(350L)
            }
        }
    }
}
