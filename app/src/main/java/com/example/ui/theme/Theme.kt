package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.AppLanguage
import com.example.data.model.ArabicFontStyle
import com.example.data.model.MeeqatThemeMode

@Immutable
data class MeeqatPalette(
    val isDark: Boolean,
    val isComfortableReading: Boolean = false,
    val backgroundTop: Color,
    val backgroundMid: Color,
    val backgroundBottom: Color,
    val glassSurface: Color,
    val glassElevated: Color,
    val goldPrimary: Color,
    val goldAccent: Color,
    val goldBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val patternTint: Color,
    val badgeBg: Color,
    val activeChipBg: Color,
    val activeChipText: Color,
    val specularHighlight: Color,
    val statusSuccess: Color = StatusEmeraldBright,
    val statusError: Color = StatusCrimsonBright
)

private val PearlWhitePalette = MeeqatPalette(
    isDark = false,
    isComfortableReading = false,
    backgroundTop = PearlBackgroundTop,
    backgroundMid = PearlBackgroundMid,
    backgroundBottom = PearlBackgroundBottom,
    glassSurface = PearlGlassSurface,
    glassElevated = PearlGlassElevated,
    goldPrimary = PearlGoldPrimary,
    goldAccent = PearlGoldAccent,
    goldBorder = PearlGoldBorder,
    textPrimary = PearlTextPrimary,
    textSecondary = PearlTextSecondary,
    patternTint = PearlPatternTint,
    badgeBg = Color(0x26D4AF37),
    activeChipBg = Color(0xFFB5891C),
    activeChipText = Color(0xFFFFFFFF),
    specularHighlight = Color(0xAAFFFFFF)
)

private val RoyalBlackPalette = MeeqatPalette(
    isDark = true,
    isComfortableReading = false,
    backgroundTop = ObsidianBackgroundTop,
    backgroundMid = ObsidianBackgroundMid,
    backgroundBottom = ObsidianBackgroundBottom,
    glassSurface = ObsidianGlassSurface,
    glassElevated = ObsidianGlassElevated,
    goldPrimary = ObsidianGoldPrimary,
    goldAccent = ObsidianGoldAccent,
    goldBorder = ObsidianGoldBorder,
    textPrimary = ObsidianTextPrimary,
    textSecondary = ObsidianTextSecondary,
    patternTint = ObsidianPatternTint,
    badgeBg = Color(0x2AE8C55A),
    activeChipBg = Color(0xFFE8C55A),
    activeChipText = Color(0xFF10131C),
    specularHighlight = Color(0x26FFFFFF)
)

private val PearlComfortPalette = MeeqatPalette(
    isDark = false,
    isComfortableReading = true,
    backgroundTop = ComfortLightBackgroundTop,
    backgroundMid = ComfortLightBackgroundMid,
    backgroundBottom = ComfortLightBackgroundBottom,
    glassSurface = ComfortLightGlassSurface,
    glassElevated = ComfortLightGlassElevated,
    goldPrimary = ComfortLightGoldPrimary,
    goldAccent = ComfortLightGoldAccent,
    goldBorder = ComfortLightGoldBorder,
    textPrimary = ComfortLightTextPrimary,
    textSecondary = ComfortLightTextSecondary,
    patternTint = ComfortLightPatternTint,
    badgeBg = Color(0x1E8B6F36),
    activeChipBg = Color(0xFF8B6F36),
    activeChipText = Color(0xFFF3ECE0),
    specularHighlight = Color(0x30FFF8EA),
    statusSuccess = ComfortStatusEmerald,
    statusError = ComfortStatusCrimson
)

private val RoyalComfortPalette = MeeqatPalette(
    isDark = true,
    isComfortableReading = true,
    backgroundTop = ComfortDarkBackgroundTop,
    backgroundMid = ComfortDarkBackgroundMid,
    backgroundBottom = ComfortDarkBackgroundBottom,
    glassSurface = ComfortDarkGlassSurface,
    glassElevated = ComfortDarkGlassElevated,
    goldPrimary = ComfortDarkGoldPrimary,
    goldAccent = ComfortDarkGoldAccent,
    goldBorder = ComfortDarkGoldBorder,
    textPrimary = ComfortDarkTextPrimary,
    textSecondary = ComfortDarkTextSecondary,
    patternTint = ComfortDarkPatternTint,
    badgeBg = Color(0x22B59B5E),
    activeChipBg = Color(0xFFB59B5E),
    activeChipText = Color(0xFF171920),
    specularHighlight = Color(0x12FFFFFF),
    statusSuccess = ComfortStatusEmerald,
    statusError = ComfortStatusCrimson
)

val LocalMeeqatPalette = staticCompositionLocalOf { PearlWhitePalette }

private val PearlLightColorScheme = lightColorScheme(
    primary = PearlGoldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6E7BE),
    onPrimaryContainer = PearlTextPrimary,
    secondary = Color(0xFF8C6B1D),
    onSecondary = Color.White,
    background = PearlBackgroundTop,
    onBackground = PearlTextPrimary,
    surface = PearlGlassElevated,
    onSurface = PearlTextPrimary,
    surfaceVariant = Color(0xFFF0E6D2),
    onSurfaceVariant = PearlTextSecondary,
    outline = PearlGoldBorder
)

private val RoyalDarkColorScheme = darkColorScheme(
    primary = ObsidianGoldPrimary,
    onPrimary = Color(0xFF10131C),
    primaryContainer = Color(0xFF332A12),
    onPrimaryContainer = ObsidianGoldAccent,
    secondary = ObsidianGoldAccent,
    onSecondary = Color(0xFF10131C),
    background = ObsidianBackgroundTop,
    onBackground = ObsidianTextPrimary,
    surface = ObsidianGlassElevated,
    onSurface = ObsidianTextPrimary,
    surfaceVariant = Color(0xFF1D2231),
    onSurfaceVariant = ObsidianTextSecondary,
    outline = ObsidianGoldBorder
)

private val PearlComfortColorScheme = lightColorScheme(
    primary = ComfortLightGoldPrimary,
    onPrimary = Color(0xFFF3ECE0),
    primaryContainer = Color(0xFFE3D5BC),
    onPrimaryContainer = ComfortLightTextPrimary,
    secondary = ComfortLightGoldAccent,
    onSecondary = Color(0xFFF3ECE0),
    background = ComfortLightBackgroundTop,
    onBackground = ComfortLightTextPrimary,
    surface = ComfortLightGlassElevated,
    onSurface = ComfortLightTextPrimary,
    surfaceVariant = Color(0xFFE2D6C1),
    onSurfaceVariant = ComfortLightTextSecondary,
    outline = ComfortLightGoldBorder
)

private val RoyalComfortColorScheme = darkColorScheme(
    primary = ComfortDarkGoldPrimary,
    onPrimary = ComfortDarkBackgroundTop,
    primaryContainer = Color(0xFF2D2A22),
    onPrimaryContainer = ComfortDarkGoldAccent,
    secondary = ComfortDarkGoldAccent,
    onSecondary = ComfortDarkBackgroundTop,
    background = ComfortDarkBackgroundTop,
    onBackground = ComfortDarkTextPrimary,
    surface = ComfortDarkGlassElevated,
    onSurface = ComfortDarkTextPrimary,
    surfaceVariant = Color(0xFF232731),
    onSurfaceVariant = ComfortDarkTextSecondary,
    outline = ComfortDarkGoldBorder
)

@Composable
fun MeeqatTheme(
    themeMode: MeeqatThemeMode = MeeqatThemeMode.PEARL_WHITE,
    fontStyle: ArabicFontStyle = ArabicFontStyle.RUQAA,
    language: AppLanguage = AppLanguage.ARABIC,
    isComfortableReading: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = when {
        isComfortableReading && themeMode == MeeqatThemeMode.PEARL_WHITE -> PearlComfortPalette
        isComfortableReading && themeMode == MeeqatThemeMode.ROYAL_BLACK -> RoyalComfortPalette
        themeMode == MeeqatThemeMode.PEARL_WHITE -> PearlWhitePalette
        else -> RoyalBlackPalette
    }
    val colorScheme = when {
        isComfortableReading && themeMode == MeeqatThemeMode.PEARL_WHITE -> PearlComfortColorScheme
        isComfortableReading && themeMode == MeeqatThemeMode.ROYAL_BLACK -> RoyalComfortColorScheme
        themeMode == MeeqatThemeMode.PEARL_WHITE -> PearlLightColorScheme
        else -> RoyalDarkColorScheme
    }
    val typography = createMeeqatTypography(
        fontStyle = fontStyle,
        language = language,
        isComfortableReading = isComfortableReading
    )
    val layoutDir = if (language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr
    val strings = if (language == AppLanguage.ARABIC) ArabicStrings else EnglishStrings

    CompositionLocalProvider(
        LocalMeeqatPalette provides palette,
        LocalMeeqatStrings provides strings,
        LocalLayoutDirection provides layoutDir
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
