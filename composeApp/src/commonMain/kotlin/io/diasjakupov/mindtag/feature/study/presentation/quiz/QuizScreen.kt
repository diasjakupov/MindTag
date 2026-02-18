package io.diasjakupov.mindtag.feature.study.presentation.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.widthIn
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
import io.diasjakupov.mindtag.feature.study.domain.model.CardType
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.ShimmerBox
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun QuizScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToResults: (String) -> Unit,
) {
    val viewModel: QuizViewModel = koinViewModel(parameters = { parametersOf(sessionId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is QuizEffect.NavigateToResults -> onNavigateToResults(effect.sessionId)
                is QuizEffect.NavigateBack -> onNavigateBack()
                is QuizEffect.ShowExitConfirmation -> onNavigateBack()
            }
        }
    }

    if (state.isLoading) {
        QuizShimmerSkeleton()
        return
    }

    QuizScreenContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun QuizScreenContent(
    state: QuizState,
    onIntent: (QuizIntent) -> Unit,
) {
    val isCompact = LocalWindowSizeClass.current == WindowSizeClass.Compact

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar: close + timer + spacer
            QuizTopBar(
                timeFormatted = state.timeRemainingFormatted,
                showTimer = state.timeRemainingSeconds != null,
                onExit = { onIntent(QuizIntent.TapExit) },
            )

            // Progress section — stays full-width
            QuizProgressSection(
                currentIndex = state.currentQuestionIndex,
                totalQuestions = state.totalQuestions,
                progressPercent = state.progressPercent,
            )

            // Question + answers scrollable area — centered on medium/expanded
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = if (isCompact) Alignment.TopStart else Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .then(
                            if (isCompact) Modifier.fillMaxWidth()
                            else Modifier.widthIn(max = MindTagSpacing.contentMaxWidthMedium)
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
                        .padding(top = MindTagSpacing.md, bottom = 140.dp),
                ) {
                    Text(
                        text = state.currentQuestion,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = MindTagSpacing.xxxxl),
                    )

                    when (state.cardType) {
                        CardType.MULTIPLE_CHOICE, CardType.TRUE_FALSE -> {
                            Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
                                state.currentOptions.forEach { option ->
                                    QuizOptionCard(
                                        option = option,
                                        isSelected = option.id == state.selectedOptionId,
                                        onClick = { onIntent(QuizIntent.SelectOption(option.id)) },
                                    )
                                }
                            }
                        }
                        CardType.FLASHCARD -> {
                            FlashCardContent(
                                answer = state.flashcardAnswer,
                                isFlipped = state.isFlipped,
                                onFlip = { onIntent(QuizIntent.FlipCard) },
                                onSelfAssess = { quality -> onIntent(QuizIntent.SelfAssess(quality)) },
                            )
                        }
                    }
                }
            }
        }

        // Sticky bottom button — centered on medium/expanded
        if (state.cardType != CardType.FLASHCARD) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .then(
                        if (isCompact) Modifier.fillMaxWidth()
                        else Modifier.widthIn(max = MindTagSpacing.contentMaxWidthMedium)
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MindTagColors.BackgroundDark,
                                    MindTagColors.BackgroundDark,
                                ),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
                        .padding(bottom = MindTagSpacing.quizHorizontalPadding),
                ) {
                    QuizNextButton(
                        isLastQuestion = state.isLastQuestion,
                        enabled = state.selectedOptionId != null,
                        onClick = { onIntent(QuizIntent.TapNext) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizTopBar(
    timeFormatted: String,
    showTimer: Boolean,
    onExit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MindTagSpacing.quizHorizontalPadding,
                vertical = MindTagSpacing.xl,
            )
            .padding(top = MindTagSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Close button
        IconButton(onClick = onExit) {
            Icon(
                imageVector = MindTagIcons.Close,
                contentDescription = "Exit",
                tint = MindTagColors.TextTertiary,
                modifier = Modifier.size(28.dp),
            )
        }

        // Timer badge
        if (showTimer) {
            Row(
                modifier = Modifier
                    .clip(MindTagShapes.full)
                    .background(Color(0xFF1C2533))
                    .border(1.dp, MindTagColors.BorderMedium, MindTagShapes.full)
                    .padding(horizontal = MindTagSpacing.quizHorizontalPadding, vertical = MindTagSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
            ) {
                Icon(
                    imageVector = MindTagIcons.Schedule,
                    contentDescription = "Timer",
                    tint = MindTagColors.Primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum",
                    ),
                    color = Color.White,
                )
            }
        }

        // Spacer for balance
        Spacer(modifier = Modifier.size(MindTagSpacing.iconButtonSize))
    }
}

@Composable
private fun QuizProgressSection(
    currentIndex: Int,
    totalQuestions: Int,
    progressPercent: Float,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent / 100f,
        animationSpec = tween(500),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
            .padding(bottom = MindTagSpacing.xl),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Question ${currentIndex + 1} of $totalQuestions",
                style = MaterialTheme.typography.bodySmall,
                color = MindTagColors.TextTertiary,
            )
            Text(
                text = "${progressPercent.toInt()}% completed",
                style = MaterialTheme.typography.labelMedium,
                color = MindTagColors.Primary,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MindTagShapes.full)
                .background(MindTagColors.QuizProgressTrack),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(MindTagShapes.full)
                    .background(MindTagColors.Primary),
            )
        }
    }
}

@Composable
private fun QuizOptionCard(
    option: QuizOptionUi,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MindTagColors.Primary else MindTagColors.QuizProgressTrack,
        animationSpec = tween(200),
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MindTagColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(200),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(bgColor)
            .border(2.dp, borderColor, MindTagShapes.lg)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
    ) {
        // Radio circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .then(
                    if (isSelected) {
                        Modifier
                            .clip(MindTagShapes.full)
                            .background(MindTagColors.Primary)
                    } else {
                        Modifier
                            .border(2.dp, MindTagColors.TextSlate500, MindTagShapes.full)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(MindTagShapes.full)
                        .background(Color.White),
                )
            }
        }

        // Option text
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isSelected) Color.White else MindTagColors.TextSlate300,
        )
    }
}

@Composable
private fun QuizShimmerSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top bar shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MindTagSpacing.quizHorizontalPadding,
                    vertical = MindTagSpacing.xl,
                )
                .padding(top = MindTagSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ShimmerBox(
                modifier = Modifier.size(40.dp),
                shape = MindTagShapes.md,
            )
            ShimmerBox(
                modifier = Modifier.size(width = 120.dp, height = 40.dp),
                shape = MindTagShapes.full,
            )
            Spacer(modifier = Modifier.size(40.dp))
        }

        // Progress section shimmer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
                .padding(bottom = MindTagSpacing.xl),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ShimmerBox(
                    modifier = Modifier.size(width = 120.dp, height = 14.dp),
                    shape = MindTagShapes.sm,
                )
                ShimmerBox(
                    modifier = Modifier.size(width = 100.dp, height = 14.dp),
                    shape = MindTagShapes.sm,
                )
            }
            Spacer(modifier = Modifier.height(MindTagSpacing.md))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                shape = MindTagShapes.full,
            )
        }

        // Question text shimmer
        Column(
            modifier = Modifier
                .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
                .padding(top = MindTagSpacing.md),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.md))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(26.dp),
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.md))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(26.dp),
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxxl))

        // Answer options shimmer
        Column(
            modifier = Modifier.padding(horizontal = MindTagSpacing.quizHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = MindTagShapes.lg,
                )
            }
        }
    }
}

@Composable
private fun FlashCardContent(
    answer: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSelfAssess: (Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Flip card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MindTagShapes.lg)
                .background(MindTagColors.CardDark)
                .border(1.dp, MindTagColors.BorderMedium, MindTagShapes.lg)
                .clickable(onClick = onFlip),
            contentAlignment = Alignment.Center,
        ) {
            if (isFlipped) {
                Text(
                    text = answer,
                    style = MaterialTheme.typography.titleLarge,
                    color = MindTagColors.Primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(MindTagSpacing.xxl),
                )
            } else {
                Text(
                    text = "Tap to reveal answer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MindTagColors.TextTertiary,
                )
            }
        }

        // Self-assessment buttons (shown only when flipped)
        if (isFlipped) {
            Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

            Text(
                text = "Did you know it?",
                style = MaterialTheme.typography.titleSmall,
                color = MindTagColors.TextSecondary,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
            ) {
                SelfAssessButton(
                    text = "No",
                    color = MindTagColors.Error,
                    onClick = { onSelfAssess(0) },
                    modifier = Modifier.weight(1f),
                )
                SelfAssessButton(
                    text = "Kinda",
                    color = MindTagColors.Warning,
                    onClick = { onSelfAssess(1) },
                    modifier = Modifier.weight(1f),
                )
                SelfAssessButton(
                    text = "Yes",
                    color = MindTagColors.Success,
                    onClick = { onSelfAssess(2) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SelfAssessButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(MindTagShapes.lg)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), MindTagShapes.lg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}

@Composable
private fun QuizNextButton(
    isLastQuestion: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (enabled) MindTagColors.Primary else MindTagColors.Primary.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(MindTagShapes.lg)
            .background(containerColor)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isLastQuestion) "Finish" else "Next Question",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            fontSize = 18.sp,
        )
        if (!isLastQuestion) {
            Spacer(modifier = Modifier.width(MindTagSpacing.md))
            Icon(
                imageVector = MindTagIcons.ArrowForward,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}
