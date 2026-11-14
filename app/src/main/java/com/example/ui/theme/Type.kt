package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.ArabicFontStyle

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold)
)

val TajawalFontFamily = FontFamily(
    Font(R.font.tajawal, FontWeight.Normal),
    Font(R.font.tajawal, FontWeight.Medium),
    Font(R.font.tajawal, FontWeight.Bold)
)

val ArefRuqaaFontFamily = FontFamily(
    Font(R.font.aref_ruqaa, FontWeight.Normal),
    Font(R.font.aref_ruqaa, FontWeight.Medium),
    Font(R.font.aref_ruqaa, FontWeight.SemiBold),
    Font(R.font.aref_ruqaa, FontWeight.Bold)
)

val NotoNaskhFontFamily = FontFamily(
    Font(R.font.noto_naskh_arabic, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic, FontWeight.Medium),
    Font(R.font.noto_naskh_arabic, FontWeight.SemiBold),
    Font(R.font.noto_naskh_arabic, FontWeight.Bold)
)

fun createMeeqatTypography(
    fontStyle: ArabicFontStyle,
    language: AppLanguage = AppLanguage.ARABIC,
    isComfortableReading: Boolean = false
): Typography {
    if (isComfortableReading) {
        return Typography(
            displayLarge = TextStyle(
                fontFamily = NotoNaskhFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                lineHeight = 48.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = NotoNaskhFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                lineHeight = 36.sp
            ),
            titleLarge = TextStyle(
                fontFamily = NotoNaskhFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 33.sp
            ),
            titleMedium = TextStyle(
                fontFamily = NotoNaskhFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.5.sp,
                lineHeight = 29.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = NotoNaskhFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 32.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = NotoNaskhFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.5.sp,
                lineHeight = 26.sp
            ),
            labelLarge = TextStyle(
                fontFamily = NotoNaskhFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                lineHeight = 22.sp
            ),
            labelMedium = TextStyle(
                fontFamily = NotoNaskhFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 19.sp
            ),
            labelSmall = TextStyle(
                fontFamily = NotoNaskhFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        )
    }

    val useRuqaa = language == AppLanguage.ARABIC && fontStyle == ArabicFontStyle.RUQAA
    val displayFamily = if (useRuqaa) ArefRuqaaFontFamily else CairoFontFamily
    val bodyFamily = if (useRuqaa) ArefRuqaaFontFamily else TajawalFontFamily

    return Typography(
        displayLarge = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            lineHeight = 48.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = if (useRuqaa) 26.sp else 24.sp,
            lineHeight = 36.sp
        ),
        titleLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = if (useRuqaa) 22.sp else 20.sp,
            lineHeight = 32.sp
        ),
        titleMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (useRuqaa) 18.sp else 16.sp,
            lineHeight = 28.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (useRuqaa) 18.sp else 16.sp,
            lineHeight = if (useRuqaa) 32.sp else 26.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (useRuqaa) 15.sp else 14.sp,
            lineHeight = if (useRuqaa) 25.sp else 22.sp
        ),
        labelLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (useRuqaa) 15.sp else 14.sp,
            lineHeight = 22.sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = if (useRuqaa) 13.sp else 12.sp,
            lineHeight = 18.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = if (useRuqaa) 12.sp else 11.sp,
            lineHeight = 16.sp
        )
    )
}
