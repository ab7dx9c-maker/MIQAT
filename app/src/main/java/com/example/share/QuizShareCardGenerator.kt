package com.example.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.IslamicQuizQuestion
import com.example.data.model.MeeqatThemeMode
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizTopic
import com.example.ui.theme.ArabicStrings
import com.example.ui.theme.EnglishStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

data class GeneratedShareCard(
    val bitmap: Bitmap,
    val imageUri: Uri,
    val shareText: String,
    val chooserTitle: String
)

object QuizShareCardGenerator {

    private const val CARD_WIDTH = 1080
    private const val CARD_HEIGHT = 1350

    suspend fun createQuestionShareCard(
        context: Context,
        language: AppLanguage,
        themeMode: MeeqatThemeMode,
        difficulty: QuizDifficulty,
        topic: QuizTopic,
        question: IslamicQuizQuestion,
        selectedOption: Int?,
        roundCorrectCount: Int,
        roundTotalCount: Int
    ): GeneratedShareCard = withContext(Dispatchers.Default) {
        val bitmap = renderCardBitmap(
            context = context,
            language = language,
            themeMode = themeMode,
            difficulty = difficulty,
            topic = topic,
            question = question,
            selectedOption = selectedOption,
            roundCorrectCount = roundCorrectCount,
            roundTotalCount = roundTotalCount,
            isRoundSummary = false
        )
        val uri = saveBitmapAndGetUri(context, bitmap)
        val s = if (language == AppLanguage.ENGLISH) EnglishStrings else ArabicStrings
        GeneratedShareCard(
            bitmap = bitmap,
            imageUri = uri,
            shareText = s.quizShareQuestionCaption,
            chooserTitle = s.quizShareChooserTitle
        )
    }

    suspend fun createRoundSummaryShareCard(
        context: Context,
        language: AppLanguage,
        themeMode: MeeqatThemeMode,
        difficulty: QuizDifficulty,
        topic: QuizTopic,
        featuredQuestion: IslamicQuizQuestion,
        selectedOptionForFeatured: Int?,
        correctCount: Int,
        totalCount: Int
    ): GeneratedShareCard = withContext(Dispatchers.Default) {
        val bitmap = renderCardBitmap(
            context = context,
            language = language,
            themeMode = themeMode,
            difficulty = difficulty,
            topic = topic,
            question = featuredQuestion,
            selectedOption = selectedOptionForFeatured,
            roundCorrectCount = correctCount,
            roundTotalCount = totalCount,
            isRoundSummary = true
        )
        val uri = saveBitmapAndGetUri(context, bitmap)
        val s = if (language == AppLanguage.ENGLISH) EnglishStrings else ArabicStrings
        val pct = if (totalCount > 0) (correctCount * 100) / totalCount else 0
        GeneratedShareCard(
            bitmap = bitmap,
            imageUri = uri,
            shareText = s.quizShareCaption(correctCount, totalCount, pct),
            chooserTitle = s.quizShareChooserTitle
        )
    }

    fun launchShareIntent(context: Context, card: GeneratedShareCard) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, card.imageUri)
            putExtra(Intent.EXTRA_TEXT, card.shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, card.chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri {
        val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
        // Keep cache tidy by removing cards older than 1 hour
        val cutoff = System.currentTimeMillis() - 3_600_000L
        dir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                file.delete()
            }
        }
        val outFile = File(dir, "meeqat_quiz_${System.currentTimeMillis()}.png")
        FileOutputStream(outFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outFile
        )
    }

    private fun renderCardBitmap(
        context: Context,
        language: AppLanguage,
        themeMode: MeeqatThemeMode,
        difficulty: QuizDifficulty,
        topic: QuizTopic,
        question: IslamicQuizQuestion,
        selectedOption: Int?,
        roundCorrectCount: Int,
        roundTotalCount: Int,
        isRoundSummary: Boolean
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isDark = themeMode == MeeqatThemeMode.ROYAL_BLACK
        val isRtl = language == AppLanguage.ARABIC
        val s = if (language == AppLanguage.ENGLISH) EnglishStrings else ArabicStrings

        val displayFont = loadTypeface(context, R.font.cairo)
        val bodyFont = loadTypeface(context, R.font.noto_naskh_arabic)

        // Palette tokens tuned for rich social-media presentation
        val bgTop = if (isDark) Color.parseColor("#07090F") else Color.parseColor("#FDFBF6")
        val bgMid = if (isDark) Color.parseColor("#111624") else Color.parseColor("#F5EDDE")
        val bgBottom = if (isDark) Color.parseColor("#181E30") else Color.parseColor("#EBDEC5")
        val cardFill = if (isDark) Color.parseColor("#D9181E2E") else Color.parseColor("#EEFFFDF8")
        val subCardFill = if (isDark) Color.parseColor("#E620273B") else Color.parseColor("#FFF8EC")
        val goldPrimary = if (isDark) Color.parseColor("#E8C55A") else Color.parseColor("#B5891C")
        val goldAccent = if (isDark) Color.parseColor("#F6DB83") else Color.parseColor("#D4AF37")
        val goldBorder = if (isDark) Color.parseColor("#78E8C55A") else Color.parseColor("#88D4AF37")
        val textPrimary = if (isDark) Color.parseColor("#FAF6EB") else Color.parseColor("#211A0E")
        val textSecondary = if (isDark) Color.parseColor("#C7BCA0") else Color.parseColor("#61533C")
        val patternColor = if (isDark) Color.parseColor("#14E8C55A") else Color.parseColor("#12B8891B")
        val emerald = if (isDark) Color.parseColor("#22C55E") else Color.parseColor("#15803D")
        val emeraldBg = if (isDark) Color.parseColor("#2622C55E") else Color.parseColor("#1F16A34A")
        val crimson = if (isDark) Color.parseColor("#EF4444") else Color.parseColor("#DC2626")
        val crimsonBg = if (isDark) Color.parseColor("#26EF4444") else Color.parseColor("#1CDC2626")
        val badgeFill = if (isDark) Color.parseColor("#2EE8C55A") else Color.parseColor("#26D4AF37")

        // 1. Background gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, CARD_HEIGHT.toFloat(),
                intArrayOf(bgTop, bgMid, bgBottom),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), bgPaint)

        // 2. Radial top halo
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                CARD_WIDTH * 0.5f, CARD_HEIGHT * 0.14f, CARD_WIDTH * 0.72f,
                intArrayOf(
                    Color.argb(if (isDark) 44 else 38, Color.red(goldAccent), Color.green(goldAccent), Color.blue(goldAccent)),
                    Color.TRANSPARENT
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(CARD_WIDTH * 0.5f, CARD_HEIGHT * 0.14f, CARD_WIDTH * 0.72f, glowPaint)

        // 3. Subtle Rub el Hizb Islamic geometric background pattern
        drawGeometricPattern(canvas, patternColor)

        // 4. Outer double gold border frame
        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = goldBorder
        }
        canvas.drawRoundRect(RectF(28f, 28f, CARD_WIDTH - 28f, CARD_HEIGHT - 28f), 42f, 42f, outerPaint)
        val innerFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = Color.argb(90, Color.red(goldAccent), Color.green(goldAccent), Color.blue(goldAccent))
        }
        canvas.drawRoundRect(RectF(42f, 42f, CARD_WIDTH - 42f, CARD_HEIGHT - 42f), 34f, 34f, innerFramePaint)

        // 5. Header Brand + Crescent Emblem
        drawCrescentStarEmblem(canvas, CARD_WIDTH * 0.5f, 108f, 36f, goldPrimary)

        var cursorY = 162f
        val brandTitle = "${s.appTitle} • ${s.quizHeaderTitle}"
        cursorY = drawTextBlock(
            canvas = canvas,
            text = brandTitle,
            x = 76f,
            y = cursorY,
            width = CARD_WIDTH - 152,
            textSize = 40f,
            color = goldPrimary,
            typeface = displayFont,
            bold = true,
            alignment = Layout.Alignment.ALIGN_CENTER,
            isRtl = isRtl,
            maxLines = 1
        )
        cursorY += 14f

        // Metadata pills: Difficulty + Topic
        val metaText = "${difficulty.title(language)}  •  ${topic.title(language)}"
        val metaPillWidth = 660f
        val metaPillRect = RectF(
            (CARD_WIDTH - metaPillWidth) / 2f,
            cursorY,
            (CARD_WIDTH + metaPillWidth) / 2f,
            cursorY + 56f
        )
        drawRoundedBox(canvas, metaPillRect, 28f, badgeFill, goldBorder, 1.5f)
        drawTextBlock(
            canvas = canvas,
            text = metaText,
            x = metaPillRect.left + 16f,
            y = cursorY + 11f,
            width = metaPillRect.width().toInt() - 32,
            textSize = 27f,
            color = textPrimary,
            typeface = displayFont,
            bold = true,
            alignment = Layout.Alignment.ALIGN_CENTER,
            isRtl = isRtl,
            maxLines = 1
        )
        cursorY += 78f

        // 6. Score / Result Banner
        val pct = if (roundTotalCount > 0) (roundCorrectCount * 100) / roundTotalCount else 0
        val isSingleAnswered = selectedOption != null
        val isSingleCorrect = isSingleAnswered && selectedOption == question.correctIndex

        if (isRoundSummary || roundTotalCount > 0) {
            val bannerRect = RectF(76f, cursorY, CARD_WIDTH - 76f, cursorY + if (isRoundSummary) 186f else 112f)
            val bannerFill = when {
                !isRoundSummary && isSingleAnswered && isSingleCorrect -> emeraldBg
                !isRoundSummary && isSingleAnswered && !isSingleCorrect -> crimsonBg
                else -> subCardFill
            }
            val bannerBorder = when {
                !isRoundSummary && isSingleAnswered && isSingleCorrect -> emerald
                !isRoundSummary && isSingleAnswered && !isSingleCorrect -> crimson
                else -> goldAccent
            }
            drawRoundedBox(canvas, bannerRect, 30f, bannerFill, bannerBorder, 2.5f)

            if (isRoundSummary) {
                val scoreHeadline = "${s.quizShareScoreBadge(roundCorrectCount, roundTotalCount)}  •  $pct%"
                drawTextBlock(
                    canvas = canvas,
                    text = scoreHeadline,
                    x = bannerRect.left + 28f,
                    y = cursorY + 24f,
                    width = bannerRect.width().toInt() - 56,
                    textSize = 46f,
                    color = if (pct >= 60) goldPrimary else textPrimary,
                    typeface = displayFont,
                    bold = true,
                    alignment = Layout.Alignment.ALIGN_CENTER,
                    isRtl = isRtl,
                    maxLines = 1
                )
                val feedbackText = when {
                    pct >= 80 -> s.quizFeedbackHigh
                    pct >= 50 -> s.quizFeedbackMid
                    else -> s.quizFeedbackLow
                }
                drawTextBlock(
                    canvas = canvas,
                    text = feedbackText,
                    x = bannerRect.left + 36f,
                    y = cursorY + 96f,
                    width = bannerRect.width().toInt() - 72,
                    textSize = 27f,
                    color = textSecondary,
                    typeface = bodyFont,
                    bold = false,
                    alignment = Layout.Alignment.ALIGN_CENTER,
                    isRtl = isRtl,
                    maxLines = 2
                )
                cursorY += 210f
            } else {
                val statusLabel = when {
                    isSingleAnswered && isSingleCorrect -> "✓  ${s.quizCorrectBadge}"
                    isSingleAnswered && !isSingleCorrect -> "✗  ${s.quizWrongBadge}"
                    else -> s.quizHeaderTitle
                }
                val statusColor = when {
                    isSingleAnswered && isSingleCorrect -> emerald
                    isSingleAnswered && !isSingleCorrect -> crimson
                    else -> goldPrimary
                }
                val combined = if (roundTotalCount > 0 && roundCorrectCount >= 0) {
                    "$statusLabel   |   ${s.quizShareScoreBadge(roundCorrectCount, roundTotalCount)}"
                } else {
                    statusLabel
                }
                drawTextBlock(
                    canvas = canvas,
                    text = combined,
                    x = bannerRect.left + 24f,
                    y = cursorY + 32f,
                    width = bannerRect.width().toInt() - 48,
                    textSize = 34f,
                    color = statusColor,
                    typeface = displayFont,
                    bold = true,
                    alignment = Layout.Alignment.ALIGN_CENTER,
                    isRtl = isRtl,
                    maxLines = 1
                )
                cursorY += 136f
            }
        }

        // 7. Central Question & Answer Card
        val cardBottomLimit = CARD_HEIGHT - 126f
        val questionCardRect = RectF(76f, cursorY, CARD_WIDTH - 76f, cardBottomLimit)
        drawRoundedBox(canvas, questionCardRect, 34f, cardFill, goldBorder, 2f)

        var innerY = cursorY + 32f
        val innerLeft = questionCardRect.left + 38f
        val innerWidth = questionCardRect.width().toInt() - 76

        if (isRoundSummary) {
            innerY = drawTextBlock(
                canvas = canvas,
                text = s.quizShareFeaturedLabel,
                x = innerLeft,
                y = innerY,
                width = innerWidth,
                textSize = 26f,
                color = goldPrimary,
                typeface = displayFont,
                bold = true,
                alignment = Layout.Alignment.ALIGN_NORMAL,
                isRtl = isRtl,
                maxLines = 1
            )
            innerY += 10f
        } else if (question.topicLabel.isNotBlank()) {
            innerY = drawTextBlock(
                canvas = canvas,
                text = question.topicLabel,
                x = innerLeft,
                y = innerY,
                width = innerWidth,
                textSize = 25f,
                color = goldPrimary,
                typeface = displayFont,
                bold = true,
                alignment = Layout.Alignment.ALIGN_NORMAL,
                isRtl = isRtl,
                maxLines = 1
            )
            innerY += 10f
        }

        // Question text
        innerY = drawTextBlock(
            canvas = canvas,
            text = question.question,
            x = innerLeft,
            y = innerY,
            width = innerWidth,
            textSize = if (question.question.length > 110) 34f else 39f,
            color = textPrimary,
            typeface = bodyFont,
            bold = true,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            isRtl = isRtl,
            maxLines = 4
        )
        innerY += 24f

        // Correct Answer Box
        val correctText = question.options.getOrElse(question.correctIndex) { "" }
        val correctDisplay = "${s.quizShareCorrectAnswerLabel}  $correctText"
        val correctLayout = buildStaticLayout(
            text = correctDisplay,
            width = innerWidth - 44,
            textSize = 30f,
            color = textPrimary,
            typeface = bodyFont,
            bold = true,
            alignment = Layout.Alignment.ALIGN_NORMAL,
            isRtl = isRtl,
            maxLines = 3
        )
        val correctBoxHeight = correctLayout.height + 34f
        if (innerY + correctBoxHeight < cardBottomLimit - 24f) {
            val correctRect = RectF(innerLeft, innerY, innerLeft + innerWidth, innerY + correctBoxHeight)
            drawRoundedBox(canvas, correctRect, 22f, emeraldBg, emerald, 2f)
            canvas.save()
            canvas.translate(innerLeft + 22f, innerY + 17f)
            correctLayout.draw(canvas)
            canvas.restore()
            innerY += correctBoxHeight + 16f
        }

        // If user answered wrongly on this question, show user's chosen option clearly
        if (selectedOption != null && selectedOption != question.correctIndex && selectedOption in question.options.indices) {
            val userChoice = "${s.quizShareYourAnswerLabel}  ${question.options[selectedOption]}"
            val wrongLayout = buildStaticLayout(
                text = userChoice,
                width = innerWidth - 44,
                textSize = 28f,
                color = textPrimary,
                typeface = bodyFont,
                bold = false,
                alignment = Layout.Alignment.ALIGN_NORMAL,
                isRtl = isRtl,
                maxLines = 2
            )
            val wrongBoxHeight = wrongLayout.height + 30f
            if (innerY + wrongBoxHeight < cardBottomLimit - 24f) {
                val wrongRect = RectF(innerLeft, innerY, innerLeft + innerWidth, innerY + wrongBoxHeight)
                drawRoundedBox(canvas, wrongRect, 22f, crimsonBg, crimson, 1.8f)
                canvas.save()
                canvas.translate(innerLeft + 22f, innerY + 15f)
                wrongLayout.draw(canvas)
                canvas.restore()
                innerY += wrongBoxHeight + 16f
            }
        }

        // Authentic Explanation Box (if space allows)
        if (question.explanation.isNotBlank() && innerY + 110f < cardBottomLimit - 20f) {
            val expText = "${s.quizExplanationTitle} ${question.explanation}"
            val remainingSpace = (cardBottomLimit - 26f - innerY - 32f).toInt()
            val maxExpLines = (remainingSpace / 38).coerceIn(2, 5)
            val expLayout = buildStaticLayout(
                text = expText,
                width = innerWidth - 40,
                textSize = 25f,
                color = textSecondary,
                typeface = bodyFont,
                bold = false,
                alignment = Layout.Alignment.ALIGN_NORMAL,
                isRtl = isRtl,
                maxLines = maxExpLines
            )
            val expBoxHeight = expLayout.height + 28f
            if (innerY + expBoxHeight <= cardBottomLimit - 18f) {
                val expRect = RectF(innerLeft, innerY, innerLeft + innerWidth, innerY + expBoxHeight)
                drawRoundedBox(canvas, expRect, 20f, badgeFill, goldBorder, 1.2f)
                canvas.save()
                canvas.translate(innerLeft + 20f, innerY + 14f)
                expLayout.draw(canvas)
                canvas.restore()
            }
        }

        // 8. Footer Brand Signature
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldBorder
            strokeWidth = 1.5f
        }
        canvas.drawLine(180f, CARD_HEIGHT - 96f, CARD_WIDTH - 180f, CARD_HEIGHT - 96f, dividerPaint)
        drawTextBlock(
            canvas = canvas,
            text = s.quizShareFooterBrand,
            x = 76f,
            y = CARD_HEIGHT - 80f,
            width = CARD_WIDTH - 152,
            textSize = 25f,
            color = goldPrimary,
            typeface = displayFont,
            bold = true,
            alignment = Layout.Alignment.ALIGN_CENTER,
            isRtl = isRtl,
            maxLines = 1
        )

        return bitmap
    }

    private fun loadTypeface(context: Context, fontResId: Int): Typeface {
        return try {
            ResourcesCompat.getFont(context, fontResId) ?: Typeface.DEFAULT
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
    }

    private fun drawRoundedBox(
        canvas: Canvas,
        rect: RectF,
        radius: Float,
        fillColor: Int,
        borderColor: Int,
        borderWidth: Float
    ) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
        }
        canvas.drawRoundRect(rect, radius, radius, fillPaint)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
        canvas.drawRoundRect(rect, radius, radius, strokePaint)
    }

    private fun buildStaticLayout(
        text: String,
        width: Int,
        textSize: Float,
        color: Int,
        typeface: Typeface,
        bold: Boolean,
        alignment: Layout.Alignment,
        isRtl: Boolean,
        maxLines: Int
    ): StaticLayout {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.color = color
            this.typeface = if (bold) Typeface.create(typeface, Typeface.BOLD) else typeface
        }
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
            .setAlignment(alignment)
            .setTextDirection(
                if (isRtl) TextDirectionHeuristics.RTL else TextDirectionHeuristics.LTR
            )
            .setLineSpacing(6f, 1.12f)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
    }

    private fun drawTextBlock(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        textSize: Float,
        color: Int,
        typeface: Typeface,
        bold: Boolean,
        alignment: Layout.Alignment,
        isRtl: Boolean,
        maxLines: Int
    ): Float {
        val layout = buildStaticLayout(
            text = text,
            width = width,
            textSize = textSize,
            color = color,
            typeface = typeface,
            bold = bold,
            alignment = alignment,
            isRtl = isRtl,
            maxLines = maxLines
        )
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height
    }

    private fun drawGeometricPattern(canvas: Canvas, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            this.color = color
        }
        val step = 190f
        val radius = 58f
        val cols = (CARD_WIDTH / step).toInt() + 2
        val rows = (CARD_HEIGHT / step).toInt() + 2
        val half = radius * 0.72f
        val rectPath = Path()

        for (r in 0..rows) {
            for (c in 0..cols) {
                val cx = c * step + if (r % 2 == 1) step * 0.5f else 0f
                val cy = r * step
                rectPath.reset()
                rectPath.moveTo(cx - half, cy - half)
                rectPath.lineTo(cx + half, cy - half)
                rectPath.lineTo(cx + half, cy + half)
                rectPath.lineTo(cx - half, cy + half)
                rectPath.close()
                canvas.drawPath(rectPath, paint)
                canvas.save()
                canvas.rotate(45f, cx, cy)
                canvas.drawPath(rectPath, paint)
                canvas.restore()
                canvas.drawCircle(cx, cy, radius * 0.22f, paint)
            }
        }
    }

    private fun drawCrescentStarEmblem(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        tintColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = tintColor
        }
        val outer = Path().apply {
            addOval(RectF(cx - radius, cy - radius, cx + radius, cy + radius), Path.Direction.CW)
        }
        val ox = cx - radius * 0.36f
        val oy = cy - radius * 0.14f
        val rInner = radius * 0.80f
        val cutout = Path().apply {
            addOval(RectF(ox - rInner, oy - rInner, ox + rInner, oy + rInner), Path.Direction.CW)
        }
        val crescent = Path().apply {
            op(outer, cutout, Path.Op.DIFFERENCE)
        }
        canvas.drawPath(crescent, paint)

        val sx = cx - radius * 0.38f
        val sy = cy - radius * 0.36f
        val sr = radius * 0.30f
        val star = Path()
        for (i in 0 until 8) {
            val angle = i * Math.PI / 4.0
            val rad = if (i % 2 == 0) sr else sr * 0.45f
            val px = sx + (rad * cos(angle)).toFloat()
            val py = sy + (rad * sin(angle)).toFloat()
            if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
        }
        star.close()
        canvas.drawPath(star, paint)
    }
}
