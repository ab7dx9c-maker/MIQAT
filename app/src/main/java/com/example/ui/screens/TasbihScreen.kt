package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AdhkarCatalog
import com.example.data.model.AppLanguage
import com.example.data.model.TasbihGoal
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.rememberPressScale
import com.example.ui.theme.LocalMeeqatPalette
import com.example.ui.theme.LocalMeeqatStrings

@Composable
fun TasbihScreen(
    selectedDhikrId: String,
    count: Int,
    totalCount: Int,
    goal: TasbihGoal,
    language: AppLanguage,
    onSelectDhikr: (String) -> Unit,
    onSelectGoal: (TasbihGoal) -> Unit,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val currentDhikr = AdhkarCatalog.dhikrPresets.firstOrNull { it.id == selectedDhikrId }
        ?: AdhkarCatalog.dhikrPresets.first()

    val target = goal.target
    val progressRatio = if (target != null && target > 0) {
        (count.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    } else {
        ((count % 33).toFloat() / 33f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tasbih_ring"
    )

    var tapPulse by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (tapPulse) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        finishedListener = { tapPulse = false },
        label = "tasbih_bounce"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 112.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Dhikr Preset Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdhkarCatalog.dhikrPresets.forEach { dhikr ->
                    val selected = dhikr.id == currentDhikr.id
                    val (chipSource, chipScale) = rememberPressScale(0.94f)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) palette.activeChipBg else palette.glassSurface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) palette.goldAccent else palette.goldBorder
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = chipScale
                                scaleY = chipScale
                            }
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = chipSource,
                                indication = LocalIndication.current
                            ) { onSelectDhikr(dhikr.id) }
                            .testTag("dhikr_chip_${dhikr.id}")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dhikr.title(language),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) palette.activeChipText else palette.textPrimary
                            )
                        }
                    }
                }
            }

            // Main Tasbih Glass Card
            LuxuryGlassCard(
                highlighted = true,
                cornerRadius = 28.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentDhikr.arabicText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.goldPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (language == AppLanguage.ENGLISH) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentDhikr.englishText,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentDhikr.note(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Goal Selector Pills (33 / 100 / Unlimited)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TasbihGoal.entries.forEach { option ->
                            val selected = option == goal
                            val (goalSource, goalScale) = rememberPressScale(0.94f)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) palette.activeChipBg else palette.badgeBg,
                                border = BorderStroke(1.dp, palette.goldBorder),
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = goalScale
                                        scaleY = goalScale
                                    }
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = goalSource,
                                        indication = LocalIndication.current
                                    ) { onSelectGoal(option) }
                                    .testTag("tasbih_goal_${option.name.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.label(language),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) palette.activeChipText else palette.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Interactive Circular Progress Ring + Tap Button
                    Box(
                        modifier = Modifier
                            .size(224.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .clickable {
                                tapPulse = true
                                onIncrement()
                            }
                            .testTag("tasbih_tap_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeW = 14.dp.toPx()
                            drawArc(
                                color = palette.badgeBg,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        palette.goldPrimary,
                                        palette.goldAccent,
                                        palette.goldPrimary
                                    )
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(174.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            palette.glassElevated,
                                            palette.badgeBg
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                                    color = palette.goldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("tasbih_count_text")
                                )
                                Text(
                                    text = if (target != null) s.outOfTarget(target) else s.openTargetLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.TouchApp,
                                    contentDescription = currentDhikr.title(language),
                                    tint = palette.goldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bottom Total & Reset Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = palette.badgeBg,
                            border = BorderStroke(0.8.dp, palette.goldBorder)
                        ) {
                            Text(
                                text = s.totalSessionsPrefix(totalCount),
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.textPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }

                        val (resetSource, resetScale) = rememberPressScale(0.94f)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = palette.glassSurface,
                            border = BorderStroke(1.dp, palette.goldBorder),
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = resetScale
                                    scaleY = resetScale
                                }
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(
                                    interactionSource = resetSource,
                                    indication = LocalIndication.current,
                                    onClick = onReset
                                )
                                .testTag("tasbih_reset_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RestartAlt,
                                    contentDescription = s.resetLabel,
                                    tint = palette.goldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = s.resetLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
