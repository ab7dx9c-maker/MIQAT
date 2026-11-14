package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.AdhkarPlaybackState
import com.example.data.local.AdhkarCatalog
import com.example.data.model.AdhkarCategory
import com.example.data.model.AdhkarItem
import com.example.data.model.AppLanguage
import com.example.data.model.CustomAdhkarAudio
import com.example.data.model.ReciterVoiceProfile
import com.example.ui.components.ComfortableReadingTogglePill
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.rememberPressScale
import com.example.ui.theme.LocalMeeqatPalette
import com.example.ui.theme.LocalMeeqatStrings

@Composable
fun AdhkarScreen(
    selectedCategory: AdhkarCategory,
    language: AppLanguage,
    remainingCounts: Map<String, Int>,
    reciterProfile: ReciterVoiceProfile,
    selectedSystemVoiceName: String?,
    customAdhkarAudios: Map<String, CustomAdhkarAudio>,
    adhkarAudioState: AdhkarPlaybackState,
    comfortableReadingMode: Boolean,
    onToggleComfortableReading: () -> Unit,
    onSelectCategory: (AdhkarCategory) -> Unit,
    onTapItem: (String) -> Unit,
    onResetCategory: (AdhkarCategory) -> Unit,
    onSelectReciterProfile: (ReciterVoiceProfile) -> Unit,
    onSelectSystemVoice: (String?) -> Unit,
    onPlayItem: (AdhkarItem) -> Unit,
    onPauseAudio: () -> Unit,
    onResumeAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onPlayAllInCategory: (AdhkarCategory) -> Unit,
    onPickCustomAudioForItem: (String) -> Unit,
    onClearCustomAudioForItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val categoryItems = AdhkarCatalog.adhkarItems.filter { it.category == selectedCategory }
    val completedCount = categoryItems.count { (remainingCounts[it.id] ?: it.initialCount) == 0 }
    val progress = if (categoryItems.isNotEmpty()) {
        completedCount.toFloat() / categoryItems.size.toFloat()
    } else 0f

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 112.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdhkarCategory.entries.forEach { cat ->
                        val selected = cat == selectedCategory
                        val (catSource, catScale) = rememberPressScale(0.94f)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) palette.activeChipBg else palette.glassSurface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) palette.goldAccent else palette.goldBorder
                            ),
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = catScale
                                    scaleY = catScale
                                }
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = catSource,
                                    indication = LocalIndication.current
                                ) { onSelectCategory(cat) }
                                .testTag("adhkar_category_${cat.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.title(language),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) palette.activeChipText else palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Quick Toggle for Comfortable Reading Mode (Low Contrast + Classic Naskh)
            item {
                ComfortableReadingTogglePill(
                    enabled = comfortableReadingMode,
                    onToggle = onToggleComfortableReading,
                    testTag = "adhkar_comfort_reading_toggle"
                )
            }

            // Custom Reciter Voice Selector & Playlist Control Card
            item {
                ReciterVoiceSelectorCard(
                    reciterProfile = reciterProfile,
                    language = language,
                    selectedSystemVoiceName = selectedSystemVoiceName,
                    adhkarAudioState = adhkarAudioState,
                    onSelectReciterProfile = onSelectReciterProfile,
                    onSelectSystemVoice = onSelectSystemVoice,
                    onPlayAllInCategory = { onPlayAllInCategory(selectedCategory) },
                    onStopAudio = onStopAudio
                )
            }

            // Category Progress Header
            item {
                LuxuryGlassCard(cornerRadius = 18.dp) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = s.categoryProgress(
                                    selectedCategory.title(language),
                                    completedCount,
                                    categoryItems.size
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onResetCategory(selectedCategory) },
                                modifier = Modifier.testTag("adhkar_reset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RestartAlt,
                                    contentDescription = s.resetAdhkarDesc,
                                    tint = palette.goldPrimary
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            color = if (completedCount == categoryItems.size) palette.statusSuccess else palette.goldPrimary,
                            trackColor = palette.badgeBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )
                    }
                }
            }

            // Interactive Adhkar Cards with Per-Dhikr Audio Play / Pause / Stop / Custom MP3 Controls
            items(categoryItems, key = { it.id }) { item ->
                val remaining = remainingCounts[item.id] ?: item.initialCount
                val customAudio = customAdhkarAudios[item.id]
                val isActiveItem = adhkarAudioState.activeItemId == item.id
                AdhkarInteractiveCard(
                    item = item,
                    language = language,
                    remaining = remaining,
                    customAudio = customAudio,
                    isActiveItem = isActiveItem,
                    isPlaying = isActiveItem && adhkarAudioState.isPlaying,
                    isPaused = isActiveItem && adhkarAudioState.isPaused,
                    statusNote = if (isActiveItem) adhkarAudioState.statusNote else null,
                    positionMs = if (isActiveItem) adhkarAudioState.positionMs else 0L,
                    durationMs = if (isActiveItem) adhkarAudioState.durationMs else 0L,
                    onTap = { onTapItem(item.id) },
                    onPlay = { onPlayItem(item) },
                    onPause = onPauseAudio,
                    onResume = onResumeAudio,
                    onStop = onStopAudio,
                    onPickCustomAudio = { onPickCustomAudioForItem(item.id) },
                    onClearCustomAudio = { onClearCustomAudioForItem(item.id) }
                )
            }
        }
    }
}

@Composable
private fun ReciterVoiceSelectorCard(
    reciterProfile: ReciterVoiceProfile,
    language: AppLanguage,
    selectedSystemVoiceName: String?,
    adhkarAudioState: AdhkarPlaybackState,
    onSelectReciterProfile: (ReciterVoiceProfile) -> Unit,
    onSelectSystemVoice: (String?) -> Unit,
    onPlayAllInCategory: () -> Unit,
    onStopAudio: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val isPlayingPlaylist = adhkarAudioState.isPlayingPlaylist &&
        (adhkarAudioState.isPlaying || adhkarAudioState.isPaused)

    LuxuryGlassCard(
        cornerRadius = 20.dp,
        highlighted = isPlayingPlaylist,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("adhkar_reciter_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = null,
                        tint = palette.goldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = s.reciterVoiceTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = reciterProfile.subtitle(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play All / Stop Playlist Button
                val (allSource, allScale) = rememberPressScale(0.94f)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isPlayingPlaylist) palette.statusError else palette.activeChipBg,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = allScale
                            scaleY = allScale
                        }
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = allSource,
                            indication = LocalIndication.current
                        ) {
                            if (isPlayingPlaylist) onStopAudio() else onPlayAllInCategory()
                        }
                        .testTag("adhkar_play_all_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlayingPlaylist) Icons.Filled.Stop else Icons.Filled.PlaylistPlay,
                            contentDescription = if (isPlayingPlaylist) s.stopAllLabel else s.playAllLabel,
                            tint = if (isPlayingPlaylist) androidx.compose.ui.graphics.Color.White else palette.activeChipText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isPlayingPlaylist) s.stopAllLabel else s.playAllLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isPlayingPlaylist) androidx.compose.ui.graphics.Color.White else palette.activeChipText
                        )
                    }
                }
            }

            // Reciter Voice Profiles Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReciterVoiceProfile.entries.forEach { profile ->
                    val selected = profile == reciterProfile
                    val (profSource, profScale) = rememberPressScale(0.94f)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) palette.activeChipBg else palette.badgeBg,
                        border = BorderStroke(
                            1.dp,
                            if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = profScale
                                scaleY = profScale
                            }
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = profSource,
                                indication = LocalIndication.current
                            ) { onSelectReciterProfile(profile) }
                            .testTag("reciter_profile_${profile.id}")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.title(language),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) palette.activeChipText else palette.textPrimary
                            )
                        }
                    }
                }
            }

            // If device exposes multiple Arabic TTS voices, let user pick one
            if (adhkarAudioState.availableArabicVoiceNames.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = s.deviceVoicesLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary
                    )
                    adhkarAudioState.availableArabicVoiceNames.take(5).forEachIndexed { idx, voiceName ->
                        val selected = selectedSystemVoiceName == voiceName
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) palette.badgeBg else palette.glassSurface,
                            border = BorderStroke(
                                0.8.dp,
                                if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectSystemVoice(if (selected) null else voiceName)
                                }
                        ) {
                            Text(
                                text = s.reciterDevicePrefix(idx + 1),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) palette.goldPrimary else palette.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdhkarInteractiveCard(
    item: AdhkarItem,
    language: AppLanguage,
    remaining: Int,
    customAudio: CustomAdhkarAudio?,
    isActiveItem: Boolean,
    isPlaying: Boolean,
    isPaused: Boolean,
    statusNote: String?,
    positionMs: Long,
    durationMs: Long,
    onTap: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onPickCustomAudio: () -> Unit,
    onClearCustomAudio: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val isCompleted = remaining == 0

    LuxuryGlassCard(
        highlighted = isActiveItem || !isCompleted,
        cornerRadius = 22.dp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isCompleted && !isActiveItem) 0.76f else 1f)
            .testTag("adhkar_card_${item.id}"),
        onClick = if (!isCompleted) onTap else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = item.arabicText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = palette.textPrimary
            )
            if (language == AppLanguage.ENGLISH) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.englishTranslation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary.copy(alpha = 0.88f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.benefit(language),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Per-Dhikr Audio Player Control Strip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isActiveItem) palette.badgeBg else palette.glassSurface.copy(alpha = 0.7f),
                border = BorderStroke(
                    0.8.dp,
                    if (isActiveItem) palette.goldAccent else palette.goldBorder.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Play / Pause / Resume Button for this specific dhikr
                            val (playSource, playScale) = rememberPressScale(0.90f)
                            Surface(
                                shape = CircleShape,
                                color = if (isPlaying) palette.activeChipBg else palette.badgeBg,
                                border = BorderStroke(1.dp, palette.goldAccent),
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = playScale
                                        scaleY = playScale
                                    }
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = playSource,
                                        indication = LocalIndication.current
                                    ) {
                                        when {
                                            isPlaying -> onPause()
                                            isPaused -> onResume()
                                            else -> onPlay()
                                        }
                                    }
                                    .testTag("adhkar_play_pause_${item.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = when {
                                            isPlaying -> s.pauseLabel
                                            else -> s.listenReciter
                                        },
                                        tint = if (isPlaying) palette.activeChipText else palette.goldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Stop Button (always available when active or paused for this dhikr)
                            if (isActiveItem) {
                                val (stopSource, stopScale) = rememberPressScale(0.90f)
                                Surface(
                                    shape = CircleShape,
                                    color = palette.statusError.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, palette.statusError),
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = stopScale
                                            scaleY = stopScale
                                        }
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = stopSource,
                                            indication = LocalIndication.current,
                                            onClick = onStop
                                        )
                                        .testTag("adhkar_stop_${item.id}")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Stop,
                                            contentDescription = s.stopLabel,
                                            tint = palette.statusError,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        isPlaying -> s.readingNow
                                        isPaused -> s.pausedLabel
                                        customAudio != null -> customAudio.fileName
                                        else -> s.listenReciter
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isActiveItem) palette.goldPrimary else palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = statusNote
                                        ?: if (customAudio != null) s.customAudioAttached
                                        else s.independentControlsHint,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Attach / Replace / Remove Custom Audio MP3 for this Dhikr
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onPickCustomAudio,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("adhkar_custom_audio_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (customAudio != null) Icons.Filled.AudioFile else Icons.Filled.UploadFile,
                                    contentDescription = s.attachAudioDesc,
                                    tint = palette.goldPrimary
                                )
                            }
                            if (customAudio != null) {
                                IconButton(
                                    onClick = onClearCustomAudio,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("adhkar_clear_audio_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = s.removeAudioDesc,
                                        tint = palette.statusError
                                    )
                                }
                            }
                        }
                    }

                    if (isActiveItem && durationMs > 0L) {
                        val p = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { p },
                            color = palette.goldPrimary,
                            trackColor = palette.glassSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    } else if (isPlaying) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = palette.goldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            LinearProgressIndicator(
                                color = palette.goldPrimary,
                                trackColor = palette.glassSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.reference(language),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.goldPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCompleted) palette.statusSuccess.copy(alpha = 0.2f) else palette.activeChipBg,
                    border = if (isCompleted) BorderStroke(1.dp, palette.statusSuccess) else null,
                    modifier = Modifier.height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = s.completedLabel,
                                tint = palette.statusSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = s.completedLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.statusSuccess
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(palette.activeChipText.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$remaining",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.activeChipText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.tapToRepeat,
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.activeChipText
                            )
                        }
                    }
                }
            }
        }
    }
}
