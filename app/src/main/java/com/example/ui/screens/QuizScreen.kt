package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppLanguage
import com.example.data.model.IslamicQuizQuestion
import com.example.data.model.MeeqatThemeMode
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizTopic
import com.example.data.model.QuizUiState
import com.example.share.GeneratedShareCard
import com.example.share.QuizShareCardGenerator
import com.example.ui.components.ComfortableReadingTogglePill
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.rememberPressScale
import com.example.ui.theme.LocalMeeqatPalette
import com.example.ui.theme.LocalMeeqatStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    language: AppLanguage,
    themeMode: MeeqatThemeMode,
    selectedDifficulty: QuizDifficulty,
    selectedTopic: QuizTopic,
    selectedCount: Int,
    quizUiState: QuizUiState,
    selectedAnswers: Map<String, Int>,
    comfortableReadingMode: Boolean,
    onToggleComfortableReading: () -> Unit,
    onSelectDifficulty: (QuizDifficulty) -> Unit,
    onSelectTopic: (QuizTopic) -> Unit,
    onSelectCount: (Int) -> Unit,
    onGenerateQuiz: () -> Unit,
    onSelectAnswer: (String, Int) -> Unit,
    onResetAnswers: () -> Unit,
    onAdvanceNextDifficulty: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isLoading = quizUiState is QuizUiState.Loading

    var isGeneratingShare by remember { mutableStateOf(false) }
    var previewShareCard by remember { mutableStateOf<GeneratedShareCard?>(null) }

    fun shareSingleQuestion(
        question: IslamicQuizQuestion,
        selectedOption: Int?,
        difficulty: QuizDifficulty,
        topic: QuizTopic,
        correctCount: Int,
        totalCount: Int
    ) {
        if (isGeneratingShare) return
        isGeneratingShare = true
        coroutineScope.launch {
            try {
                previewShareCard = QuizShareCardGenerator.createQuestionShareCard(
                    context = context,
                    language = language,
                    themeMode = themeMode,
                    difficulty = difficulty,
                    topic = topic,
                    question = question,
                    selectedOption = selectedOption,
                    roundCorrectCount = correctCount,
                    roundTotalCount = totalCount
                )
            } finally {
                isGeneratingShare = false
            }
        }
    }

    fun shareRoundSummary(
        questions: List<IslamicQuizQuestion>,
        difficulty: QuizDifficulty,
        topic: QuizTopic,
        correctCount: Int
    ) {
        if (isGeneratingShare || questions.isEmpty()) return
        val featured = questions.firstOrNull { selectedAnswers[it.id] == it.correctIndex }
            ?: questions.firstOrNull { selectedAnswers.containsKey(it.id) }
            ?: questions.first()
        isGeneratingShare = true
        coroutineScope.launch {
            try {
                previewShareCard = QuizShareCardGenerator.createRoundSummaryShareCard(
                    context = context,
                    language = language,
                    themeMode = themeMode,
                    difficulty = difficulty,
                    topic = topic,
                    featuredQuestion = featured,
                    selectedOptionForFeatured = selectedAnswers[featured.id],
                    correctCount = correctCount,
                    totalCount = questions.size
                )
            } finally {
                isGeneratingShare = false
            }
        }
    }

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
            // Quick Toggle for Comfortable Reading Mode (Low Contrast + Classic Naskh)
            item {
                ComfortableReadingTogglePill(
                    enabled = comfortableReadingMode,
                    onToggle = onToggleComfortableReading,
                    testTag = "quiz_comfort_reading_toggle"
                )
            }

            // Generator & Difficulty Config Card (Easy -> Medium -> Hard)
            item {
                QuizGeneratorConfigCard(
                    language = language,
                    selectedDifficulty = selectedDifficulty,
                    selectedTopic = selectedTopic,
                    selectedCount = selectedCount,
                    isLoading = isLoading,
                    hasLoadedQuestions = quizUiState is QuizUiState.Ready,
                    onSelectDifficulty = onSelectDifficulty,
                    onSelectTopic = onSelectTopic,
                    onSelectCount = onSelectCount,
                    onGenerateQuiz = onGenerateQuiz
                )
            }

            when (quizUiState) {
                QuizUiState.Idle -> {
                    item {
                        LuxuryGlassCard(cornerRadius = 22.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(palette.badgeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Quiz,
                                        contentDescription = s.quizHeaderTitle,
                                        tint = palette.goldPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = s.quizEmptyHint,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = palette.textSecondary
                                )
                            }
                        }
                    }
                }

                QuizUiState.Loading -> {
                    item {
                        val pulseTransition = rememberInfiniteTransition(label = "quiz_loading_pulse")
                        val pulseScale by pulseTransition.animateFloat(
                            initialValue = 0.92f,
                            targetValue = 1.08f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(750, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "quiz_loading_icon_scale"
                        )
                        LuxuryGlassCard(
                            highlighted = true,
                            cornerRadius = 22.dp,
                            modifier = Modifier.testTag("quiz_loading_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                                ) {
                                    CircularProgressIndicator(
                                        color = palette.goldPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = s.quizGeneratingMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = palette.textPrimary
                                )
                            }
                        }
                    }
                }

                is QuizUiState.Error -> {
                    item {
                        val (retrySource, retryScale) = rememberPressScale(0.95f)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = palette.statusError.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, palette.statusError),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_error_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = quizUiState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.statusError
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = palette.activeChipBg,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = retryScale
                                            scaleY = retryScale
                                        }
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(
                                            interactionSource = retrySource,
                                            indication = LocalIndication.current,
                                            onClick = onGenerateQuiz
                                        )
                                        .testTag("quiz_retry_error_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = s.quizRetryButton,
                                            tint = palette.activeChipText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = s.quizRetryButton,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = palette.activeChipText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is QuizUiState.Ready -> {
                    val questions = quizUiState.questions
                    val answeredCount = questions.count { selectedAnswers.containsKey(it.id) }
                    val correctCount = questions.count { q ->
                        selectedAnswers[q.id] == q.correctIndex
                    }

                    // Live Score Summary Banner
                    item {
                        QuizScoreBanner(
                            language = language,
                            difficulty = quizUiState.difficulty,
                            topic = quizUiState.topic,
                            correctCount = correctCount,
                            answeredCount = answeredCount,
                            totalCount = questions.size,
                            onResetAnswers = onResetAnswers,
                            onShareRound = {
                                shareRoundSummary(
                                    questions = questions,
                                    difficulty = quizUiState.difficulty,
                                    topic = quizUiState.topic,
                                    correctCount = correctCount
                                )
                            }
                        )
                    }

                    // Staggered Animated Question Cards
                    itemsIndexed(questions, key = { _, q -> q.id }) { idx, question ->
                        QuizQuestionCard(
                            index = idx + 1,
                            total = questions.size,
                            language = language,
                            question = question,
                            selectedOption = selectedAnswers[question.id],
                            onSelectOption = { optIndex -> onSelectAnswer(question.id, optIndex) },
                            onShareQuestion = {
                                shareSingleQuestion(
                                    question = question,
                                    selectedOption = selectedAnswers[question.id],
                                    difficulty = quizUiState.difficulty,
                                    topic = quizUiState.topic,
                                    correctCount = correctCount,
                                    totalCount = questions.size
                                )
                            }
                        )
                    }

                    // Completion Summary Card once all questions are answered
                    if (questions.isNotEmpty() && answeredCount == questions.size) {
                        item(key = "quiz_completion_${questions.first().id}") {
                            QuizCompletionCard(
                                language = language,
                                difficulty = quizUiState.difficulty,
                                correctCount = correctCount,
                                totalCount = questions.size,
                                onRetry = onResetAnswers,
                                onGenerateNew = onGenerateQuiz,
                                onAdvanceNextDifficulty = onAdvanceNextDifficulty,
                                onShareResult = {
                                    shareRoundSummary(
                                        questions = questions,
                                        difficulty = quizUiState.difficulty,
                                        topic = quizUiState.topic,
                                        correctCount = correctCount
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isGeneratingShare) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                LuxuryGlassCard(highlighted = true, cornerRadius = 20.dp) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = palette.goldPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.4.dp
                        )
                        Text(
                            text = s.quizSharePreparing,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary
                        )
                    }
                }
            }
        }

        previewShareCard?.let { card ->
            QuizSharePreviewDialog(
                card = card,
                onShareNow = {
                    QuizShareCardGenerator.launchShareIntent(context, card)
                },
                onDismiss = { previewShareCard = null }
            )
        }
    }
}

@Composable
private fun QuizGeneratorConfigCard(
    language: AppLanguage,
    selectedDifficulty: QuizDifficulty,
    selectedTopic: QuizTopic,
    selectedCount: Int,
    isLoading: Boolean,
    hasLoadedQuestions: Boolean,
    onSelectDifficulty: (QuizDifficulty) -> Unit,
    onSelectTopic: (QuizTopic) -> Unit,
    onSelectCount: (Int) -> Unit,
    onGenerateQuiz: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current

    LuxuryGlassCard(
        highlighted = true,
        cornerRadius = 24.dp,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag("quiz_config_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Quiz,
                    contentDescription = null,
                    tint = palette.goldPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.quizHeaderTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.goldPrimary
                    )
                    Text(
                        text = s.quizHeaderSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary
                    )
                }
            }

            // Difficulty Selector: Easy -> Medium -> Hard
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = s.quizDifficultyLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuizDifficulty.entries.forEach { diff ->
                        val selected = diff == selectedDifficulty
                        val (pressSource, pressScale) = rememberPressScale(0.94f)
                        val chipColor by animateColorAsState(
                            targetValue = if (selected) palette.activeChipBg else palette.badgeBg,
                            animationSpec = tween(220),
                            label = "diff_bg_${diff.id}"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.55f),
                            animationSpec = tween(220),
                            label = "diff_border_${diff.id}"
                        )
                        val badgeTint = when (diff) {
                            QuizDifficulty.EASY -> palette.statusSuccess
                            QuizDifficulty.MEDIUM -> palette.goldPrimary
                            QuizDifficulty.HARD -> palette.statusError
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = chipColor,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = borderColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    scaleX = pressScale
                                    scaleY = pressScale
                                }
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(
                                    interactionSource = pressSource,
                                    indication = LocalIndication.current,
                                    enabled = !isLoading
                                ) { onSelectDifficulty(diff) }
                                .testTag("quiz_difficulty_${diff.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) palette.activeChipText else badgeTint)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = diff.title(language),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) palette.activeChipText else palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                AnimatedContent(
                    targetState = selectedDifficulty,
                    transitionSpec = {
                        (fadeIn(tween(200)) + expandVertically(tween(200))) togetherWith
                            (fadeOut(tween(150)) + shrinkVertically(tween(150)))
                    },
                    label = "difficulty_subtitle_anim"
                ) { diff ->
                    Text(
                        text = diff.subtitle(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary
                    )
                }
            }

            // Topic Filter Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = s.quizTopicLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuizTopic.entries.forEach { topic ->
                        val selected = topic == selectedTopic
                        val (topicSource, topicScale) = rememberPressScale(0.94f)
                        val topicBg by animateColorAsState(
                            targetValue = if (selected) palette.activeChipBg else palette.glassSurface,
                            animationSpec = tween(200),
                            label = "topic_bg_${topic.id}"
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = topicBg,
                            border = BorderStroke(
                                1.dp,
                                if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = topicScale
                                    scaleY = topicScale
                                }
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = topicSource,
                                    indication = LocalIndication.current,
                                    enabled = !isLoading
                                ) { onSelectTopic(topic) }
                                .testTag("quiz_topic_${topic.id}")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = topic.title(language),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) palette.activeChipText else palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Question Count Pills + Generate CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = s.quizCountLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 5, 7).forEach { countOption ->
                        val selected = countOption == selectedCount
                        val (cntSource, cntScale) = rememberPressScale(0.93f)
                        val cntBg by animateColorAsState(
                            targetValue = if (selected) palette.activeChipBg else palette.badgeBg,
                            animationSpec = tween(200),
                            label = "cnt_bg_$countOption"
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = cntBg,
                            border = BorderStroke(
                                1.dp,
                                if (selected) palette.goldAccent else palette.goldBorder.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = cntScale
                                    scaleY = cntScale
                                }
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = cntSource,
                                    indication = LocalIndication.current,
                                    enabled = !isLoading
                                ) { onSelectCount(countOption) }
                                .testTag("quiz_count_$countOption")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = s.quizQuestionsUnit(countOption),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) palette.activeChipText else palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            val (genSource, genScale) = rememberPressScale(0.96f)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = palette.activeChipBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = genScale
                        scaleY = genScale
                    }
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = genSource,
                        indication = LocalIndication.current,
                        enabled = !isLoading,
                        onClick = onGenerateQuiz
                    )
                    .testTag("quiz_generate_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = palette.activeChipText,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = palette.activeChipText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasLoadedQuestions) s.quizRegenerateButton else s.quizGenerateButton,
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.activeChipText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizScoreBanner(
    language: AppLanguage,
    difficulty: QuizDifficulty,
    topic: QuizTopic,
    correctCount: Int,
    answeredCount: Int,
    totalCount: Int,
    onResetAnswers: () -> Unit,
    onShareRound: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val animatedCorrect by animateIntAsState(
        targetValue = correctCount,
        animationSpec = tween(250),
        label = "score_correct_count"
    )
    val animatedAnswered by animateIntAsState(
        targetValue = answeredCount,
        animationSpec = tween(250),
        label = "score_answered_count"
    )
    val targetRatio = if (totalCount > 0) answeredCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "quiz_score_progress"
    )

    LuxuryGlassCard(
        cornerRadius = 18.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quiz_score_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.quizScoreSummary(animatedCorrect, animatedAnswered, totalCount),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.goldPrimary
                    )
                    Text(
                        text = "${difficulty.title(language)} • ${topic.title(language)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary
                    )
                }
                AnimatedVisibility(visible = answeredCount > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val (shareSource, shareScale) = rememberPressScale(0.93f)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.activeChipBg,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = shareScale
                                    scaleY = shareScale
                                }
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = shareSource,
                                    indication = LocalIndication.current,
                                    onClick = onShareRound
                                )
                                .testTag("quiz_share_score_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = s.quizShareResultButton,
                                    tint = palette.activeChipText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        val (resetSource, resetScale) = rememberPressScale(0.93f)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.badgeBg,
                            border = BorderStroke(0.8.dp, palette.goldBorder),
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = resetScale
                                    scaleY = resetScale
                                }
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = resetSource,
                                    indication = LocalIndication.current,
                                    onClick = onResetAnswers
                                )
                                .testTag("quiz_reset_answers_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RestartAlt,
                                    contentDescription = s.quizRetryButton,
                                    tint = palette.goldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = s.quizRetryButton,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                color = if (answeredCount == totalCount && correctCount == totalCount) palette.statusSuccess else palette.goldPrimary,
                trackColor = palette.badgeBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun QuizQuestionCard(
    index: Int,
    total: Int,
    language: AppLanguage,
    question: IslamicQuizQuestion,
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit,
    onShareQuestion: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val isAnswered = selectedOption != null
    val isCorrect = selectedOption == question.correctIndex
    val optionLetters = if (language == AppLanguage.ENGLISH) {
        listOf("A", "B", "C", "D")
    } else {
        listOf("أ", "ب", "ج", "د")
    }

    // Staggered card entrance animation when new questions appear
    val entryAlpha = remember(question.id) { Animatable(0f) }
    val entryOffsetY = remember(question.id) { Animatable(42f) }
    val entryScale = remember(question.id) { Animatable(0.95f) }

    LaunchedEffect(question.id) {
        delay(((index - 1) * 65L).coerceAtMost(350L))
        launch { entryAlpha.animateTo(1f, tween(260)) }
        launch {
            entryOffsetY.animateTo(
                0f,
                spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
            )
        }
        launch {
            entryScale.animateTo(
                1f,
                spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    LuxuryGlassCard(
        highlighted = !isAnswered,
        cornerRadius = 22.dp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = entryAlpha.value
                translationY = entryOffsetY.value
                scaleX = entryScale.value
                scaleY = entryScale.value
            }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag("quiz_question_card_$index")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = palette.badgeBg
                ) {
                    Text(
                        text = s.quizQuestionNumber(index, total),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.goldPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (question.topicLabel.isNotBlank()) {
                        Text(
                            text = question.topicLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onShareQuestion,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("quiz_share_question_icon_$index")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = s.quizShareButton,
                            tint = palette.goldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.textPrimary
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEachIndexed { optIdx, optionText ->
                    val isThisSelected = selectedOption == optIdx
                    val isThisCorrect = optIdx == question.correctIndex
                    val letterLabel = optionLetters.getOrElse(optIdx) { "${optIdx + 1}" }

                    val (optSource, optPressScale) = rememberPressScale(0.96f)
                    val feedbackScale = remember(question.id, optIdx) { Animatable(1f) }
                    val shakeOffsetX = remember(question.id, optIdx) { Animatable(0f) }

                    LaunchedEffect(isAnswered, isThisSelected, isThisCorrect) {
                        if (isAnswered && isThisCorrect && isThisSelected) {
                            feedbackScale.animateTo(1.03f, spring(stiffness = Spring.StiffnessHigh))
                            feedbackScale.animateTo(
                                1f,
                                spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
                            )
                        } else if (isAnswered && isThisSelected && !isThisCorrect) {
                            val keyframes = listOf(-14f, 14f, -10f, 10f, -5f, 0f)
                            for (offset in keyframes) {
                                shakeOffsetX.animateTo(offset, tween(42))
                            }
                        }
                    }

                    val targetBgColor: Color
                    val targetBorderColor: Color
                    val textColor: Color
                    val targetBadgeBg: Color
                    val badgeText: Color

                    when {
                        !isAnswered -> {
                            targetBgColor = palette.glassSurface.copy(alpha = 0.7f)
                            targetBorderColor = palette.goldBorder.copy(alpha = 0.55f)
                            textColor = palette.textPrimary
                            targetBadgeBg = palette.badgeBg
                            badgeText = palette.goldPrimary
                        }
                        isThisCorrect -> {
                            targetBgColor = palette.statusSuccess.copy(alpha = 0.16f)
                            targetBorderColor = palette.statusSuccess
                            textColor = palette.textPrimary
                            targetBadgeBg = palette.statusSuccess
                            badgeText = Color.White
                        }
                        isThisSelected -> {
                            targetBgColor = palette.statusError.copy(alpha = 0.16f)
                            targetBorderColor = palette.statusError
                            textColor = palette.textPrimary
                            targetBadgeBg = palette.statusError
                            badgeText = Color.White
                        }
                        else -> {
                            targetBgColor = palette.glassSurface.copy(alpha = 0.45f)
                            targetBorderColor = palette.goldBorder.copy(alpha = 0.25f)
                            textColor = palette.textSecondary
                            targetBadgeBg = palette.badgeBg.copy(alpha = 0.5f)
                            badgeText = palette.textSecondary
                        }
                    }

                    val animBgColor by animateColorAsState(
                        targetValue = targetBgColor,
                        animationSpec = tween(220),
                        label = "opt_bg_${index}_$optIdx"
                    )
                    val animBorderColor by animateColorAsState(
                        targetValue = targetBorderColor,
                        animationSpec = tween(220),
                        label = "opt_border_${index}_$optIdx"
                    )
                    val animBadgeBg by animateColorAsState(
                        targetValue = targetBadgeBg,
                        animationSpec = tween(220),
                        label = "opt_badge_${index}_$optIdx"
                    )

                    val activeScale = (if (!isAnswered) optPressScale else 1f) * feedbackScale.value

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = animBgColor,
                        border = BorderStroke(
                            width = if (isAnswered && (isThisCorrect || isThisSelected)) 1.5.dp else 1.dp,
                            color = animBorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = activeScale
                                scaleY = activeScale
                                translationX = shakeOffsetX.value
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = optSource,
                                indication = LocalIndication.current,
                                enabled = !isAnswered
                            ) { onSelectOption(optIdx) }
                            .testTag("quiz_q${index}_option_$optIdx")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(animBadgeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letterLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = badgeText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            AnimatedVisibility(
                                visible = isAnswered && (isThisCorrect || isThisSelected),
                                enter = scaleIn(
                                    animationSpec = spring(
                                        dampingRatio = 0.5f,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeIn(tween(180))
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (isThisCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                        contentDescription = if (isThisCorrect) s.quizCorrectBadge else s.quizWrongBadge,
                                        tint = if (isThisCorrect) palette.statusSuccess else palette.statusError,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isAnswered && question.explanation.isNotBlank(),
                enter = fadeIn(tween(240)) + expandVertically(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = fadeOut(tween(150)) + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCorrect) palette.statusSuccess.copy(alpha = 0.10f) else palette.badgeBg,
                    border = BorderStroke(
                        0.8.dp,
                        if (isCorrect) palette.statusSuccess.copy(alpha = 0.6f) else palette.goldBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = palette.goldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (isCorrect) s.quizCorrectBadge else s.quizWrongBadge} • ${s.quizExplanationTitle}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isCorrect) palette.statusSuccess else palette.goldPrimary
                            )
                        }
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textPrimary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isAnswered,
                enter = fadeIn(tween(220)) + expandVertically(),
                exit = fadeOut(tween(150)) + shrinkVertically()
            ) {
                val (shareBtnSource, shareBtnScale) = rememberPressScale(0.95f)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.badgeBg,
                    border = BorderStroke(1.dp, palette.goldBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = shareBtnScale
                            scaleY = shareBtnScale
                        }
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = shareBtnSource,
                            indication = LocalIndication.current,
                            onClick = onShareQuestion
                        )
                        .testTag("quiz_share_question_$index")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = s.quizShareButton,
                            tint = palette.goldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.quizShareButton,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.goldPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizCompletionCard(
    language: AppLanguage,
    difficulty: QuizDifficulty,
    correctCount: Int,
    totalCount: Int,
    onRetry: () -> Unit,
    onGenerateNew: () -> Unit,
    onAdvanceNextDifficulty: () -> Unit,
    onShareResult: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    val pct = if (totalCount > 0) (correctCount * 100) / totalCount else 0
    val feedback = when {
        pct >= 80 -> s.quizFeedbackHigh
        pct >= 50 -> s.quizFeedbackMid
        else -> s.quizFeedbackLow
    }
    val nextDifficulty = when (difficulty) {
        QuizDifficulty.EASY -> QuizDifficulty.MEDIUM
        QuizDifficulty.MEDIUM -> QuizDifficulty.HARD
        QuizDifficulty.HARD -> null
    }

    val cardScale = remember { Animatable(0.92f) }
    val cardAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { cardAlpha.animateTo(1f, tween(260)) }
        launch {
            cardScale.animateTo(
                1f,
                spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    val trophyTransition = rememberInfiniteTransition(label = "completion_trophy")
    val trophyScale by trophyTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_pulse"
    )

    LuxuryGlassCard(
        highlighted = true,
        cornerRadius = 24.dp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = cardAlpha.value
                scaleX = cardScale.value
                scaleY = cardScale.value
            }
            .testTag("quiz_completion_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        scaleX = trophyScale
                        scaleY = trophyScale
                    }
                    .clip(CircleShape)
                    .background(palette.badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = s.quizCompletedTitle,
                    tint = palette.goldPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = s.quizCompletedTitle,
                style = MaterialTheme.typography.titleLarge,
                color = palette.goldPrimary
            )
            Text(
                text = s.quizAccuracyText(correctCount, totalCount, pct),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary
            )
            Text(
                text = feedback,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val (retrySource, retryScale) = rememberPressScale(0.95f)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.badgeBg,
                    border = BorderStroke(1.dp, palette.goldBorder),
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = retryScale
                            scaleY = retryScale
                        }
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = retrySource,
                            indication = LocalIndication.current,
                            onClick = onRetry
                        )
                        .testTag("quiz_completion_retry_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RestartAlt,
                            contentDescription = s.quizRetryButton,
                            tint = palette.goldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.quizRetryButton,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary
                        )
                    }
                }

                val (newSource, newScale) = rememberPressScale(0.95f)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.activeChipBg,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = newScale
                            scaleY = newScale
                        }
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = newSource,
                            indication = LocalIndication.current,
                            onClick = onGenerateNew
                        )
                        .testTag("quiz_completion_new_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = s.quizRegenerateButton,
                            tint = palette.activeChipText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.quizRegenerateButton,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.activeChipText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (nextDifficulty != null) {
                val (nextSource, nextScale) = rememberPressScale(0.96f)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.statusSuccess.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, palette.statusSuccess),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = nextScale
                            scaleY = nextScale
                        }
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = nextSource,
                            indication = LocalIndication.current,
                            onClick = onAdvanceNextDifficulty
                        )
                        .testTag("quiz_advance_difficulty_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = palette.statusSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.quizTryNextLevel(nextDifficulty.title(language)),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.statusSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Prominent Share Result Image Card Button
            val (shareCompSource, shareCompScale) = rememberPressScale(0.96f)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = palette.badgeBg,
                border = BorderStroke(1.2.dp, palette.goldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = shareCompScale
                        scaleY = shareCompScale
                    }
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = shareCompSource,
                        indication = LocalIndication.current,
                        onClick = onShareResult
                    )
                    .testTag("quiz_share_completion_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = s.quizShareResultButton,
                        tint = palette.goldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = s.quizShareResultButton,
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.goldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizSharePreviewDialog(
    card: GeneratedShareCard,
    onShareNow: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .widthIn(max = 460.dp)
        ) {
            LuxuryGlassCard(
                highlighted = true,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_share_preview_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                tint = palette.goldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.quizSharePreviewTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.goldPrimary
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("quiz_close_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = s.close,
                                tint = palette.textSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, palette.goldBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    ) {
                        Image(
                            bitmap = card.bitmap.asImageBitmap(),
                            contentDescription = s.quizSharePreviewTitle,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    val (shareSource, shareScale) = rememberPressScale(0.96f)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.activeChipBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = shareScale
                                scaleY = shareScale
                            }
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = shareSource,
                                indication = LocalIndication.current,
                                onClick = onShareNow
                            )
                            .testTag("quiz_confirm_share_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = s.quizShareNowButton,
                                tint = palette.activeChipText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.quizShareNowButton,
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.activeChipText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
