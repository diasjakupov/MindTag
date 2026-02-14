package io.diasjakupov.mindtag.feature.study.presentation.results

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.ShimmerBox
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ResultsScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLibrary: () -> Unit,
) {
    val viewModel: ResultsViewModel = koinViewModel(parameters = { parametersOf(sessionId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ResultsEffect.NavigateBack -> onNavigateBack()
                is ResultsEffect.NavigateToLibrary -> onNavigateToLibrary()
            }
        }
    }

    if (state.isLoading) {
        ResultsShimmerSkeleton()
        return
    }

    ResultsScreenContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun ResultsScreenContent(
    state: ResultsState,
    onIntent: (ResultsIntent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
        ) {
            // Top bar
            ResultsTopBar(
                onClose = { onIntent(ResultsIntent.TapClose) },
            )

            // Score ring
            ScoreRingSection(
                scorePercent = state.scorePercent,
                feedbackMessage = state.feedbackMessage,
                feedbackSubtext = state.feedbackSubtext,
            )

            // Time stat
            TimeStatCard(timeSpent = state.timeSpent)

            Spacer(modifier = Modifier.height(MindTagSpacing.xxxxl))

            // Detailed Analysis section
            DetailedAnalysisSection(
                answers = state.answers,
                expandedAnswerId = state.expandedAnswerId,
                onToggle = { onIntent(ResultsIntent.ToggleAnswer(it)) },
            )
        }

        // Sticky footer button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MindTagColors.BackgroundDark.copy(alpha = 0.8f),
                            MindTagColors.BackgroundDark,
                        ),
                    ),
                )
                .padding(MindTagSpacing.xl)
                .padding(bottom = MindTagSpacing.sm),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(MindTagShapes.lg)
                    .background(MindTagColors.Primary)
                    .clickable { onIntent(ResultsIntent.TapReviewNotes) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = MindTagIcons.MenuBook,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(MindTagSpacing.md))
                Text(
                    text = "Review Related Notes",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ResultsTopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MindTagSpacing.topAppBarHeight)
            .padding(horizontal = MindTagSpacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = MindTagIcons.Close,
                contentDescription = "Close",
                tint = Color.White,
            )
        }
        Text(
            text = "Quiz Results",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        // Invisible spacer for centering
        Spacer(modifier = Modifier.size(MindTagSpacing.iconButtonSize))
    }
}

@Composable
private fun ScoreRingSection(
    scorePercent: Int,
    feedbackMessage: String,
    feedbackSubtext: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MindTagSpacing.md, bottom = MindTagSpacing.xxxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Score ring
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val strokeWidth = 6.dp.toPx()
                val padding = strokeWidth / 2
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(padding, padding)

                // Track arc
                drawArc(
                    color = MindTagColors.Primary.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                // Fill arc
                drawArc(
                    color = MindTagColors.Primary,
                    startAngle = -90f,
                    sweepAngle = 360f * scorePercent / 100f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }

            Text(
                text = "$scorePercent%",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                ),
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        Text(
            text = feedbackMessage,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        Text(
            text = feedbackSubtext,
            style = MaterialTheme.typography.bodyMedium,
            color = MindTagColors.TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = MindTagSpacing.xxxxl),
        )
    }
}

@Composable
private fun TimeStatCard(timeSpent: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.xl),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MindTagShapes.xl)
                .background(MindTagColors.SurfaceDarkAlt)
                .border(1.dp, MindTagColors.BorderMedium, MindTagShapes.xl)
                .padding(MindTagSpacing.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(MindTagShapes.full)
                    .background(MindTagColors.Info.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MindTagIcons.Schedule,
                    contentDescription = null,
                    tint = MindTagColors.Info,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(MindTagSpacing.lg))

            Column {
                Text(
                    text = "TIME",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MindTagColors.TextTertiary,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = timeSpent,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun DetailedAnalysisSection(
    answers: List<AnswerDetailUi>,
    expandedAnswerId: String?,
    onToggle: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.xl),
    ) {
        // Section heading
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
            modifier = Modifier.padding(bottom = MindTagSpacing.xl),
        ) {
            Icon(
                imageVector = MindTagIcons.Analytics,
                contentDescription = null,
                tint = MindTagColors.Primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Detailed Analysis",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }

        // Answer cards
        Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
            answers.forEach { answer ->
                AnswerCard(
                    answer = answer,
                    isExpanded = answer.cardId == expandedAnswerId,
                    onClick = { onToggle(answer.cardId) },
                )
            }
        }
    }
}

@Composable
private fun AnswerCard(
    answer: AnswerDetailUi,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val statusColor = if (answer.isCorrect) MindTagColors.Success else MindTagColors.Error
    val statusBgColor = if (answer.isCorrect) MindTagColors.SuccessBg else MindTagColors.ErrorBg
    val borderModifier = if (!answer.isCorrect && isExpanded) {
        Modifier.border(1.dp, MindTagColors.Error.copy(alpha = 0.2f), MindTagShapes.lg)
    } else {
        Modifier.border(1.dp, MindTagColors.BorderMedium.copy(alpha = 0.5f), MindTagShapes.lg)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(MindTagColors.SurfaceDarkAlt)
            .then(borderModifier)
            .animateContentSize(animationSpec = tween(300)),
    ) {
        // Header row (always visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(MindTagSpacing.xl),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(MindTagShapes.full)
                    .background(statusBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (answer.isCorrect) MindTagIcons.Check else MindTagIcons.Close,
                    contentDescription = if (answer.isCorrect) "Correct" else "Incorrect",
                    tint = statusColor,
                    modifier = Modifier.size(16.dp),
                )
            }

            // Question text
            Text(
                text = answer.questionText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (isExpanded) Color.White else MindTagColors.TextSlate300,
                modifier = Modifier.weight(1f),
            )

            // Expand icon
            Icon(
                imageVector = MindTagIcons.ExpandMore,
                contentDescription = "Expand",
                tint = MindTagColors.TextTertiary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (isExpanded) 180f else 0f),
            )
        }

        // Expanded content
        if (isExpanded) {
            HorizontalDivider(
                color = MindTagColors.NodeBorder.copy(alpha = 0.5f),
                thickness = 1.dp,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MindTagColors.BorderMedium.copy(alpha = 0.2f))
                    .padding(MindTagSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
            ) {
                // Your Answer
                AnswerBlock(
                    label = "YOUR ANSWER",
                    labelColor = if (answer.isCorrect) MindTagColors.Success else MindTagColors.Error,
                    text = answer.userAnswer,
                    textColor = if (answer.isCorrect) Color(0xFFBBF7D0) else Color(0xFFFECACA),
                    bgColor = if (answer.isCorrect) Color(0x1A22C55E) else Color(0x1AEF4444),
                    borderColor = if (answer.isCorrect) Color(0x4D22C55E) else Color(0x4DEF4444),
                )

                // Correct Answer (only shown if incorrect)
                if (!answer.isCorrect) {
                    AnswerBlock(
                        label = "CORRECT ANSWER",
                        labelColor = MindTagColors.Success,
                        text = answer.correctAnswer,
                        textColor = Color(0xFFBBF7D0),
                        bgColor = Color(0x1A22C55E),
                        borderColor = Color(0x4D22C55E),
                    )
                }

                // AI Insight (only for incorrect)
                if (!answer.isCorrect && answer.aiInsight != null) {
                    AiInsightBlock(insight = answer.aiInsight)
                }
            }
        }
    }
}

@Composable
private fun AnswerBlock(
    label: String,
    labelColor: Color,
    text: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.sm)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = labelColor,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = MindTagSpacing.xs),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MindTagShapes.md)
                .background(bgColor)
                .border(1.dp, borderColor, MindTagShapes.md)
                .padding(MindTagSpacing.lg),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )
        }
    }
}

@Composable
private fun ResultsShimmerSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top bar shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MindTagSpacing.topAppBarHeight)
                .padding(horizontal = MindTagSpacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(
                modifier = Modifier.size(40.dp),
                shape = MindTagShapes.md,
            )
            Spacer(modifier = Modifier.weight(1f))
            ShimmerBox(
                modifier = Modifier.size(width = 120.dp, height = 20.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        // Score ring shimmer
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            ShimmerBox(
                modifier = Modifier.size(160.dp),
                shape = MindTagShapes.full,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        // Feedback text shimmer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShimmerBox(
                modifier = Modifier.size(width = 200.dp, height = 24.dp),
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.md))
            ShimmerBox(
                modifier = Modifier.size(width = 250.dp, height = 16.dp),
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxxl))

        // Time stat shimmer
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.xl)
                .height(64.dp),
            shape = MindTagShapes.xl,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxxl))

        // Analysis section shimmer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            ShimmerBox(
                modifier = Modifier.size(width = 160.dp, height = 20.dp),
            )
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = MindTagShapes.lg,
                )
            }
        }
    }
}

@Composable
private fun AiInsightBlock(insight: String) {
    Column(modifier = Modifier.padding(top = MindTagSpacing.md)) {
        Box(
                modifier = Modifier
                    .clip(MindTagShapes.full)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(MindTagColors.Primary, MindTagColors.AccentPurple),
                        ),
                    )
                    .padding(horizontal = MindTagSpacing.md, vertical = MindTagSpacing.xxs),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xs),
                ) {
                    Icon(
                        imageVector = MindTagIcons.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "AI Insight",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        ),
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(MindTagSpacing.md))

            // Insight content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MindTagShapes.lg)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MindTagColors.Primary.copy(alpha = 0.05f),
                                MindTagColors.AccentPurple.copy(alpha = 0.05f),
                            ),
                        ),
                    )
                    .border(
                        1.dp,
                        MindTagColors.Primary.copy(alpha = 0.2f),
                        MindTagShapes.lg,
                    )
                    .padding(MindTagSpacing.xl),
            ) {
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodySmall,
                    color = MindTagColors.TextSlate300,
                    lineHeight = 20.sp,
                )
            }
    }
}
