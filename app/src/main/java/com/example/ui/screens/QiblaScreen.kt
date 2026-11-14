package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.theme.LocalMeeqatPalette
import com.example.ui.theme.LocalMeeqatStrings
import com.example.ui.theme.StatusEmeraldBright
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(
    qiblaBearing: Float,
    deviceAzimuth: Float?,
    hasCompassSensor: Boolean,
    manualOffset: Float,
    cityName: String,
    onManualOffsetChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val effectiveAzimuth = if (deviceAzimuth != null) {
        (deviceAzimuth + manualOffset) % 360f
    } else {
        manualOffset
    }

    val needleAngle = ((qiblaBearing - effectiveAzimuth) + 360f) % 360f
    val diffFromTop = minOf(needleAngle, 360f - needleAngle)
    val isAligned = diffFromTop <= 5f

    val animatedNeedleAngle by animateFloatAsState(
        targetValue = needleAngle,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "qibla_needle_anim"
    )
    val animatedDialAngle by animateFloatAsState(
        targetValue = -effectiveAzimuth,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "qibla_dial_anim"
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
            LuxuryGlassCard(
                highlighted = isAligned,
                cornerRadius = 26.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qibla_compass_card")
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.qiblaTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.goldPrimary
                            )
                            Text(
                                text = s.qiblaSubtitle(cityName),
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.badgeBg,
                            border = BorderStroke(1.dp, palette.goldBorder)
                        ) {
                            Text(
                                text = "${qiblaBearing.roundToInt()}°",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.goldPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Alignment Status Pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isAligned) StatusEmeraldBright.copy(alpha = 0.18f) else palette.badgeBg,
                        border = BorderStroke(
                            1.dp,
                            if (isAligned) StatusEmeraldBright else palette.goldBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAligned) Icons.Filled.CheckCircle else Icons.Filled.Explore,
                                contentDescription = null,
                                tint = if (isAligned) StatusEmeraldBright else palette.goldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAligned) s.qiblaAligned else s.qiblaTurnBy(abs(diffFromTop).roundToInt()),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isAligned) StatusEmeraldBright else palette.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Luxury Compass Dial Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val radius = size.minDimension / 2f - 10.dp.toPx()

                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        palette.goldAccent,
                                        palette.goldPrimary.copy(alpha = 0.45f),
                                        palette.goldAccent,
                                        palette.goldPrimary.copy(alpha = 0.45f),
                                        palette.goldAccent
                                    )
                                ),
                                radius = radius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 4.dp.toPx())
                            )

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        palette.goldAccent.copy(alpha = if (palette.isDark) 0.16f else 0.12f),
                                        palette.glassSurface
                                    ),
                                    center = Offset(cx, cy),
                                    radius = radius
                                ),
                                radius = radius - 4.dp.toPx(),
                                center = Offset(cx, cy)
                            )

                            rotate(degrees = animatedDialAngle, pivot = Offset(cx, cy)) {
                                for (deg in 0 until 360 step 10) {
                                    val rad = Math.toRadians(deg.toDouble() - 90.0)
                                    val isMajor = deg % 30 == 0
                                    val tickLen = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
                                    val startR = radius - 8.dp.toPx() - tickLen
                                    val endR = radius - 8.dp.toPx()
                                    drawLine(
                                        color = if (deg == 0) StatusEmeraldBright else palette.goldPrimary.copy(
                                            alpha = if (isMajor) 0.85f else 0.35f
                                        ),
                                        start = Offset(
                                            cx + (startR * cos(rad)).toFloat(),
                                            cy + (startR * sin(rad)).toFloat()
                                        ),
                                        end = Offset(
                                            cx + (endR * cos(rad)).toFloat(),
                                            cy + (endR * sin(rad)).toFloat()
                                        ),
                                        strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            drawLine(
                                color = if (isAligned) StatusEmeraldBright else palette.goldAccent,
                                start = Offset(cx, cy - radius - 8.dp.toPx()),
                                end = Offset(cx, cy - radius + 8.dp.toPx()),
                                strokeWidth = 3.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            rotate(degrees = animatedNeedleAngle, pivot = Offset(cx, cy)) {
                                val tipY = cy - radius * 0.72f
                                val tailY = cy + radius * 0.42f
                                val halfW = 10.dp.toPx()

                                val northPath = Path().apply {
                                    moveTo(cx, tipY)
                                    lineTo(cx - halfW, cy)
                                    lineTo(cx + halfW, cy)
                                    close()
                                }
                                drawPath(
                                    path = northPath,
                                    color = if (isAligned) StatusEmeraldBright else palette.goldPrimary
                                )

                                val southPath = Path().apply {
                                    moveTo(cx, tailY)
                                    lineTo(cx - halfW * 0.75f, cy)
                                    lineTo(cx + halfW * 0.75f, cy)
                                    close()
                                }
                                drawPath(
                                    path = southPath,
                                    color = palette.textSecondary.copy(alpha = 0.38f)
                                )

                                val boxSize = 18.dp.toPx()
                                drawRect(
                                    color = Color(0xFF141414),
                                    topLeft = Offset(cx - boxSize / 2f, tipY - boxSize - 6.dp.toPx()),
                                    size = Size(boxSize, boxSize)
                                )
                                drawRect(
                                    color = palette.goldAccent,
                                    topLeft = Offset(cx - boxSize / 2f, tipY - boxSize - 2.dp.toPx()),
                                    size = Size(boxSize, 3.dp.toPx())
                                )
                            }

                            drawCircle(
                                color = palette.goldAccent,
                                radius = 9.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = palette.glassElevated,
                                radius = 4.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CompassMetricPill(s.qiblaBearingLabel, "${qiblaBearing.roundToInt()}°")
                        CompassMetricPill(s.deviceHeadingLabel, "${effectiveAzimuth.roundToInt()}°")
                    }
                }
            }

            // Manual Test Rotation Slider
            LuxuryGlassCard(
                cornerRadius = 20.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasCompassSensor && deviceAzimuth != null) {
                                s.compassCalibrateSensor
                            } else {
                                s.compassManualTest
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "${manualOffset.roundToInt()}°",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.goldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (deviceAzimuth != null) s.compassSensorActiveHint else s.compassManualHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                        textAlign = TextAlign.Start
                    )
                    Slider(
                        value = manualOffset,
                        onValueChange = onManualOffsetChange,
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = palette.goldPrimary,
                            activeTrackColor = palette.goldPrimary
                        ),
                        modifier = Modifier.testTag("qibla_manual_slider")
                    )
                }
            }
        }
    }
}

@Composable
private fun CompassMetricPill(label: String, value: String) {
    val palette = LocalMeeqatPalette.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = palette.badgeBg,
        border = BorderStroke(0.8.dp, palette.goldBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = palette.goldPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
