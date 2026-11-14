package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AdhkarItem
import com.example.data.model.CustomAdhkarAudio
import com.example.data.model.ReciterVoiceProfile
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
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

private const val TAG = "AdhkarAudioManager"

data class AdhkarPlaybackState(
    val activeItemId: String? = null,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isUsingCustomAudio: Boolean = false,
    val isPlayingPlaylist: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val ttsArabicReady: Boolean = false,
    val availableArabicVoiceNames: List<String> = emptyList(),
    val statusNote: String? = null
)

class AdhkarAudioManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(AdhkarPlaybackState())
    val state: StateFlow<AdhkarPlaybackState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ttsInitialized = false
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var fallbackJob: Job? = null
    private var fallbackTrack: AudioTrack? = null

    private var activeQueue: List<AdhkarItem> = emptyList()
    private var currentProfile: ReciterVoiceProfile = ReciterVoiceProfile.CALM_KHASHIA
    private var currentSelectedVoiceName: String? = null
    private var customAudioMap: Map<String, CustomAdhkarAudio> = emptyMap()
    private var currentItem: AdhkarItem? = null

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsInitialized = true
                    val arabicLocale = Locale("ar")
                    val res = tts?.setLanguage(arabicLocale)
                    val supported = res != TextToSpeech.LANG_MISSING_DATA &&
                        res != TextToSpeech.LANG_NOT_SUPPORTED
                    val voices = try {
                        tts?.voices
                            ?.filter { it.locale.language.equals("ar", ignoreCase = true) }
                            ?.map { it.name }
                            ?.sorted()
                            ?: emptyList()
                    } catch (_: Exception) {
                        emptyList()
                    }
                    _state.value = _state.value.copy(
                        ttsArabicReady = supported,
                        availableArabicVoiceNames = voices
                    )
                    setupUtteranceListener()
                } else {
                    Log.w(TAG, "TextToSpeech init failed with status $status")
                    _state.value = _state.value.copy(ttsArabicReady = false)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "TextToSpeech unavailable: ${e.message}")
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                scope.launch {
                    _state.value = _state.value.copy(
                        isPlaying = true,
                        isPaused = false,
                        statusNote = null
                    )
                }
            }

            override fun onDone(utteranceId: String?) {
                scope.launch {
                    handleItemCompleted(utteranceId)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                scope.launch {
                    val item = currentItem
                    if (item != null && _state.value.activeItemId == item.id) {
                        startCalmFallbackCadence(item)
                    }
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                scope.launch {
                    val item = currentItem
                    if (item != null && _state.value.activeItemId == item.id) {
                        startCalmFallbackCadence(item)
                    }
                }
            }
        })
    }

    fun updateSettings(
        profile: ReciterVoiceProfile,
        selectedSystemVoiceName: String?,
        customMap: Map<String, CustomAdhkarAudio>
    ) {
        currentProfile = profile
        currentSelectedVoiceName = selectedSystemVoiceName
        customAudioMap = customMap
    }

    fun playSingle(item: AdhkarItem) {
        activeQueue = emptyList()
        startItem(item, isPlaylist = false)
    }

    fun playCategoryPlaylist(items: List<AdhkarItem>) {
        if (items.isEmpty()) return
        activeQueue = items
        startItem(items.first(), isPlaylist = true)
    }

    private fun startItem(item: AdhkarItem, isPlaylist: Boolean) {
        stopInternal(clearQueue = !isPlaylist)
        currentItem = item

        val custom = customAudioMap[item.id]
        if (custom != null) {
            playCustomMedia(item, custom, isPlaylist)
            return
        }

        if (ttsInitialized && _state.value.ttsArabicReady) {
            playWithArabicTts(item, isPlaylist)
        } else {
            startCalmFallbackCadence(item, isPlaylist)
        }
    }

    private fun playCustomMedia(
        item: AdhkarItem,
        custom: CustomAdhkarAudio,
        isPlaylist: Boolean
    ) {
        _state.value = _state.value.copy(
            activeItemId = item.id,
            isPlaying = false,
            isPaused = false,
            isUsingCustomAudio = true,
            isPlayingPlaylist = isPlaylist,
            positionMs = 0L,
            durationMs = 0L,
            statusNote = "قراءة مخصصة: ${custom.fileName}"
        )
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(context, Uri.parse(custom.uri))
                setOnPreparedListener { player ->
                    player.start()
                    _state.value = _state.value.copy(
                        isPlaying = true,
                        isPaused = false,
                        durationMs = player.duration.toLong().coerceAtLeast(1L)
                    )
                    startProgressPolling()
                }
                setOnCompletionListener {
                    scope.launch { handleItemCompleted(item.id) }
                }
                setOnErrorListener { _, _, _ ->
                    Log.w(TAG, "Custom dhikr audio error, falling back to Arabic TTS")
                    if (ttsInitialized && _state.value.ttsArabicReady) {
                        playWithArabicTts(item, isPlaylist)
                    } else {
                        startCalmFallbackCadence(item, isPlaylist)
                    }
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.w(TAG, "Custom dhikr URI failed: ${e.message}")
            if (ttsInitialized && _state.value.ttsArabicReady) {
                playWithArabicTts(item, isPlaylist)
            } else {
                startCalmFallbackCadence(item, isPlaylist)
            }
        }
    }

    private fun playWithArabicTts(item: AdhkarItem, isPlaylist: Boolean) {
        val engine = tts ?: run {
            startCalmFallbackCadence(item, isPlaylist)
            return
        }
        try {
            engine.setPitch(currentProfile.pitch)
            engine.setSpeechRate(currentProfile.speechRate)

            if (!currentSelectedVoiceName.isNullOrBlank()) {
                val matched = engine.voices?.firstOrNull { it.name == currentSelectedVoiceName }
                if (matched != null) {
                    engine.voice = matched
                }
            }

            val sanitized = item.arabicText
                .replace("﴿", "")
                .replace("﴾", "")
                .replace("ﷺ", "صلى الله عليه وسلم")
                .trim()

            _state.value = _state.value.copy(
                activeItemId = item.id,
                isPlaying = true,
                isPaused = false,
                isUsingCustomAudio = false,
                isPlayingPlaylist = isPlaylist,
                positionMs = 0L,
                durationMs = 0L,
                statusNote = currentProfile.arabicTitle
            )

            val result = engine.speak(sanitized, TextToSpeech.QUEUE_FLUSH, null, item.id)
            if (result == TextToSpeech.ERROR) {
                startCalmFallbackCadence(item, isPlaylist)
            }
        } catch (e: Exception) {
            Log.w(TAG, "TTS speak failed: ${e.message}")
            startCalmFallbackCadence(item, isPlaylist)
        }
    }

    private fun startCalmFallbackCadence(item: AdhkarItem, isPlaylist: Boolean = _state.value.isPlayingPlaylist) {
        val approxDurationMs = (item.arabicText.length * 48L).coerceIn(4500L, 16000L)
        _state.value = _state.value.copy(
            activeItemId = item.id,
            isPlaying = true,
            isPaused = false,
            isUsingCustomAudio = false,
            isPlayingPlaylist = isPlaylist,
            positionMs = 0L,
            durationMs = approxDurationMs,
            statusNote = "قراءة تأملية مدمجة (صوت الجهاز العربي غير مثبت)"
        )

        fallbackJob?.cancel()
        fallbackJob = scope.launch(Dispatchers.IO) {
            try {
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
                fallbackTrack = track
                track.play()

                val cadenceNotes = listOf(293.66, 329.63, 369.99, 392.00, 369.99, 329.63)
                var elapsedMs = 0L
                var noteIdx = 0
                val stepMs = 750L
                val samplesPerStep = ((sampleRate * stepMs) / 1000L).toInt()
                val buf = ShortArray(samplesPerStep)

                while (elapsedMs < approxDurationMs && isActive) {
                    while (_state.value.isPaused && isActive) {
                        delay(80L)
                    }
                    if (!isActive) break
                    val freq = cadenceNotes[noteIdx % cadenceNotes.size]
                    for (i in 0 until samplesPerStep) {
                        val t = i.toDouble() / sampleRate
                        val p = i.toDouble() / samplesPerStep
                        val env = sin(PI * p)
                        val wave = 0.65 * sin(2 * PI * freq * t) + 0.25 * sin(2 * PI * freq * 1.5 * t)
                        buf[i] = (wave * env * Short.MAX_VALUE * 0.22).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                    }
                    track.write(buf, 0, samplesPerStep)
                    elapsedMs += stepMs
                    noteIdx++
                    _state.value = _state.value.copy(
                        positionMs = elapsedMs.coerceAtMost(approxDurationMs)
                    )
                }
                track.stop()
                track.release()
                fallbackTrack = null
                if (isActive) {
                    launch(Dispatchers.Main.immediate) { handleItemCompleted(item.id) }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun handleItemCompleted(completedId: String?) {
        val current = _state.value
        if (current.activeItemId != completedId) return

        if (current.isPlayingPlaylist && activeQueue.isNotEmpty()) {
            val idx = activeQueue.indexOfFirst { it.id == completedId }
            if (idx != -1 && idx + 1 < activeQueue.size) {
                startItem(activeQueue[idx + 1], isPlaylist = true)
                return
            }
        }
        stop()
    }

    fun pause() {
        val cur = _state.value
        if (!cur.isPlaying || cur.isPaused) return
        if (cur.isUsingCustomAudio) {
            try {
                mediaPlayer?.pause()
            } catch (_: Exception) {
            }
            _state.value = cur.copy(isPlaying = false, isPaused = true)
        } else if (fallbackJob != null) {
            try {
                fallbackTrack?.pause()
            } catch (_: Exception) {
            }
            _state.value = cur.copy(isPlaying = false, isPaused = true)
        } else {
            // Android TextToSpeech does not support mid-utterance pause reliably; stop cleanly & allow replay
            try {
                tts?.stop()
            } catch (_: Exception) {
            }
            _state.value = cur.copy(isPlaying = false, isPaused = true)
        }
    }

    fun resume() {
        val cur = _state.value
        if (!cur.isPaused) return
        val item = currentItem ?: return
        if (cur.isUsingCustomAudio) {
            try {
                mediaPlayer?.start()
                _state.value = cur.copy(isPlaying = true, isPaused = false)
            } catch (_: Exception) {
                startItem(item, cur.isPlayingPlaylist)
            }
        } else if (fallbackJob != null) {
            try {
                fallbackTrack?.play()
            } catch (_: Exception) {
            }
            _state.value = cur.copy(isPlaying = true, isPaused = false)
        } else {
            playWithArabicTts(item, cur.isPlayingPlaylist)
        }
    }

    fun stop() {
        stopInternal(clearQueue = true)
    }

    private fun stopInternal(clearQueue: Boolean) {
        if (clearQueue) {
            activeQueue = emptyList()
        }
        currentItem = null
        progressJob?.cancel()
        progressJob = null
        fallbackJob?.cancel()
        fallbackJob = null

        try {
            fallbackTrack?.stop()
            fallbackTrack?.release()
        } catch (_: Exception) {
        }
        fallbackTrack = null

        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null

        try {
            tts?.stop()
        } catch (_: Exception) {
        }

        _state.value = _state.value.copy(
            activeItemId = null,
            isPlaying = false,
            isPaused = false,
            isUsingCustomAudio = false,
            isPlayingPlaylist = false,
            positionMs = 0L,
            durationMs = 0L,
            statusNote = null
        )
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val mp = mediaPlayer ?: break
                try {
                    if (mp.isPlaying) {
                        _state.value = _state.value.copy(
                            positionMs = mp.currentPosition.toLong(),
                            durationMs = mp.duration.toLong().coerceAtLeast(1L)
                        )
                    }
                } catch (_: Exception) {
                    break
                }
                delay(250L)
            }
        }
    }

    fun release() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
    }
}
