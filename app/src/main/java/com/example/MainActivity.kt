package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.data.model.MeeqatTab
import com.example.notifications.MeeqatNotificationHelper
import com.example.ui.MeeqatViewModel
import com.example.ui.components.CityPickerDialog
import com.example.ui.components.FloatingSettingsBar
import com.example.ui.components.IslamicGeometricBackdrop
import com.example.ui.components.LuxuryAlertToastBanner
import com.example.ui.components.MeeqatTabStrip
import com.example.ui.components.MeeqatTopHeader
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.CompanionScreen
import com.example.ui.screens.PrayersScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.theme.MeeqatTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before UI or ViewModels
        configureAppCheck(this, intent)
        MeeqatNotificationHelper.ensureChannel(this)

        enableEdgeToEdge()
        setContent {
            val viewModel: MeeqatViewModel = viewModel()
            MeeqatAppRoot(viewModel = viewModel)
        }
    }
}

@Composable
fun MeeqatAppRoot(viewModel: MeeqatViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val adhkarAudioState by viewModel.adhkarAudioState.collectAsStateWithLifecycle()

    var requestedInitialLocation by rememberSaveable { mutableStateOf(false) }
    var pendingCustomAdhkarItemId by rememberSaveable { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.refreshGpsLocation()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val customMp3Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onCustomAudioPicked(it) }
    }

    val customAdhkarMp3Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val itemId = pendingCustomAdhkarItemId
        pendingCustomAdhkarItemId = null
        if (uri != null && itemId != null) {
            viewModel.onCustomAdhkarAudioPicked(itemId, uri)
        }
    }

    fun requestOrRefreshLocation() {
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCoarse) {
            viewModel.refreshGpsLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    fun ensureNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPost = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPost) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!requestedInitialLocation) {
            requestedInitialLocation = true
            val hasCoarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasCoarse) {
                viewModel.refreshGpsLocation()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    // Auto-dismiss visual toast banner after 12 seconds
    LaunchedEffect(uiState.alertBanner?.id) {
        if (uiState.alertBanner != null) {
            delay(12_000L)
            viewModel.dismissAlertBanner()
        }
    }

    // System back button support for drawers & secondary tabs
    BackHandler(
        enabled = uiState.isSettingsOpen ||
            uiState.isCityPickerOpen ||
            uiState.activeTab != MeeqatTab.PRAYERS
    ) {
        when {
            uiState.isSettingsOpen -> viewModel.openSettings(false)
            uiState.isCityPickerOpen -> viewModel.openCityPicker(false)
            uiState.activeTab != MeeqatTab.PRAYERS -> viewModel.selectTab(MeeqatTab.PRAYERS)
        }
    }

    val language = uiState.prefs.language
    val displayCity = uiState.prefs.displayCity(language)
    val anyAudioPlaying = audioState.isPlaying ||
        audioState.isBuffering ||
        adhkarAudioState.isPlaying

    val isReadingTab = uiState.activeTab == MeeqatTab.ADHKAR || uiState.activeTab == MeeqatTab.QUIZ
    val applyComfortableReading = uiState.prefs.comfortableReadingMode && isReadingTab

    MeeqatTheme(
        themeMode = uiState.prefs.themeMode,
        fontStyle = uiState.prefs.fontStyle,
        language = language,
        isComfortableReading = applyComfortableReading
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IslamicGeometricBackdrop()

            Column(modifier = Modifier.fillMaxSize()) {
                MeeqatTopHeader(
                    cityName = displayCity,
                    language = language,
                    isRefreshingLocation = uiState.isRefreshingLocation,
                    masterAlertsEnabled = uiState.prefs.masterAlertsEnabled,
                    isOnline = uiState.isOnline,
                    schedule = uiState.schedule,
                    onToggleAlerts = {
                        if (!uiState.prefs.masterAlertsEnabled) {
                            ensureNotificationPermissionIfNeeded()
                        }
                        viewModel.toggleMasterAlerts()
                    },
                    onRefreshGps = { requestOrRefreshLocation() },
                    onOpenCityPicker = { viewModel.openCityPicker(true) }
                )

                LuxuryAlertToastBanner(
                    banner = uiState.alertBanner,
                    isAudioPlaying = audioState.isPlaying || audioState.isBuffering,
                    onStopAudio = { viewModel.stopAzan() },
                    onDismiss = { viewModel.dismissAlertBanner() },
                    modifier = Modifier.fillMaxWidth()
                )

                MeeqatTabStrip(
                    activeTab = uiState.activeTab,
                    language = language,
                    onSelectTab = { viewModel.selectTab(it) }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Crossfade(
                        targetState = uiState.activeTab,
                        label = "meeqat_tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            MeeqatTab.PRAYERS -> PrayersScreen(
                                schedule = uiState.schedule,
                                countdown = uiState.countdown,
                                language = language,
                                isFetching = uiState.isFetchingPrayers,
                                masterAlertsEnabled = uiState.prefs.masterAlertsEnabled,
                                iqamahMinutes = uiState.prefs.iqamahMinutes,
                                onTogglePrayerMute = { viewModel.togglePrayerMute(it) }
                            )

                            MeeqatTab.QIBLA -> QiblaScreen(
                                qiblaBearing = uiState.qiblaBearing,
                                deviceAzimuth = uiState.deviceAzimuth,
                                hasCompassSensor = uiState.hasCompassSensor,
                                manualOffset = uiState.manualCompassOffset,
                                cityName = displayCity,
                                onManualOffsetChange = { viewModel.setManualCompassOffset(it) }
                            )

                            MeeqatTab.TASBIH -> TasbihScreen(
                                selectedDhikrId = uiState.prefs.tasbihDhikrId,
                                count = uiState.prefs.tasbihCount,
                                totalCount = uiState.prefs.tasbihTotalCount,
                                goal = uiState.prefs.tasbihGoal,
                                language = language,
                                onSelectDhikr = { viewModel.selectDhikr(it) },
                                onSelectGoal = { viewModel.selectTasbihGoal(it) },
                                onIncrement = { viewModel.incrementTasbih() },
                                onReset = { viewModel.resetTasbih() }
                            )

                            MeeqatTab.ADHKAR -> AdhkarScreen(
                                selectedCategory = uiState.selectedAdhkarCategory,
                                language = language,
                                remainingCounts = uiState.adhkarRemainingCounts,
                                reciterProfile = uiState.prefs.reciterProfile,
                                selectedSystemVoiceName = uiState.prefs.reciterSystemVoiceName,
                                customAdhkarAudios = uiState.prefs.customAdhkarAudios,
                                adhkarAudioState = adhkarAudioState,
                                comfortableReadingMode = uiState.prefs.comfortableReadingMode,
                                onToggleComfortableReading = { viewModel.toggleComfortableReadingMode() },
                                onSelectCategory = { viewModel.selectAdhkarCategory(it) },
                                onTapItem = { viewModel.tapAdhkarItem(it) },
                                onResetCategory = { viewModel.resetAdhkarCategory(it) },
                                onSelectReciterProfile = { viewModel.selectReciterProfile(it) },
                                onSelectSystemVoice = { viewModel.selectReciterSystemVoice(it) },
                                onPlayItem = { viewModel.playAdhkarItem(it) },
                                onPauseAudio = { viewModel.pauseAdhkarAudio() },
                                onResumeAudio = { viewModel.resumeAdhkarAudio() },
                                onStopAudio = { viewModel.stopAdhkarAudio() },
                                onPlayAllInCategory = { viewModel.playAllAdhkarInCategory(it) },
                                onPickCustomAudioForItem = { itemId ->
                                    pendingCustomAdhkarItemId = itemId
                                    customAdhkarMp3Launcher.launch(arrayOf("audio/*"))
                                },
                                onClearCustomAudioForItem = { viewModel.clearCustomAdhkarAudio(it) }
                            )

                            MeeqatTab.QUIZ -> QuizScreen(
                                language = language,
                                themeMode = uiState.prefs.themeMode,
                                selectedDifficulty = uiState.quizDifficulty,
                                selectedTopic = uiState.quizTopic,
                                selectedCount = uiState.quizCount,
                                quizUiState = uiState.quizUiState,
                                selectedAnswers = uiState.quizSelectedAnswers,
                                comfortableReadingMode = uiState.prefs.comfortableReadingMode,
                                onToggleComfortableReading = { viewModel.toggleComfortableReadingMode() },
                                onSelectDifficulty = { viewModel.selectQuizDifficulty(it) },
                                onSelectTopic = { viewModel.selectQuizTopic(it) },
                                onSelectCount = { viewModel.selectQuizCount(it) },
                                onGenerateQuiz = { viewModel.generateIslamicQuiz() },
                                onSelectAnswer = { qId, optIdx -> viewModel.selectQuizAnswer(qId, optIdx) },
                                onResetAnswers = { viewModel.resetQuizAnswers() },
                                onAdvanceNextDifficulty = { viewModel.advanceToNextDifficultyAndGenerate() }
                            )

                            MeeqatTab.COMPANION -> CompanionScreen(
                                messages = uiState.chatMessages,
                                companionState = uiState.companionState,
                                isOnline = uiState.isOnline,
                                onSendQuery = { viewModel.sendCompanionQuery(it) }
                            )
                        }
                    }
                }
            }

            // Bottom-Center Floating Settings Bar
            FloatingSettingsBar(
                isAudioPlaying = anyAudioPlaying,
                onOpenSettings = { viewModel.openSettings(true) },
                onStopAudio = {
                    viewModel.stopAzan()
                    viewModel.stopAdhkarAudio()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // City Picker Dialog
            if (uiState.isCityPickerOpen) {
                CityPickerDialog(
                    currentCity = displayCity,
                    language = language,
                    onSelectCity = { viewModel.selectCity(it) },
                    onUseGps = { requestOrRefreshLocation() },
                    onDismiss = { viewModel.openCityPicker(false) }
                )
            }

            // Bottom-Center Glassmorphic Settings Drawer
            if (uiState.isSettingsOpen) {
                SettingsBottomSheet(
                    prefs = uiState.prefs,
                    preferCustomAzan = uiState.preferCustomAzan,
                    audioState = audioState,
                    onDismiss = { viewModel.openSettings(false) },
                    onSelectLanguage = { viewModel.setAppLanguage(it) },
                    onSelectTheme = { viewModel.setThemeMode(it) },
                    onSelectFontStyle = { viewModel.setFontStyle(it) },
                    onToggleComfortableReading = { viewModel.setComfortableReadingMode(it) },
                    onSelectReciterProfile = { viewModel.selectReciterProfile(it) },
                    onToggleMasterAlerts = { enabled ->
                        if (enabled) ensureNotificationPermissionIfNeeded()
                        viewModel.setMasterAlerts(enabled)
                    },
                    onTestAlert = {
                        ensureNotificationPermissionIfNeeded()
                        viewModel.triggerTestAlert()
                    },
                    onIqamahChange = { viewModel.setIqamahMinutes(it) },
                    onSelectPreset = { viewModel.selectAzanPreset(it) },
                    onSelectCustomAudio = { viewModel.useCustomAzanFile() },
                    onPickCustomMp3 = {
                        customMp3Launcher.launch(arrayOf("audio/*"))
                    },
                    onClearCustomMp3 = { viewModel.clearCustomAzan() },
                    onPlayAzan = { viewModel.playSelectedAzan() },
                    onPauseAzan = { viewModel.pauseAzan() },
                    onResumeAzan = { viewModel.resumeAzan() },
                    onStopAzan = { viewModel.stopAzan() }
                )
            }
        }
    }
}
