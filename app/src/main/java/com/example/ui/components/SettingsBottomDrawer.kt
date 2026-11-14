package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.AzanPlaybackState
import com.example.data.local.MeeqatUserPrefs
import com.example.data.model.AppLanguage
import com.example.data.model.ArabicFontStyle
import com.example.data.model.AzanPreset
import com.example.data.model.MeeqatThemeMode
import com.example.data.model.ReciterVoiceProfile
import com.example.ui.theme.LocalMeeqatPalette
import com.example.ui.theme.LocalMeeqatStrings
import com.example.ui.theme.StatusCrimsonBright
import com.example.ui.theme.StatusEmeraldBright
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun FloatingSettingsBar(
    isAudioPlaying: Boolean,
    onOpenSettings: () -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val (settingsSource, settingsScale) = rememberPressScale(0.95f)
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = palette.glassElevated,
                border = BorderStroke(1.3.dp, palette.goldAccent),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = settingsScale
                        scaleY = settingsScale
                    }
                    .height(52.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = settingsSource,
                        indication = LocalIndication.current,
                        onClick = onOpenSettings
                    )
                    .testTag("floating_settings_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = s.settingsTitle,
                        tint = palette.goldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = s.settingsTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isAudioPlaying) {
                val (stopSource, stopScale) = rememberPressScale(0.92f)
                Surface(
                    shape = CircleShape,
                    color = StatusCrimsonBright,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = stopScale
                            scaleY = stopScale
                        }
                        .size(52.dp)
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = stopSource,
                            indication = LocalIndication.current,
                            onClick = onStopAudio
                        )
                        .testTag("floating_stop_azan_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = s.stopAzanDesc,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    prefs: MeeqatUserPrefs,
    preferCustomAzan: Boolean,
    audioState: AzanPlaybackState,
    onDismiss: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (MeeqatThemeMode) -> Unit,
    onSelectFontStyle: (ArabicFontStyle) -> Unit,
    onToggleComfortableReading: (Boolean) -> Unit,
    onSelectReciterProfile: (ReciterVoiceProfile) -> Unit,
    onToggleMasterAlerts: (Boolean) -> Unit,
    onTestAlert: () -> Unit,
    onIqamahChange: (Int) -> Unit,
    onSelectPreset: (AzanPreset) -> Unit,
    onSelectCustomAudio: () -> Unit,
    onPickCustomMp3: () -> Unit,
    onClearCustomMp3: () -> Unit,
    onPlayAzan: () -> Unit,
    onPauseAzan: () -> Unit,
    onResumeAzan: () -> Unit,
    onStopAzan: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val lang = prefs.language
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.glassElevated,
        contentColor = palette.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drawer Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = palette.goldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = s.settingsDrawerTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.goldPrimary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("settings_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = s.close,
                        tint = palette.textSecondary
                    )
                }
            }

            // 0. Language Selector (Arabic / English)
            LuxuryGlassCard(cornerRadius = 18.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            tint = palette.goldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.languageSectionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppLanguage.entries.forEach { option ->
                            val selected = prefs.language == option
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) palette.activeChipBg else Color.Transparent,
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectLanguage(option) }
                                    .testTag("language_option_${option.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = option.nativeLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) palette.activeChipText else palette.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. Dual Theme Switcher
            LuxuryGlassCard(cornerRadius = 18.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = s.themeSectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeOptionButton(
                            title = MeeqatThemeMode.PEARL_WHITE.title(lang),
                            icon = Icons.Filled.LightMode,
                            selected = prefs.themeMode == MeeqatThemeMode.PEARL_WHITE,
                            swatchColors = listOf(Color(0xFFFDFBF6), Color(0xFFD4AF37)),
                            onClick = { onSelectTheme(MeeqatThemeMode.PEARL_WHITE) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_pearl_white")
                        )
                        ThemeOptionButton(
                            title = MeeqatThemeMode.ROYAL_BLACK.title(lang),
                            icon = Icons.Filled.DarkMode,
                            selected = prefs.themeMode == MeeqatThemeMode.ROYAL_BLACK,
                            swatchColors = listOf(Color(0xFF0D1018), Color(0xFFE8C55A)),
                            onClick = { onSelectTheme(MeeqatThemeMode.ROYAL_BLACK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_royal_black")
                        )
                    }
                }
            }

            // 1b. Arabic Calligraphy Font Switcher (Ruq'ah vs Modern)
            LuxuryGlassCard(cornerRadius = 18.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = s.fontSectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ArabicFontStyle.entries.forEach { style ->
                            val selected = prefs.fontStyle == style
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) palette.badgeBg else Color.Transparent,
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectFontStyle(style) }
                                    .testTag("font_style_${style.id}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = style.title(lang),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) palette.goldPrimary else palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1b-2. Comfortable Reading Mode (Low contrast + Classic Naskh for Adhkar & Quiz)
            LuxuryGlassCard(
                cornerRadius = 18.dp,
                highlighted = prefs.comfortableReadingMode,
                onClick = { onToggleComfortableReading(!prefs.comfortableReadingMode) },
                modifier = Modifier.testTag("settings_comfortable_reading_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(palette.badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoStories,
                                contentDescription = s.comfortableReadingTitle,
                                tint = palette.goldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.comfortableReadingTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                            Text(
                                text = s.comfortableReadingSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = prefs.comfortableReadingMode,
                        onCheckedChange = onToggleComfortableReading,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.activeChipText,
                            checkedTrackColor = palette.goldPrimary
                        ),
                        modifier = Modifier.testTag("settings_comfortable_reading_switch")
                    )
                }
            }

            // 1c. Adhkar Reciter Voice Profile
            LuxuryGlassCard(cornerRadius = 18.dp) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = s.reciterVoiceTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    ReciterVoiceProfile.entries.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { profile ->
                                val selected = prefs.reciterProfile == profile
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) palette.badgeBg else Color.Transparent,
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectReciterProfile(profile) }
                                        .testTag("settings_reciter_${profile.id}")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = profile.title(lang),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) palette.goldPrimary else palette.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Master Notification Switch & Test Alert
            LuxuryGlassCard(cornerRadius = 18.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.alertsSectionTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                            Text(
                                text = if (prefs.masterAlertsEnabled) s.alertsSyncedActive else s.alertsSyncedMuted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (prefs.masterAlertsEnabled) StatusEmeraldBright else StatusCrimsonBright
                            )
                        }
                        Switch(
                            checked = prefs.masterAlertsEnabled,
                            onCheckedChange = onToggleMasterAlerts,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = palette.activeChipText,
                                checkedTrackColor = palette.goldPrimary
                            ),
                            modifier = Modifier.testTag("settings_master_alerts_switch")
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.badgeBg,
                        border = BorderStroke(1.dp, palette.goldBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onTestAlert)
                            .testTag("test_alert_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = palette.goldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.testAlertButton,
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.goldPrimary
                            )
                        }
                    }
                }
            }

            // 3. Iqamah Interval Customizer
            LuxuryGlassCard(cornerRadius = 18.dp) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = s.iqamahIntervalTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.badgeBg
                        ) {
                            Text(
                                text = s.minutesUnit(prefs.iqamahMinutes),
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.goldPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Slider(
                        value = prefs.iqamahMinutes.toFloat(),
                        onValueChange = { onIqamahChange(it.roundToInt()) },
                        valueRange = 5f..30f,
                        steps = 24,
                        colors = SliderDefaults.colors(
                            thumbColor = palette.goldPrimary,
                            activeTrackColor = palette.goldPrimary
                        ),
                        modifier = Modifier.testTag("iqamah_slider")
                    )
                }
            }

            // 4. Azan Audio Engine, Presets, Custom MP3 Upload & Built-in Player
            LuxuryGlassCard(cornerRadius = 18.dp) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = s.azanAudioSectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )

                    AzanPreset.entries.forEach { preset ->
                        val selected = !preferCustomAzan && prefs.selectedAzanPreset == preset
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) palette.badgeBg else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectPreset(preset) }
                                .testTag("azan_preset_${preset.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.GraphicEq,
                                    contentDescription = null,
                                    tint = if (selected) palette.goldPrimary else palette.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.title(lang),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = preset.subtitle(lang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Custom MP3 Picker
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (preferCustomAzan && !prefs.customAzanUri.isNullOrBlank()) {
                            palette.badgeBg
                        } else Color.Transparent,
                        border = BorderStroke(1.dp, palette.goldBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (!prefs.customAzanUri.isNullOrBlank()) onSelectCustomAudio()
                                else onPickCustomMp3()
                            }
                            .testTag("custom_mp3_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (prefs.customAzanName != null) Icons.Filled.AudioFile else Icons.Filled.UploadFile,
                                contentDescription = s.customMp3Prompt,
                                tint = palette.goldPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prefs.customAzanName ?: s.customMp3Prompt,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (prefs.customAzanName != null) s.customMp3SavedHint else s.customMp3PickHint,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary
                                )
                            }
                            if (prefs.customAzanName != null) {
                                IconButton(
                                    onClick = onPickCustomMp3,
                                    modifier = Modifier.testTag("replace_custom_mp3_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.UploadFile,
                                        contentDescription = s.attachAudioDesc,
                                        tint = palette.goldPrimary
                                    )
                                }
                                IconButton(
                                    onClick = onClearCustomMp3,
                                    modifier = Modifier.testTag("clear_custom_mp3_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = s.removeAudioDesc,
                                        tint = StatusCrimsonBright
                                    )
                                }
                            }
                        }
                    }

                    // Built-in Audio Player Controls
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.glassSurface,
                        border = BorderStroke(1.dp, palette.goldBorder.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = audioState.activeTitle.ifBlank {
                                        if (preferCustomAzan && prefs.customAzanName != null) {
                                            prefs.customAzanName
                                        } else {
                                            prefs.selectedAzanPreset.title(lang)
                                        }
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.goldPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${formatMs(audioState.positionMs)} / ${formatMs(audioState.durationMs)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val progress = if (audioState.durationMs > 0) {
                                (audioState.positionMs.toFloat() / audioState.durationMs.toFloat()).coerceIn(0f, 1f)
                            } else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                color = palette.goldPrimary,
                                trackColor = palette.badgeBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = palette.activeChipBg,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            when {
                                                audioState.isPlaying -> onPauseAzan()
                                                audioState.isPaused -> onResumeAzan()
                                                else -> onPlayAzan()
                                            }
                                        }
                                        .testTag("player_play_pause_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = if (audioState.isPlaying) s.pauseLabel else s.playAzanLabel,
                                            tint = palette.activeChipText
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = palette.badgeBg,
                                    border = BorderStroke(1.dp, palette.goldBorder),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onStopAzan)
                                        .testTag("player_stop_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Stop,
                                            contentDescription = s.stopLabel,
                                            tint = palette.goldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    swatchColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) palette.badgeBg else Color.Transparent,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(swatchColors))
                    .border(1.dp, palette.goldAccent, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.goldPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSec = ms / 1000L
    val m = totalSec / 60L
    val s = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d", m, s)
}
