package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CountdownState
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerKey
import com.example.data.model.PrayerTimeItem
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.theme.LocalMeeqatPalette
import com.example.ui.theme.LocalMeeqatStrings
import com.example.ui.theme.StatusCrimsonBright

@Composable
fun PrayersScreen(
    schedule: DayPrayerSchedule?,
    countdown: CountdownState?,
    language: AppLanguage,
    isFetching: Boolean,
    masterAlertsEnabled: Boolean,
    iqamahMinutes: Int,
    onTogglePrayerMute: (PrayerKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current

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
            item {
                HeroCountdownCard(
                    countdown = countdown,
                    schedule = schedule,
                    language = language,
                    isFetching = isFetching,
                    iqamahMinutes = iqamahMinutes
                )
            }

            if (isFetching && schedule == null) {
                item {
                    LuxuryGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = palette.goldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = s.loadingPrayers,
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.textPrimary
                            )
                        }
                    }
                }
            } else if (schedule != null) {
                items(schedule.prayers, key = { it.key.name }) { item ->
                    PrayerTimeRowCard(
                        item = item,
                        language = language,
                        masterAlertsEnabled = masterAlertsEnabled,
                        onToggleMute = { onTogglePrayerMute(item.key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCountdownCard(
    countdown: CountdownState?,
    schedule: DayPrayerSchedule?,
    language: AppLanguage,
    isFetching: Boolean,
    iqamahMinutes: Int
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current

    LuxuryGlassCard(
        highlighted = true,
        cornerRadius = 26.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_countdown_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.badgeBg,
                    border = BorderStroke(0.8.dp, palette.goldBorder)
                ) {
                    Text(
                        text = if (countdown != null) {
                            "${s.nextPrayerPrefix} ${countdown.nextPrayerName}"
                        } else {
                            s.prayerTimesFallbackTitle
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.goldPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                Text(
                    text = if (schedule?.isFromApi == true) s.apiMethodBadge else (schedule?.methodName(language) ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = s.timeUntilAzan,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = countdown?.formattedRemaining ?: "--:--:--",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 42.sp,
                        letterSpacing = 2.sp
                    ),
                    color = palette.goldPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("countdown_timer_text")
                )
            }

            if (countdown != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${s.azanAtPrefix} ${countdown.nextPrayerName}: ${countdown.nextAzanDisplay}  •  ${s.iqamahAfterMinutes(iqamahMinutes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { countdown.progress },
                    color = palette.goldPrimary,
                    trackColor = palette.badgeBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            }

            if (isFetching) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s.updatingTimings,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun PrayerTimeRowCard(
    item: PrayerTimeItem,
    language: AppLanguage,
    masterAlertsEnabled: Boolean,
    onToggleMute: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val icon = prayerIconFor(item.key)
    val prayerTitle = item.key.title(language)
    val iqamahText = item.iqamahDisplay(language)

    LuxuryGlassCard(
        highlighted = item.isNext,
        cornerRadius = 20.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${item.key.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (item.isNext) palette.activeChipBg else palette.badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = prayerTitle,
                    tint = if (item.isNext) palette.activeChipText else palette.goldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prayerTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.isNext) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.activeChipBg
                        ) {
                            Text(
                                text = s.nextBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.activeChipText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (iqamahText != null) {
                    Text(
                        text = "${s.iqamahPrefix} $iqamahText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary
                    )
                } else {
                    Text(
                        text = s.sunriseSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary
                    )
                }
            }

            Text(
                text = item.azanDisplay(language),
                style = MaterialTheme.typography.titleMedium,
                color = if (item.isNext) palette.goldPrimary else palette.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(6.dp))

            val isActiveAlert = masterAlertsEnabled && !item.isMuted
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("prayer_bell_${item.key.name.lowercase()}")
            ) {
                Icon(
                    imageVector = if (isActiveAlert) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                    contentDescription = prayerTitle,
                    tint = when {
                        !masterAlertsEnabled -> palette.textSecondary.copy(alpha = 0.5f)
                        item.isMuted -> StatusCrimsonBright
                        else -> palette.goldPrimary
                    }
                )
            }
        }
    }
}

private fun prayerIconFor(key: PrayerKey): ImageVector = when (key) {
    PrayerKey.FAJR -> Icons.Filled.WbTwilight
    PrayerKey.SUNRISE -> Icons.Filled.WbSunny
    PrayerKey.DHUHR -> Icons.Filled.BrightnessHigh
    PrayerKey.ASR -> Icons.Filled.BrightnessMedium
    PrayerKey.MAGHRIB -> Icons.Filled.NightsStay
    PrayerKey.ISHA -> Icons.Filled.Bedtime
}
