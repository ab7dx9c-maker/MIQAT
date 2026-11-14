package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AdhkarCatalog
import com.example.data.model.AlertBannerState
import com.example.data.model.AppLanguage
import com.example.data.model.CityOption
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.MeeqatTab
import com.example.ui.theme.LocalMeeqatPalette
import com.example.ui.theme.LocalMeeqatStrings
import com.example.ui.theme.StatusCrimsonBright
import com.example.ui.theme.StatusEmeraldBright
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IslamicGeometricBackdrop(modifier: Modifier = Modifier) {
    val palette = LocalMeeqatPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.backgroundTop,
                        palette.backgroundMid,
                        palette.backgroundBottom
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 110.dp.toPx()
            val radius = 34.dp.toPx()
            val cols = (size.width / step).toInt() + 2
            val rows = (size.height / step).toInt() + 2

            // Subtle radial glow at top (toned down in comfortable reading mode)
            val glowAlpha = when {
                palette.isComfortableReading -> 0.04f
                palette.isDark -> 0.14f
                else -> 0.12f
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.goldAccent.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.12f),
                    radius = size.width * 0.75f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.12f),
                radius = size.width * 0.75f
            )

            for (r in 0..rows) {
                for (c in 0..cols) {
                    val cx = c * step + if (r % 2 == 1) step * 0.5f else 0f
                    val cy = r * step
                    drawRubElHizb(
                        center = Offset(cx, cy),
                        radius = radius,
                        color = palette.patternTint
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRubElHizb(
    center: Offset,
    radius: Float,
    color: Color
) {
    val stroke = Stroke(width = 1.dp.toPx())
    val half = radius * 0.72f
    val rectPath = Path().apply {
        moveTo(center.x - half, center.y - half)
        lineTo(center.x + half, center.y - half)
        lineTo(center.x + half, center.y + half)
        lineTo(center.x - half, center.y + half)
        close()
    }
    drawPath(rectPath, color = color, style = stroke)
    rotate(degrees = 45f, pivot = center) {
        drawPath(rectPath, color = color, style = stroke)
    }
    drawCircle(color = color, radius = radius * 0.22f, center = center, style = stroke)
}

@Composable
fun rememberPressScale(
    targetScale: Float = 0.96f
): Pair<MutableInteractionSource, Float> {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tactile_press_scale"
    )
    return interactionSource to scale
}

@Composable
fun ComfortableReadingTogglePill(
    enabled: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val (pressSource, pressScale) = rememberPressScale(0.96f)
    val pillBg by animateColorAsState(
        targetValue = if (enabled) palette.activeChipBg else palette.glassSurface,
        animationSpec = tween(220),
        label = "comfort_pill_bg"
    )
    val pillBorder by animateColorAsState(
        targetValue = if (enabled) palette.goldAccent else palette.goldBorder.copy(alpha = 0.6f),
        animationSpec = tween(220),
        label = "comfort_pill_border"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = pillBg,
        border = BorderStroke(if (enabled) 1.3.dp else 1.dp, pillBorder),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = pressSource,
                indication = LocalIndication.current,
                onClick = onToggle
            )
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoStories,
                    contentDescription = s.comfortableReadingTitle,
                    tint = if (enabled) palette.activeChipText else palette.goldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (enabled) s.comfortableReadingBadgeOn else s.comfortableReadingBadgeOff,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) palette.activeChipText else palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) palette.activeChipText else palette.goldPrimary.copy(alpha = 0.45f)
                    )
            )
        }
    }
}

@Composable
fun LuxuryGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val shape = RoundedCornerShape(cornerRadius)
    val borderColor = if (highlighted) {
        palette.goldAccent.copy(alpha = 0.92f)
    } else {
        palette.goldBorder
    }
    val borderWidth = if (highlighted) 1.5.dp else 1.dp

    val baseBrush = if (highlighted) {
        val highlightAlpha = when {
            palette.isComfortableReading -> 0.07f
            palette.isDark -> 0.18f
            else -> 0.14f
        }
        Brush.verticalGradient(
            colors = listOf(
                palette.glassElevated,
                palette.goldAccent.copy(alpha = highlightAlpha)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                palette.glassElevated,
                palette.glassSurface
            )
        )
    }

    val (interactionSource, pressScale) = rememberPressScale(0.975f)
    val cardElevation = when {
        palette.isComfortableReading -> if (highlighted) 2.dp else 1.dp
        highlighted -> 10.dp
        else -> 4.dp
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                if (onClick != null) {
                    scaleX = pressScale
                    scaleY = pressScale
                }
            }
            .shadow(
                elevation = cardElevation,
                shape = shape,
                ambientColor = palette.goldAccent.copy(alpha = if (palette.isComfortableReading) 0.08f else 0.22f),
                spotColor = palette.goldAccent.copy(alpha = if (palette.isComfortableReading) 0.10f else 0.25f)
            )
            .clip(shape)
            .background(baseBrush)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        palette.specularHighlight,
                        borderColor,
                        borderColor.copy(alpha = 0.45f)
                    )
                ),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun MeeqatTopHeader(
    cityName: String,
    language: AppLanguage,
    isRefreshingLocation: Boolean,
    masterAlertsEnabled: Boolean,
    schedule: DayPrayerSchedule?,
    onToggleAlerts: () -> Unit,
    onRefreshGps: () -> Unit,
    onOpenCityPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Notification Quick Toggle Bell with live status dot
            LuxuryGlassCard(
                cornerRadius = 16.dp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("header_notification_toggle"),
                onClick = onToggleAlerts
            ) {
                Box(
                    modifier = Modifier.padding(11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (masterAlertsEnabled) {
                            Icons.Filled.Notifications
                        } else {
                            Icons.Filled.NotificationsOff
                        },
                        contentDescription = if (masterAlertsEnabled) s.alertsActiveDesc else s.alertsMutedDesc,
                        tint = if (masterAlertsEnabled) palette.goldPrimary else palette.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(
                                if (masterAlertsEnabled) StatusEmeraldBright else StatusCrimsonBright
                            )
                            .border(1.dp, palette.glassElevated, CircleShape)
                    )
                }
            }

            // Center: App Emblem + Logo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CrescentStarEmblem(
                        tint = palette.goldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = s.appTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.goldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Compact Location Pill + Refresh GPS button
            LuxuryGlassCard(
                cornerRadius = 16.dp,
                modifier = Modifier
                    .widthIn(max = 152.dp)
                    .testTag("header_location_pill"),
                onClick = onOpenCityPicker
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = s.changeCityDesc,
                        tint = palette.goldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cityName,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(palette.badgeBg)
                            .clickable(onClick = onRefreshGps)
                            .testTag("refresh_gps_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRefreshingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 1.8.dp,
                                color = palette.goldPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = s.refreshGpsDesc,
                                tint = palette.goldPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // Dual Hijri & Gregorian Date Strip
        Spacer(modifier = Modifier.height(6.dp))
        val hijriText = schedule?.hijriDate?.formatted(language) ?: s.loadingHijri
        val gregText = schedule?.gregorianDate(language) ?: ""
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.badgeBg,
            border = BorderStroke(0.8.dp, palette.goldBorder.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = hijriText,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.goldPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (gregText.isNotBlank()) {
                    Text(
                        text = "  •  $gregText",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CrescentStarEmblem(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.48f
        val cy = h * 0.52f
        val rOuter = w * 0.42f
        val path = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    cx - rOuter, cy - rOuter, cx + rOuter, cy + rOuter
                )
            )
        }
        val cutout = Path().apply {
            val ox = cx - rOuter * 0.36f
            val oy = cy - rOuter * 0.14f
            val rInner = rOuter * 0.80f
            addOval(
                androidx.compose.ui.geometry.Rect(
                    ox - rInner, oy - rInner, ox + rInner, oy + rInner
                )
            )
        }
        val crescent = Path.combine(androidx.compose.ui.graphics.PathOperation.Difference, path, cutout)
        drawPath(crescent, color = tint)

        val sx = w * 0.32f
        val sy = h * 0.36f
        val sr = w * 0.14f
        val star = Path()
        for (i in 0 until 8) {
            val angle = i * Math.PI / 4.0
            val rad = if (i % 2 == 0) sr else sr * 0.45f
            val x = sx + (rad * cos(angle)).toFloat()
            val y = sy + (rad * sin(angle)).toFloat()
            if (i == 0) star.moveTo(x, y) else star.lineTo(x, y)
        }
        star.close()
        drawPath(star, color = tint)
    }
}

@Composable
fun MeeqatTabStrip(
    activeTab: MeeqatTab,
    language: AppLanguage,
    onSelectTab: (MeeqatTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MeeqatTab.entries.forEach { tab ->
            val selected = tab == activeTab
            val icon = when (tab) {
                MeeqatTab.PRAYERS -> Icons.Filled.Mosque
                MeeqatTab.QIBLA -> Icons.Filled.Explore
                MeeqatTab.TASBIH -> Icons.Filled.TouchApp
                MeeqatTab.ADHKAR -> Icons.Filled.MenuBook
                MeeqatTab.QUIZ -> Icons.Filled.Quiz
                MeeqatTab.COMPANION -> Icons.Filled.AutoAwesome
            }
            val label = tab.title(language)
            val (interactionSource, pressScale) = rememberPressScale(0.94f)
            val chipColor by animateColorAsState(
                targetValue = if (selected) palette.activeChipBg else palette.glassSurface,
                animationSpec = tween(220),
                label = "tab_bg_${tab.name}"
            )
            val borderColor by animateColorAsState(
                targetValue = if (selected) palette.goldAccent else palette.goldBorder,
                animationSpec = tween(220),
                label = "tab_border_${tab.name}"
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = chipColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = borderColor
                ),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current
                    ) { onSelectTab(tab) }
                    .testTag("tab_${tab.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) palette.activeChipText else palette.goldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) palette.activeChipText else palette.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun LuxuryAlertToastBanner(
    banner: AlertBannerState?,
    isAudioPlaying: Boolean,
    onStopAudio: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current

    AnimatedVisibility(
        visible = banner != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        banner?.let { item ->
            LuxuryGlassCard(
                highlighted = true,
                cornerRadius = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("alert_toast_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(palette.badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = palette.goldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    }
                    if (isAudioPlaying) {
                        IconButton(
                            onClick = onStopAudio,
                            modifier = Modifier.testTag("banner_stop_audio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = s.stopAzanDesc,
                                tint = StatusCrimsonBright
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("banner_dismiss_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = s.dismissAlertDesc,
                            tint = palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CityPickerDialog(
    currentCity: String,
    language: AppLanguage,
    onSelectCity: (CityOption) -> Unit,
    onUseGps: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current

    Dialog(onDismissRequest = onDismiss) {
        LuxuryGlassCard(
            cornerRadius = 24.dp,
            highlighted = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("city_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = s.chooseCityTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.goldPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = s.close,
                            tint = palette.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.activeChipBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onUseGps()
                            onDismiss()
                        }
                        .testTag("city_picker_gps_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = palette.activeChipText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = s.detectGpsButton,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.activeChipText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AdhkarCatalog.fallbackCities, key = { it.id }) { city ->
                        val cityLabel = city.name(language)
                        val isSelected = cityLabel == currentCity || city.arabicName == currentCity
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) palette.badgeBg else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectCity(city) }
                                .testTag("city_option_${city.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cityLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = city.country(language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
