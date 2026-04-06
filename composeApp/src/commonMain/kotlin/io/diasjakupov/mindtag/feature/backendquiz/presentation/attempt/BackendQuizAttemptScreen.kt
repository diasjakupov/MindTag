package io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.hoverBorder
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BackendQuizAttemptScreen(
    quizId: Long,
    attemptId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToResults: (Long, Long) -> Unit,
) {
    val viewModel: BackendQuizAttemptViewModel = koinViewModel(
        parameters = { parametersOf(quizId, attemptId) }
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackendQuizAttemptEffect.NavigateToResults ->
                    onNavigateToResults(effect.quizId, effect.attemptId)
                is BackendQuizAttemptEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    BackendQuizAttemptContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun BackendQuizAttemptContent(
    state: BackendQuizAttemptState,
    onIntent: (BackendQuizAttemptIntent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MindTagColors.Primary,
                )
            }
            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(MindTagSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
                ) {
                    Text(
                        text = "Something went wrong",
                        style = MaterialTheme.typography.titleMedium,
                        color = MindTagColors.TextSecondary,
                    )
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MindTagColors.Error,
                    )
                    Box(
                        modifier = Modifier
                            .clip(MindTagShapes.lg)
                            .background(MindTagColors.Primary)
                            .clickable { onIntent(BackendQuizAttemptIntent.TapBack) }
                            .padding(horizontal = MindTagSpacing.xxl, vertical = MindTagSpacing.lg),
                    ) {
                        Text(
                            text = "Go Back",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }
            else -> {
                val isCompact = LocalWindowSizeClass.current == WindowSizeClass.Compact
                val isExpanded = LocalWindowSizeClass.current == WindowSizeClass.Expanded
                val contentMaxWidth = when {
                    isExpanded -> MindTagSpacing.contentMaxWidthExpanded
                    isCompact -> Dp.Unspecified
                    else -> MindTagSpacing.contentMaxWidthMedium
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Top bar — stays full-width
                    AttemptTopBar(
                        currentIndex = state.currentIndex,
                        totalQuestions = state.totalQuestions,
                        onBack = { onIntent(BackendQuizAttemptIntent.TapBack) },
                    )

                    // Progress bar — stays full-width, but scaled on desktop
                    AttemptProgressBar(
                        currentIndex = state.currentIndex,
                        totalQuestions = state.totalQuestions,
                        isExpanded = isExpanded,
                    )

                    // Question + options — centered on medium/expanded
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = if (isCompact) Alignment.TopStart else Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .then(
                                    if (isCompact) Modifier.fillMaxWidth()
                                    else Modifier.widthIn(max = contentMaxWidth)
                                )
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
                                .padding(top = MindTagSpacing.md, bottom = 160.dp),
                        ) {
                            // Question text
                            Text(
                                text = state.questionText,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = MindTagSpacing.xxxl),
                            )

                            // Options — 2x2 grid on Expanded, vertical on Compact/Medium
                            if (isExpanded && state.options.size >= 3) {
                                val rows = state.options.chunked(2)
                                Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
                                    rows.forEach { rowOptions ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
                                        ) {
                                            rowOptions.forEach { option ->
                                                AttemptOptionCardGrid(
                                                    option = option,
                                                    onClick = { onIntent(BackendQuizAttemptIntent.SelectOption(option.label)) },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                            if (rowOptions.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
                                    state.options.forEach { option ->
                                        AttemptOptionCard(
                                            option = option,
                                            onClick = { onIntent(BackendQuizAttemptIntent.SelectOption(option.label)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom navigation buttons
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .then(
                            if (isCompact) Modifier.fillMaxWidth()
                            else Modifier.widthIn(max = contentMaxWidth)
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
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
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
                            .padding(bottom = MindTagSpacing.quizHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                    ) {
                        val isLast = state.currentIndex == state.totalQuestions - 1
                        val isFirst = state.currentIndex == 0

                        if (isLast) {
                            // Submit button
                            SubmitButton(
                                answeredCount = state.answers.size,
                                totalQuestions = state.totalQuestions,
                                isSubmitting = state.isSubmitting,
                                onClick = { onIntent(BackendQuizAttemptIntent.TapSubmit) },
                            )
                        }

                        // Previous / Next row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                if (isExpanded) MindTagSpacing.xl else MindTagSpacing.md,
                            ),
                        ) {
                            if (!isFirst) {
                                NavButton(
                                    text = "Previous",
                                    modifier = Modifier.weight(1f),
                                    isPrimary = false,
                                    onClick = { onIntent(BackendQuizAttemptIntent.TapPrevious) },
                                )
                            }
                            if (!isLast) {
                                NavButton(
                                    text = "Next",
                                    modifier = Modifier.weight(1f),
                                    isPrimary = true,
                                    onClick = { onIntent(BackendQuizAttemptIntent.TapNext) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttemptTopBar(
    currentIndex: Int,
    totalQuestions: Int,
    onBack: () -> Unit,
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
        IconButton(onClick = onBack) {
            Icon(
                imageVector = MindTagIcons.ArrowBack,
                contentDescription = "Back",
                tint = MindTagColors.TextTertiary,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = "Q ${currentIndex + 1}/${totalQuestions}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )

        Spacer(modifier = Modifier.size(MindTagSpacing.iconButtonSize))
    }
}

@Composable
private fun AttemptProgressBar(
    currentIndex: Int,
    totalQuestions: Int,
    isExpanded: Boolean = false,
) {
    val progress = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
    )
    val barHeight = if (isExpanded) 12.dp else 8.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.quizHorizontalPadding)
            .padding(bottom = MindTagSpacing.xl),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(MindTagShapes.full)
                .background(MindTagColors.QuizProgressTrack),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(barHeight)
                    .clip(MindTagShapes.full)
                    .background(MindTagColors.Primary),
            )
        }
    }
}

@Composable
private fun AttemptOptionCard(
    option: QuizOptionUi,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (option.isSelected) MindTagColors.Primary else MindTagColors.QuizProgressTrack,
        animationSpec = tween(200),
    )
    val bgColor by animateColorAsState(
        targetValue = if (option.isSelected) MindTagColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(200),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(bgColor)
            .border(2.dp, borderColor, MindTagShapes.lg)
            .hoverBorder(
                hoverColor = MindTagColors.Primary.copy(alpha = 0.3f),
                defaultColor = Color.Transparent,
                borderWidth = 1.dp,
            )
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
    ) {
        // Label badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(MindTagShapes.md)
                .background(
                    if (option.isSelected) MindTagColors.Primary
                    else MindTagColors.CardDark,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (option.isSelected) Color.White else MindTagColors.TextTertiary,
            )
        }

        // Option text
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (option.isSelected) Color.White else MindTagColors.TextSlate300,
        )
    }
}

@Composable
private fun AttemptOptionCardGrid(
    option: QuizOptionUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (option.isSelected) MindTagColors.Primary else MindTagColors.QuizProgressTrack,
        animationSpec = tween(200),
    )
    val bgColor by animateColorAsState(
        targetValue = if (option.isSelected) MindTagColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(200),
    )

    Column(
        modifier = modifier
            .clip(MindTagShapes.lg)
            .background(bgColor)
            .border(2.dp, borderColor, MindTagShapes.lg)
            .hoverBorder(
                hoverColor = MindTagColors.Primary.copy(alpha = 0.3f),
                defaultColor = Color.Transparent,
                borderWidth = 1.dp,
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(MindTagShapes.md)
                .background(
                    if (option.isSelected) MindTagColors.Primary
                    else MindTagColors.CardDark,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (option.isSelected) Color.White else MindTagColors.TextTertiary,
            )
        }
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (option.isSelected) Color.White else MindTagColors.TextSlate300,
        )
    }
}

@Composable
private fun SubmitButton(
    answeredCount: Int,
    totalQuestions: Int,
    isSubmitting: Boolean,
    onClick: () -> Unit,
) {
    val allAnswered = answeredCount == totalQuestions
    val containerColor = if (allAnswered) MindTagColors.Success else MindTagColors.Primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(MindTagShapes.lg)
            .background(containerColor)
            .clickable(enabled = !isSubmitting, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = if (allAnswered) "Submit Quiz" else "Submit ($answeredCount/$totalQuestions answered)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun NavButton(
    text: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isPrimary) MindTagColors.Primary else MindTagColors.CardDark
    val textColor = if (isPrimary) Color.White else MindTagColors.TextSecondary

    Row(
        modifier = modifier
            .height(50.dp)
            .clip(MindTagShapes.lg)
            .background(bgColor)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isPrimary) {
            Icon(
                imageVector = MindTagIcons.ArrowBack,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(MindTagSpacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
        if (isPrimary) {
            Spacer(modifier = Modifier.width(MindTagSpacing.sm))
            Icon(
                imageVector = MindTagIcons.ArrowForward,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
