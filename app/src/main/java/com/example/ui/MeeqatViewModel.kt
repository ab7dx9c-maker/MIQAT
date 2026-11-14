package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AdhkarAudioManager
import com.example.audio.AdhkarPlaybackState
import com.example.audio.AzanAudioManager
import com.example.audio.AzanPlaybackState
import com.example.data.local.AdhkarCatalog
import com.example.data.local.MeeqatPreferencesRepository
import com.example.data.local.MeeqatUserPrefs
import com.example.data.local.OfflineIslamicGuide
import com.example.data.local.OfflineIslamicQuizBank
import com.example.data.model.AdhkarCategory
import com.example.data.model.AdhkarItem
import com.example.data.model.AiChatMessage
import com.example.data.model.AlertBannerState
import com.example.data.model.AppLanguage
import com.example.data.model.ArabicFontStyle
import com.example.data.model.AzanPreset
import com.example.data.model.CityOption
import com.example.data.model.CountdownState
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.IslamicQuizQuestion
import com.example.data.model.MeeqatTab
import com.example.data.model.MeeqatThemeMode
import com.example.data.model.PrayerKey
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizTopic
import com.example.data.model.QuizUiState
import com.example.data.model.ReciterVoiceProfile
import com.example.data.model.TasbihGoal
import com.example.data.remote.PrayerRepository
import com.example.location.MeeqatLocationHelper
import com.example.notifications.MeeqatNotificationHelper
import com.example.sensor.QiblaCalculator
import com.example.sensor.QiblaSensorManager
import com.example.util.NetworkStatusHelper
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Calendar
import java.util.Locale

private const val TAG = "MeeqatViewModel"

sealed interface CompanionUiState {
    data object Idle : CompanionUiState
    data object Loading : CompanionUiState
    data object Streaming : CompanionUiState
    data class Error(val message: String) : CompanionUiState
}

data class MeeqatMainUiState(
    val activeTab: MeeqatTab = MeeqatTab.PRAYERS,
    val prefs: MeeqatUserPrefs = MeeqatUserPrefs(),
    val schedule: DayPrayerSchedule? = null,
    val isFetchingPrayers: Boolean = true,
    val isRefreshingLocation: Boolean = false,
    val countdown: CountdownState? = null,
    val qiblaBearing: Float = 277.0f,
    val deviceAzimuth: Float? = null,
    val hasCompassSensor: Boolean = false,
    val manualCompassOffset: Float = 0f,
    val selectedAdhkarCategory: AdhkarCategory = AdhkarCategory.MORNING,
    val adhkarRemainingCounts: Map<String, Int> = AdhkarCatalog.adhkarItems.associate { it.id to it.initialCount },
    val alertBanner: AlertBannerState? = null,
    val isSettingsOpen: Boolean = false,
    val isCityPickerOpen: Boolean = false,
    val preferCustomAzan: Boolean = false,
    val chatMessages: List<AiChatMessage> = listOf(
        AiChatMessage(
            id = "welcome",
            isUser = false,
            text = "أهلاً بك في المرشد الإسلامي الذكي بميقات. اسألني عن معاني الأذكار، آداب الصلاة، الأدعية المأثورة، أو أحكام العبادات اليومية."
        )
    ),
    val companionState: CompanionUiState = CompanionUiState.Idle,
    val quizDifficulty: QuizDifficulty = QuizDifficulty.EASY,
    val quizTopic: QuizTopic = QuizTopic.HISTORY_THEOLOGY,
    val quizCount: Int = 5,
    val quizUiState: QuizUiState = QuizUiState.Idle,
    val quizSelectedAnswers: Map<String, Int> = emptyMap(),
    val isOnline: Boolean = true
)

private data class QuizPanelState(
    val difficulty: QuizDifficulty = QuizDifficulty.EASY,
    val topic: QuizTopic = QuizTopic.HISTORY_THEOLOGY,
    val count: Int = 5,
    val uiState: QuizUiState = QuizUiState.Idle,
    val selectedAnswers: Map<String, Int> = emptyMap()
)

class MeeqatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = MeeqatPreferencesRepository(application)
    private val prayerRepo = PrayerRepository(application)
    private val locationHelper = MeeqatLocationHelper(application)
    private val qiblaSensor = QiblaSensorManager(application)
    val audioManager = AzanAudioManager(application)
    val adhkarAudioManager = AdhkarAudioManager(application)

    private val geminiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "You are the \"Smart Islamic Guide\" (المرشد الذكي) inside the Meeqat app. Reply in the exact language requested in the prompt (Arabic or English) with a warm, serene, concise, and well-structured tone. Reference authentic Qur'an verses or Sahih Hadith when helpful, and consider the user's city, Hijri date, and upcoming prayer context."
                )
            }
        )
    }

    private val quizResponseSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "questions" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "question" to Schema.string(description = "The Islamic quiz question text"),
                            "options" to Schema.array(
                                items = Schema.string(),
                                description = "Exactly 4 distinct multiple-choice options"
                            ),
                            "correctIndex" to Schema.integer(description = "Zero-based index (0 to 3) of the correct option"),
                            "explanation" to Schema.string(description = "Concise authentic explanation citing Qur'an or Hadith when helpful"),
                            "topicLabel" to Schema.string(description = "Short topic badge such as Seerah, Fiqh, or Tafsir")
                        )
                    ),
                    description = "Generated multiple-choice Islamic quiz questions"
                )
            )
        )
    }

    private val quizModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = quizResponseSchema
                temperature = 0.85f
            },
            systemInstruction = content {
                text(
                    "You are an authentic Islamic scholar and educator generating multiple-choice religious quiz questions for the Meeqat app. Verify facts carefully against the Qur'an, authentic Sunnah, and mainstream Islamic scholarship. Each question must have exactly 4 distinct options, one unambiguous correctIndex (0..3), and a brief helpful explanation."
                )
            }
        )
    }

    private val _activeTab = MutableStateFlow(MeeqatTab.PRAYERS)
    private val _schedule = MutableStateFlow<DayPrayerSchedule?>(null)
    private val _isFetchingPrayers = MutableStateFlow(true)
    private val _isRefreshingLocation = MutableStateFlow(false)
    private val _countdown = MutableStateFlow<CountdownState?>(null)
    private val _deviceAzimuth = MutableStateFlow<Float?>(null)
    private val _manualCompassOffset = MutableStateFlow(0f)
    private val _selectedAdhkarCategory = MutableStateFlow(AdhkarCategory.MORNING)
    private val _adhkarCounts = MutableStateFlow(
        AdhkarCatalog.adhkarItems.associate { it.id to it.initialCount }
    )
    private val _alertBanner = MutableStateFlow<AlertBannerState?>(null)
    private val _isSettingsOpen = MutableStateFlow(false)
    private val _isCityPickerOpen = MutableStateFlow(false)
    private val _preferCustomAzan = MutableStateFlow(false)
    private val _chatMessages = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = "welcome",
                isUser = false,
                text = "أهلاً بك في المرشد الإسلامي الذكي بميقات. اسألني عن معاني الأذكار، فضل الصلوات، الأدعية المأثورة، أو تأملات إيمانية تناسب وقتك الحالي."
            )
        )
    )
    private val _companionState = MutableStateFlow<CompanionUiState>(CompanionUiState.Idle)
    private val _quizPanelState = MutableStateFlow(QuizPanelState())
    private val _isOnline = MutableStateFlow(NetworkStatusHelper.isOnline(application))

    private var latestPrefs = MeeqatUserPrefs()
    private var lastLoadedCoordKey: String? = null
    private var lastTriggeredAlertKey: String? = null

    val audioState: StateFlow<AzanPlaybackState> = audioManager.playbackState
    val adhkarAudioState: StateFlow<AdhkarPlaybackState> = adhkarAudioManager.state

    val uiState: StateFlow<MeeqatMainUiState> = combine(
        _activeTab,
        prefsRepo.prefsFlow,
        _schedule,
        _isFetchingPrayers,
        _isRefreshingLocation,
        _countdown,
        _deviceAzimuth,
        _manualCompassOffset,
        _selectedAdhkarCategory,
        _adhkarCounts,
        _alertBanner,
        _isSettingsOpen,
        _isCityPickerOpen,
        _preferCustomAzan,
        _chatMessages,
        _companionState,
        _quizPanelState,
        _isOnline
    ) { values ->
        val tab = values[0] as MeeqatTab
        val prefs = values[1] as MeeqatUserPrefs
        val schedule = values[2] as DayPrayerSchedule?
        val fetching = values[3] as Boolean
        val refreshingLoc = values[4] as Boolean
        val countdown = values[5] as CountdownState?
        val azimuth = values[6] as Float?
        val manualOffset = values[7] as Float
        val adhkarCat = values[8] as AdhkarCategory
        @Suppress("UNCHECKED_CAST")
        val adhkarMap = values[9] as Map<String, Int>
        val alert = values[10] as AlertBannerState?
        val settingsOpen = values[11] as Boolean
        val cityPickerOpen = values[12] as Boolean
        val preferCustom = values[13] as Boolean
        @Suppress("UNCHECKED_CAST")
        val messages = values[14] as List<AiChatMessage>
        val compState = values[15] as CompanionUiState
        val quizState = values[16] as QuizPanelState
        val online = values[17] as Boolean

        MeeqatMainUiState(
            activeTab = tab,
            prefs = prefs,
            schedule = schedule,
            isFetchingPrayers = fetching,
            isRefreshingLocation = refreshingLoc,
            countdown = countdown,
            qiblaBearing = QiblaCalculator.calculateQiblaBearing(prefs.latitude, prefs.longitude),
            deviceAzimuth = azimuth,
            hasCompassSensor = qiblaSensor.hasCompassSensor,
            manualCompassOffset = manualOffset,
            selectedAdhkarCategory = adhkarCat,
            adhkarRemainingCounts = adhkarMap,
            alertBanner = alert,
            isSettingsOpen = settingsOpen,
            isCityPickerOpen = cityPickerOpen,
            preferCustomAzan = preferCustom,
            chatMessages = messages,
            companionState = compState,
            quizDifficulty = quizState.difficulty,
            quizTopic = quizState.topic,
            quizCount = quizState.count,
            quizUiState = quizState.uiState,
            quizSelectedAnswers = quizState.selectedAnswers,
            isOnline = online
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MeeqatMainUiState(
            hasCompassSensor = qiblaSensor.hasCompassSensor,
            isOnline = NetworkStatusHelper.isOnline(application)
        )
    )

    init {
        observePreferences()
        observeNetworkStatus()
        observeCompassSensor()
        startLiveClockLoop()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            var wasOnline = _isOnline.value
            NetworkStatusHelper.observeOnlineStatus(getApplication()).collect { online ->
                _isOnline.value = online
                if (online && !wasOnline && _schedule.value?.isFromApi == false) {
                    fetchSchedule(latestPrefs)
                }
                wasOnline = online
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            prefsRepo.prefsFlow.collect { prefs ->
                val langChanged = latestPrefs.language != prefs.language
                latestPrefs = prefs
                adhkarAudioManager.updateSettings(
                    profile = prefs.reciterProfile,
                    selectedSystemVoiceName = prefs.reciterSystemVoiceName,
                    customMap = prefs.customAdhkarAudios
                )
                if (langChanged) {
                    updateCountdownAndAlerts()
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == "welcome") {
                            msg.copy(
                                text = if (prefs.language == AppLanguage.ENGLISH) {
                                    "Welcome to Meeqat Smart Islamic Guide. Ask me about the meanings of daily Adhkar, prayer etiquette, authentic supplications, or daily spiritual reflections."
                                } else {
                                    "أهلاً بك في المرشد الإسلامي الذكي بميقات. اسألني عن معاني الأذكار، فضل الصلوات، الأدعية المأثورة، أو تأملات إيمانية تناسب وقتك الحالي."
                                }
                            )
                        } else msg
                    }
                }
                if (!prefs.customAzanUri.isNullOrBlank() && _preferCustomAzan.value.not() &&
                    lastLoadedCoordKey == null
                ) {
                    _preferCustomAzan.value = true
                }
                val coordKey = "${prefs.latitude},${prefs.longitude}"
                if (coordKey != lastLoadedCoordKey) {
                    lastLoadedCoordKey = coordKey
                    fetchSchedule(prefs)
                } else {
                    _schedule.value?.let { current ->
                        _schedule.value = prayerRepo.rebuildWithSettings(
                            current,
                            prefs.iqamahMinutes,
                            prefs.mutedPrayers
                        )
                    }
                }
            }
        }
    }

    private fun fetchSchedule(prefs: MeeqatUserPrefs) {
        viewModelScope.launch {
            _isFetchingPrayers.value = true
            val result = prayerRepo.fetchPrayerSchedule(
                latitude = prefs.latitude,
                longitude = prefs.longitude,
                iqamahMinutes = prefs.iqamahMinutes,
                mutedPrayers = prefs.mutedPrayers
            )
            _schedule.value = result
            _isFetchingPrayers.value = false
            updateCountdownAndAlerts()
        }
    }

    private fun observeCompassSensor() {
        viewModelScope.launch {
            qiblaSensor.observeAzimuthDegrees().collect { azimuth ->
                _deviceAzimuth.value = azimuth
            }
        }
    }

    private fun startLiveClockLoop() {
        viewModelScope.launch {
            while (isActive) {
                updateCountdownAndAlerts()
                delay(1000L)
            }
        }
    }

    private fun updateCountdownAndAlerts() {
        val currentSchedule = _schedule.value ?: return
        val now = Calendar.getInstance()
        val nowSeconds =
            now.get(Calendar.HOUR_OF_DAY) * 3600 +
                now.get(Calendar.MINUTE) * 60 +
                now.get(Calendar.SECOND)

        val prayers = currentSchedule.prayers
        val nextIndex = prayers.indexOfFirst { it.minutesFromMidnight * 60 > nowSeconds }
        val (nextPrayer, remainingSec, prevSec) = if (nextIndex != -1) {
            val target = prayers[nextIndex]
            val prev = if (nextIndex > 0) prayers[nextIndex - 1].minutesFromMidnight * 60 else 0
            Triple(target, (target.minutesFromMidnight * 60) - nowSeconds, prev)
        } else {
            val fajr = prayers.first()
            val prev = prayers.last().minutesFromMidnight * 60
            val rem = (24 * 3600 - nowSeconds) + (fajr.minutesFromMidnight * 60)
            Triple(fajr, rem, prev)
        }

        val totalSpan = if (nextIndex != -1) {
            (nextPrayer.minutesFromMidnight * 60 - prevSec).coerceAtLeast(1)
        } else {
            ((24 * 3600 - prevSec) + nextPrayer.minutesFromMidnight * 60).coerceAtLeast(1)
        }
        val progress = (1f - (remainingSec.toFloat() / totalSpan.toFloat())).coerceIn(0f, 1f)

        val hrs = remainingSec / 3600
        val mins = (remainingSec % 3600) / 60
        val secs = remainingSec % 60
        val formatted = String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)

        val lang = latestPrefs.language
        _countdown.value = CountdownState(
            nextPrayerName = nextPrayer.key.title(lang),
            nextPrayerKey = nextPrayer.key,
            nextAzanDisplay = nextPrayer.azanDisplay(lang),
            formattedRemaining = formatted,
            progress = progress
        )

        // Update isNext badge dynamically when minute crosses a prayer
        val currentNext = prayers.firstOrNull { it.isNext }?.key
        if (currentNext != nextPrayer.key) {
            _schedule.value = currentSchedule.copy(
                prayers = prayers.map { it.copy(isNext = it.key == nextPrayer.key) }
            )
        }

        // Check Azan / Iqamah live alert trigger (on exact minute boundary seconds 0..2)
        val nowMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val secOfMinute = now.get(Calendar.SECOND)
        if (secOfMinute <= 2 && latestPrefs.masterAlertsEnabled) {
            val dayKey = "${now.get(Calendar.DAY_OF_YEAR)}"
            for (p in prayers) {
                if (p.isMuted) continue
                if (p.minutesFromMidnight == nowMinute) {
                    val triggerKey = "$dayKey-azan-${p.key.name}-$nowMinute"
                    if (lastTriggeredAlertKey != triggerKey) {
                        lastTriggeredAlertKey = triggerKey
                        val title = if (lang == AppLanguage.ENGLISH) {
                            "It is now time for ${p.key.englishName} Azan"
                        } else {
                            "حان الآن موعد أذان ${p.key.arabicName}"
                        }
                        firePrayerAlert(
                            title = title,
                            subtitle = "${latestPrefs.displayCity(lang)} • ${p.azanDisplay(lang)}",
                            isIqamah = false
                        )
                    }
                } else if (p.iqamahMinutesFromMidnight == nowMinute && p.key.hasIqamah) {
                    val triggerKey = "$dayKey-iqamah-${p.key.name}-$nowMinute"
                    if (lastTriggeredAlertKey != triggerKey) {
                        lastTriggeredAlertKey = triggerKey
                        val title = if (lang == AppLanguage.ENGLISH) {
                            "Iqamah time for ${p.key.englishName} Prayer"
                        } else {
                            "حان الآن وقت إقامة صلاة ${p.key.arabicName}"
                        }
                        val sub = if (lang == AppLanguage.ENGLISH) {
                            "Prepare for prayer • ${p.iqamahDisplay(lang) ?: ""}"
                        } else {
                            "استعدوا للصلاة • ${p.iqamahDisplay(lang) ?: ""}"
                        }
                        firePrayerAlert(
                            title = title,
                            subtitle = sub,
                            isIqamah = true
                        )
                    }
                }
            }
        }
    }

    private fun firePrayerAlert(title: String, subtitle: String, isIqamah: Boolean) {
        _alertBanner.value = AlertBannerState(
            id = System.currentTimeMillis(),
            title = title,
            subtitle = subtitle,
            isIqamah = isIqamah
        )
        MeeqatNotificationHelper.showPrayerNotification(getApplication(), title, subtitle)
        if (!isIqamah) {
            playSelectedAzan()
        } else {
            audioManager.playTasbihClick(isGoalReached = true)
        }
        vibratePulse(longPulse = true)
    }

    fun dismissAlertBanner() {
        _alertBanner.value = null
    }

    fun selectTab(tab: MeeqatTab) {
        _activeTab.value = tab
    }

    fun openSettings(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun openCityPicker(open: Boolean) {
        _isCityPickerOpen.value = open
    }

    fun setThemeMode(mode: MeeqatThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setFontStyle(style: ArabicFontStyle) {
        viewModelScope.launch { prefsRepo.setFontStyle(style) }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setAppLanguage(language) }
    }

    fun setComfortableReadingMode(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setComfortableReadingMode(enabled) }
    }

    fun toggleComfortableReadingMode() {
        viewModelScope.launch {
            prefsRepo.setComfortableReadingMode(!latestPrefs.comfortableReadingMode)
        }
    }

    fun toggleMasterAlerts() {
        viewModelScope.launch {
            prefsRepo.setMasterAlerts(!latestPrefs.masterAlertsEnabled)
        }
    }

    fun setMasterAlerts(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setMasterAlerts(enabled) }
    }

    fun togglePrayerMute(prayer: PrayerKey) {
        viewModelScope.launch { prefsRepo.togglePrayerMute(prayer) }
    }

    fun setIqamahMinutes(minutes: Int) {
        viewModelScope.launch { prefsRepo.setIqamahMinutes(minutes) }
    }

    fun selectAzanPreset(preset: AzanPreset) {
        _preferCustomAzan.value = false
        viewModelScope.launch { prefsRepo.setAzanPreset(preset) }
    }

    fun useCustomAzanFile() {
        if (!latestPrefs.customAzanUri.isNullOrBlank()) {
            _preferCustomAzan.value = true
        }
    }

    fun onCustomAudioPicked(uri: Uri) {
        val app = getApplication<Application>()
        try {
            app.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
        val fileName = resolveFileName(uri) ?: "ملف أذان مخصص.mp3"
        _preferCustomAzan.value = true
        viewModelScope.launch {
            prefsRepo.setCustomAzan(uri.toString(), fileName)
        }
    }

    fun clearCustomAzan() {
        _preferCustomAzan.value = false
        viewModelScope.launch { prefsRepo.setCustomAzan(null, null) }
    }

    private fun resolveFileName(uri: Uri): String? {
        val resolver = getApplication<Application>().contentResolver
        return try {
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    cursor.getString(nameIndex)
                } else null
            }
        } catch (_: Exception) {
            uri.lastPathSegment
        }
    }

    fun playSelectedAzan() {
        adhkarAudioManager.stop()
        audioManager.playAzan(
            preset = latestPrefs.selectedAzanPreset,
            customUriString = latestPrefs.customAzanUri,
            customName = latestPrefs.customAzanName,
            preferCustom = _preferCustomAzan.value && !latestPrefs.customAzanUri.isNullOrBlank()
        )
    }

    fun pauseAzan() = audioManager.pause()
    fun resumeAzan() = audioManager.resume()
    fun stopAzan() = audioManager.stop()
    fun seekAzan(positionMs: Long) = audioManager.seekTo(positionMs)

    fun triggerTestAlert() {
        val lang = latestPrefs.language
        val nextName = _countdown.value?.nextPrayerName ?: PrayerKey.DHUHR.title(lang)
        val soundName = if (_preferCustomAzan.value && !latestPrefs.customAzanName.isNullOrBlank()) {
            latestPrefs.customAzanName
        } else {
            latestPrefs.selectedAzanPreset.title(lang)
        }
        val title = if (lang == AppLanguage.ENGLISH) {
            "Test Alert: Time for $nextName Azan"
        } else {
            "تنبيه تجريبي: حان وقت أذان $nextName"
        }
        val subtitle = if (lang == AppLanguage.ENGLISH) {
            "${latestPrefs.displayCity(lang)} • Audio: $soundName"
        } else {
            "${latestPrefs.displayCity(lang)} • الصوت: $soundName"
        }
        firePrayerAlert(
            title = title,
            subtitle = subtitle,
            isIqamah = false
        )
    }

    fun refreshGpsLocation() {
        viewModelScope.launch {
            _isRefreshingLocation.value = true
            val detected = locationHelper.fetchCoarseLocation()
            if (detected != null) {
                prefsRepo.setLocation(
                    city = detected.cityArabic,
                    cityEn = detected.cityEnglish,
                    country = detected.countryArabic,
                    countryEn = detected.countryEnglish,
                    latitude = detected.latitude,
                    longitude = detected.longitude,
                    isGps = true
                )
            }
            _isRefreshingLocation.value = false
        }
    }

    fun selectCity(city: CityOption) {
        _isCityPickerOpen.value = false
        viewModelScope.launch {
            prefsRepo.setLocation(
                city = city.arabicName,
                cityEn = city.englishName,
                country = city.countryArabic,
                countryEn = city.countryEnglish,
                latitude = city.latitude,
                longitude = city.longitude,
                isGps = false
            )
        }
    }

    fun setManualCompassOffset(degrees: Float) {
        _manualCompassOffset.value = degrees
    }

    // Digital Tasbih
    fun incrementTasbih() {
        val goalTarget = latestPrefs.tasbihGoal.target
        val nextCount = if (goalTarget != null && latestPrefs.tasbihCount >= goalTarget) {
            1
        } else {
            latestPrefs.tasbihCount + 1
        }
        val reached = goalTarget != null && nextCount == goalTarget
        val nextTotal = latestPrefs.tasbihTotalCount + 1

        audioManager.playTasbihClick(isGoalReached = reached)
        vibratePulse(longPulse = reached)

        viewModelScope.launch {
            prefsRepo.updateTasbih(
                dhikrId = latestPrefs.tasbihDhikrId,
                count = nextCount,
                total = nextTotal,
                goal = latestPrefs.tasbihGoal
            )
        }
    }

    fun resetTasbih() {
        viewModelScope.launch {
            prefsRepo.updateTasbih(
                dhikrId = latestPrefs.tasbihDhikrId,
                count = 0,
                total = latestPrefs.tasbihTotalCount,
                goal = latestPrefs.tasbihGoal
            )
        }
    }

    fun selectDhikr(dhikrId: String) {
        viewModelScope.launch {
            prefsRepo.updateTasbih(
                dhikrId = dhikrId,
                count = 0,
                total = latestPrefs.tasbihTotalCount,
                goal = latestPrefs.tasbihGoal
            )
        }
    }

    fun selectTasbihGoal(goal: TasbihGoal) {
        viewModelScope.launch {
            prefsRepo.updateTasbih(
                dhikrId = latestPrefs.tasbihDhikrId,
                count = 0,
                total = latestPrefs.tasbihTotalCount,
                goal = goal
            )
        }
    }

    // Daily Adhkar
    fun selectAdhkarCategory(category: AdhkarCategory) {
        _selectedAdhkarCategory.value = category
    }

    fun tapAdhkarItem(itemId: String) {
        val current = _adhkarCounts.value[itemId] ?: return
        if (current <= 0) return
        val updated = current - 1
        _adhkarCounts.value = _adhkarCounts.value.toMutableMap().apply {
            put(itemId, updated)
        }
        audioManager.playTasbihClick(isGoalReached = updated == 0)
        vibratePulse(longPulse = updated == 0)
    }

    fun resetAdhkarCategory(category: AdhkarCategory) {
        val updated = _adhkarCounts.value.toMutableMap()
        AdhkarCatalog.adhkarItems
            .filter { it.category == category }
            .forEach { updated[it.id] = it.initialCount }
        _adhkarCounts.value = updated
    }

    // Adhkar Reciter Audio Controls
    fun playAdhkarItem(item: AdhkarItem) {
        audioManager.stop()
        adhkarAudioManager.playSingle(item)
    }

    fun pauseAdhkarAudio() = adhkarAudioManager.pause()

    fun resumeAdhkarAudio() = adhkarAudioManager.resume()

    fun stopAdhkarAudio() = adhkarAudioManager.stop()

    fun playAllAdhkarInCategory(category: AdhkarCategory) {
        audioManager.stop()
        val items = AdhkarCatalog.adhkarItems.filter { it.category == category }
        adhkarAudioManager.playCategoryPlaylist(items)
    }

    fun selectReciterProfile(profile: ReciterVoiceProfile) {
        viewModelScope.launch { prefsRepo.setReciterProfile(profile) }
    }

    fun selectReciterSystemVoice(voiceName: String?) {
        viewModelScope.launch { prefsRepo.setReciterSystemVoice(voiceName) }
    }

    fun onCustomAdhkarAudioPicked(dhikrId: String, uri: Uri) {
        val app = getApplication<Application>()
        try {
            app.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
        val fileName = resolveFileName(uri) ?: "تلاوة مخصصة.mp3"
        viewModelScope.launch {
            prefsRepo.setCustomAdhkarAudio(dhikrId, uri.toString(), fileName)
        }
    }

    fun clearCustomAdhkarAudio(dhikrId: String) {
        if (adhkarAudioState.value.activeItemId == dhikrId) {
            adhkarAudioManager.stop()
        }
        viewModelScope.launch {
            prefsRepo.setCustomAdhkarAudio(dhikrId, null, null)
        }
    }

    // Gemini AI Islamic Companion (with seamless Offline Local Guide)
    fun sendCompanionQuery(userQuery: String) {
        val trimmed = userQuery.trim()
        if (trimmed.isEmpty() || _companionState.value == CompanionUiState.Loading ||
            _companionState.value == CompanionUiState.Streaming
        ) return

        val lang = latestPrefs.language
        val userMsg = AiChatMessage(
            id = "u-${System.currentTimeMillis()}",
            isUser = true,
            text = trimmed
        )

        fun buildLocalAnswer(): String = OfflineIslamicGuide.answer(
            query = trimmed,
            language = lang,
            cityName = latestPrefs.displayCity(lang),
            hijriDate = _schedule.value?.hijriDate?.formatted(lang),
            nextPrayerName = _countdown.value?.nextPrayerName,
            nextPrayerTime = _countdown.value?.nextAzanDisplay,
            remaining = _countdown.value?.formattedRemaining
        )

        // Instant offline guide path when no internet is available
        if (!NetworkStatusHelper.isOnline(getApplication())) {
            val localMsg = AiChatMessage(
                id = "m-local-${System.currentTimeMillis() + 1}",
                isUser = false,
                text = buildLocalAnswer(),
                isStreaming = false
            )
            _chatMessages.value = _chatMessages.value + userMsg + localMsg
            _companionState.value = CompanionUiState.Idle
            return
        }

        val modelMsgId = "m-${System.currentTimeMillis() + 1}"
        _chatMessages.value = _chatMessages.value + userMsg + AiChatMessage(
            id = modelMsgId,
            isUser = false,
            text = "",
            isStreaming = true
        )
        _companionState.value = CompanionUiState.Loading

        val contextPrompt = buildString {
            appendLine(
                if (lang == AppLanguage.ENGLISH) {
                    "Respond in clear, warm English. User context:"
                } else {
                    "أجب باللغة العربية الفصحى الميسرة. السياق الحالي للمستخدم:"
                }
            )
            appendLine("- City: ${latestPrefs.displayCity(lang)}, ${latestPrefs.displayCountry(lang)}")
            _schedule.value?.let {
                appendLine("- Hijri Date: ${it.hijriDate.formatted(lang)}")
            }
            _countdown.value?.let {
                appendLine("- Next Prayer: ${it.nextPrayerName} (${it.nextAzanDisplay}), remaining ${it.formattedRemaining}")
            }
            appendLine("\nQuestion: $trimmed")
        }

        viewModelScope.launch {
            val buffer = StringBuilder()
            try {
                withTimeout(60_000L) {
                    geminiModel.generateContentStream(contextPrompt).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        if (piece.isNotEmpty()) {
                            buffer.append(piece)
                            _companionState.value = CompanionUiState.Streaming
                            updateStreamMessage(modelMsgId, buffer.toString(), streaming = true)
                        }
                    }
                }
                if (buffer.isNotBlank()) {
                    updateStreamMessage(modelMsgId, buffer.toString(), streaming = false)
                    _companionState.value = CompanionUiState.Idle
                } else {
                    updateStreamMessage(modelMsgId, buildLocalAnswer(), streaming = false)
                    _companionState.value = CompanionUiState.Idle
                }
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "Gemini streaming timed out, falling back to OfflineIslamicGuide", e)
                if (buffer.isBlank()) {
                    updateStreamMessage(modelMsgId, buildLocalAnswer(), streaming = false)
                    _companionState.value = CompanionUiState.Idle
                } else {
                    updateStreamMessage(modelMsgId, buffer.toString(), streaming = false)
                    _companionState.value = CompanionUiState.Idle
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Gemini AI call failed, falling back to OfflineIslamicGuide", e)
                if (buffer.isBlank()) {
                    updateStreamMessage(modelMsgId, buildLocalAnswer(), streaming = false)
                    _companionState.value = CompanionUiState.Idle
                } else {
                    updateStreamMessage(modelMsgId, buffer.toString(), streaming = false)
                    _companionState.value = CompanionUiState.Idle
                }
            }
        }
    }

    private fun updateStreamMessage(id: String, text: String, streaming: Boolean) {
        _chatMessages.value = _chatMessages.value.map { msg ->
            if (msg.id == id) msg.copy(text = text, isStreaming = streaming) else msg
        }
    }

    private fun removeEmptyMessage(id: String) {
        _chatMessages.value = _chatMessages.value.filterNot { it.id == id && it.text.isBlank() }
    }

    // Islamic Knowledge Quiz Generator (Firebase AI Logic Structured JSON)
    fun selectQuizDifficulty(difficulty: QuizDifficulty) {
        _quizPanelState.value = _quizPanelState.value.copy(difficulty = difficulty)
    }

    fun selectQuizTopic(topic: QuizTopic) {
        _quizPanelState.value = _quizPanelState.value.copy(topic = topic)
    }

    fun selectQuizCount(count: Int) {
        _quizPanelState.value = _quizPanelState.value.copy(count = count.coerceIn(3, 10))
    }

    fun selectQuizAnswer(questionId: String, optionIndex: Int) {
        val current = _quizPanelState.value
        if (current.selectedAnswers.containsKey(questionId)) return
        val ready = current.uiState as? QuizUiState.Ready ?: return
        val question = ready.questions.firstOrNull { it.id == questionId } ?: return
        val isCorrect = optionIndex == question.correctIndex

        audioManager.playTasbihClick(isGoalReached = isCorrect)
        vibratePulse(longPulse = isCorrect)

        _quizPanelState.value = current.copy(
            selectedAnswers = current.selectedAnswers + (questionId to optionIndex)
        )
    }

    fun resetQuizAnswers() {
        _quizPanelState.value = _quizPanelState.value.copy(selectedAnswers = emptyMap())
    }

    fun advanceToNextDifficultyAndGenerate() {
        val next = when (_quizPanelState.value.difficulty) {
            QuizDifficulty.EASY -> QuizDifficulty.MEDIUM
            QuizDifficulty.MEDIUM -> QuizDifficulty.HARD
            QuizDifficulty.HARD -> QuizDifficulty.HARD
        }
        _quizPanelState.value = _quizPanelState.value.copy(difficulty = next)
        generateIslamicQuiz()
    }

    fun generateIslamicQuiz() {
        val currentPanel = _quizPanelState.value
        if (currentPanel.uiState is QuizUiState.Loading) return

        val difficulty = currentPanel.difficulty
        val topic = currentPanel.topic
        val count = currentPanel.count
        val lang = latestPrefs.language

        val difficultyGuidance = when (difficulty) {
            QuizDifficulty.EASY -> "EASY: foundational Islamic history & theology (Six Articles of Iman, Five Pillars, well-known Prophets, early Makkah/Madinah milestones)."
            QuizDifficulty.MEDIUM -> "MEDIUM: intermediate Islamic history & theology (major events in Prophetic Seerah, Rightly Guided Caliphs, attributes of Allah, Names of Allah, reasons for revelation)."
            QuizDifficulty.HARD -> "HARD: advanced scholarly Islamic history & theology (classical Islamic historiography, Abbasid/Umayyad/Andalusian eras, Hadith sciences, nuanced Aqeedah & Tafsir scholarship). Avoid obvious trivia."
        }

        val topicInstruction = when (topic) {
            QuizTopic.HISTORY_THEOLOGY -> "Cover a balanced mix of Islamic History (Prophetic era, Caliphs, classical Islamic civilization) and Islamic Theology (Aqeedah, Articles of Faith, Tawhid, Names of Allah)."
            QuizTopic.THEOLOGY -> "Focus strictly on Islamic Theology (Aqeedah, Tawhid, Articles of Iman, Angels, Divine Books, Qadr, Day of Judgment)."
            QuizTopic.HISTORY -> "Focus strictly on Islamic History (Prophetic era, Rightly Guided Caliphs, battles, treaties, scholars, and historic Islamic cities)."
            QuizTopic.SEERAH -> "Focus strictly on the Prophetic Seerah (Makkan and Madinan eras, Hijrah, battles, treaties, and noble character)."
            QuizTopic.QURAN -> "Focus strictly on the Holy Qur'an, revelation history, Surahs, and Tafsir."
            QuizTopic.FIQH -> "Focus strictly on Fiqh & Acts of Worship (Salah, Zakah, Sawm, Hajj, Taharah)."
        }

        val langInstruction = if (lang == AppLanguage.ENGLISH) {
            "Write every question, all 4 options, topicLabel, and explanation in clear, natural English."
        } else {
            "اكتب كل سؤال والخيارات الأربعة والتصنيف والشرح باللغة العربية الفصحى الواضحة والسليمة."
        }

        val prompt = buildString {
            appendLine("Generate exactly $count unique Islamic multiple-choice questions.")
            appendLine("Difficulty level: $difficultyGuidance")
            appendLine("Topic focus: $topicInstruction")
            appendLine("Language requirement: $langInstruction")
            appendLine("Ensure each question has exactly 4 distinct options, an accurate 0-based correctIndex (0..3), and a brief factual explanation.")
        }

        // Instant offline question bank when device has no internet
        if (!NetworkStatusHelper.isOnline(getApplication())) {
            val localQuestions = OfflineIslamicQuizBank.getQuestions(
                difficulty = difficulty,
                topic = topic,
                count = count,
                language = lang
            )
            _quizPanelState.value = currentPanel.copy(
                uiState = QuizUiState.Ready(
                    questions = localQuestions,
                    difficulty = difficulty,
                    topic = topic,
                    isOfflineBank = true
                ),
                selectedAnswers = emptyMap()
            )
            return
        }

        _quizPanelState.value = currentPanel.copy(
            uiState = QuizUiState.Loading,
            selectedAnswers = emptyMap()
        )

        viewModelScope.launch {
            try {
                val response = withTimeout(60_000L) {
                    quizModel.generateContent(prompt)
                }
                val jsonString = response.text
                    ?: throw IllegalStateException("No quiz data returned.")

                val root = JSONObject(jsonString)
                val questionsArray = root.getJSONArray("questions")
                val batchStamp = System.currentTimeMillis()
                val parsed = buildList {
                    for (i in 0 until questionsArray.length()) {
                        val obj = questionsArray.getJSONObject(i)
                        val qText = obj.optString("question").trim()
                        val optionsJson = obj.getJSONArray("options")
                        val options = (0 until optionsJson.length())
                            .map { optionsJson.getString(it).trim() }
                            .filter { it.isNotEmpty() }
                            .take(4)
                        if (qText.isNotEmpty() && options.size >= 2) {
                            val rawCorrect = obj.optInt("correctIndex", 0)
                            val safeCorrect = rawCorrect.coerceIn(0, options.lastIndex)
                            add(
                                IslamicQuizQuestion(
                                    id = "quiz_${batchStamp}_$i",
                                    question = qText,
                                    options = options,
                                    correctIndex = safeCorrect,
                                    explanation = obj.optString("explanation").trim(),
                                    topicLabel = obj.optString("topicLabel").trim()
                                )
                            )
                        }
                    }
                }

                if (parsed.isEmpty()) {
                    val localFallback = OfflineIslamicQuizBank.getQuestions(difficulty, topic, count, lang)
                    _quizPanelState.value = _quizPanelState.value.copy(
                        uiState = QuizUiState.Ready(
                            questions = localFallback,
                            difficulty = difficulty,
                            topic = topic,
                            isOfflineBank = true
                        )
                    )
                } else {
                    _quizPanelState.value = _quizPanelState.value.copy(
                        uiState = QuizUiState.Ready(
                            questions = parsed,
                            difficulty = difficulty,
                            topic = topic,
                            isOfflineBank = false
                        )
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "Quiz generation timed out, falling back to OfflineIslamicQuizBank", e)
                val localFallback = OfflineIslamicQuizBank.getQuestions(difficulty, topic, count, lang)
                _quizPanelState.value = _quizPanelState.value.copy(
                    uiState = QuizUiState.Ready(
                        questions = localFallback,
                        difficulty = difficulty,
                        topic = topic,
                        isOfflineBank = true
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Online quiz generation failed, falling back to OfflineIslamicQuizBank", e)
                val localFallback = OfflineIslamicQuizBank.getQuestions(difficulty, topic, count, lang)
                _quizPanelState.value = _quizPanelState.value.copy(
                    uiState = QuizUiState.Ready(
                        questions = localFallback,
                        difficulty = difficulty,
                        topic = topic,
                        isOfflineBank = true
                    )
                )
            }
        }
    }

    private fun vibratePulse(longPulse: Boolean) {
        try {
            val app = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = app.getSystemService(VibratorManager::class.java)
                mgr?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                app.getSystemService(Vibrator::class.java)
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            if (longPulse) 140L else 22L,
                            if (longPulse) VibrationEffect.DEFAULT_AMPLITUDE else 70
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (longPulse) 140L else 22L)
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.stop()
        adhkarAudioManager.release()
    }
}
