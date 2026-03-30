package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.components.ShimmerBox
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BackendQuizResultsScreen(
    quizId: Long,
    attemptId: Long,
    onNavigateBack: () -> Unit,
) {
    val viewModel: BackendQuizResultsViewModel = koinViewModel(
        parameters = { parametersOf(quizId, attemptId) }
    )
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackendQuizResultsEffect.NavigateBack -> onNavigateBack()
                is BackendQuizResultsEffect.NavigateToQuizList -> onNavigateBack()
                is BackendQuizResultsEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (state.isLoading) {
        BackendQuizResultsShimmer()
        return
    }

    if (state.errorMessage != null) {
        BackendQuizResultsError(
            message = state.errorMessage!!,
            onRetry = { viewModel.onIntent(BackendQuizResultsIntent.TapRetry) },
        )
        return
    }

    BackendQuizResultsContent(state = state, onIntent = viewModel::onIntent, snackbarHostState = snackbarHostState)
}

@Composable
private fun BackendQuizResultsContent(
    state: BackendQuizResultsState,
    onIntent: (BackendQuizResultsIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val isCompact = LocalWindowSizeClass.current == WindowSizeClass.Compact

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (isCompact) Alignment.TopStart else Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .then(
                        if (isCompact) Modifier.fillMaxWidth()
                        else Modifier.widthIn(max = MindTagSpacing.contentMaxWidthMedium)
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp),
            ) {
                // Top bar
                ResultsTopBar(
                    title = state.noteTitleSnapshot,
                    onClose = { onIntent(BackendQuizResultsIntent.TapClose) },
                )

                // Score ring section
                ScoreSection(
                    score = state.score,
                    correctAnswers = state.correctAnswers,
                    totalQuestions = state.totalQuestions,
                    feedbackMessage = state.feedbackMessage,
                )

                Spacer(modifier = Modifier.height(MindTagSpacing.xxxxl))

                // Question results list
                QuestionResultsSection(
                    questionResults = state.questionResults,
                    onToggle = { onIntent(BackendQuizResultsIntent.ToggleQuestion(it)) },
                )
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MindTagColors.CardDark,
                contentColor = MindTagColors.Success,
            )
        }

        // Sticky bottom buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .then(
                    if (isCompact) Modifier.fillMaxWidth()
                    else Modifier.widthIn(max = MindTagSpacing.contentMaxWidthMedium)
                )
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
            ) {
                // Save to Study button
                if (!state.hasSaved) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(MindTagShapes.lg)
                            .background(MindTagColors.CardDark)
                            .clickable { onIntent(BackendQuizResultsIntent.TapSaveToStudy) },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = MindTagIcons.School,
                            contentDescription = null,
                            tint = MindTagColors.Primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(MindTagSpacing.md))
                        Text(
                            text = "Save to Study",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MindTagColors.Primary,
                        )
                    }
                }

                // Done button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(MindTagShapes.lg)
                        .background(MindTagColors.Primary)
                        .clickable { onIntent(BackendQuizResultsIntent.TapClose) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MindTagIcons.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(MindTagSpacing.md))
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                }
            }
        }
    }

    if (state.showSaveDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(BackendQuizResultsIntent.DismissSaveDialog) },
            containerColor = MindTagColors.CardDark,
            title = {
                Text("Save to Study", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Which questions do you want to save as flashcards?",
                    color = MindTagColors.TextSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = { onIntent(BackendQuizResultsIntent.ConfirmSave(saveAll = true)) },
                    colors = ButtonDefaults.buttonColors(containerColor = MindTagColors.Primary),
                ) {
                    Text("Save all", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { onIntent(BackendQuizResultsIntent.ConfirmSave(saveAll = false)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Text("Missed only", color = MindTagColors.Primary)
                }
            },
        )
    }
}

@Composable
private fun ResultsTopBar(
    title: String,
    onClose: () -> Unit,
) {
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
            text = if (title.isNotEmpty()) title else "Quiz Results",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(MindTagSpacing.iconButtonSize))
    }
}

@Composable
private fun ScoreSection(
    score: Int,
    correctAnswers: Int,
    totalQuestions: Int,
    feedbackMessage: String,
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
                    sweepAngle = 360f * score / 100f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }

            Text(
                text = "$score%",
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
            text = "$correctAnswers / $totalQuestions correct",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MindTagColors.TextTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun QuestionResultsSection(
    questionResults: List<QuestionResultUi>,
    onToggle: (Long) -> Unit,
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
                text = "Question Results",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg)) {
            questionResults.forEach { qr ->
                QuestionResultCard(
                    questionResult = qr,
                    onClick = { onToggle(qr.questionId) },
                )
            }
        }
    }
}

@Composable
private fun QuestionResultCard(
    questionResult: QuestionResultUi,
    onClick: () -> Unit,
) {
    val statusColor = if (questionResult.isCorrect) MindTagColors.Success else MindTagColors.Error
    val statusBgColor = if (questionResult.isCorrect) MindTagColors.SuccessBg else MindTagColors.ErrorBg
    val borderModifier = if (!questionResult.isCorrect && questionResult.isExpanded) {
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
        // Header row
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
                    imageVector = if (questionResult.isCorrect) MindTagIcons.Check else MindTagIcons.Close,
                    contentDescription = if (questionResult.isCorrect) "Correct" else "Incorrect",
                    tint = statusColor,
                    modifier = Modifier.size(16.dp),
                )
            }

            // Question text
            Text(
                text = questionResult.questionText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (questionResult.isExpanded) Color.White else MindTagColors.TextSlate300,
                modifier = Modifier.weight(1f),
            )

            // Expand icon
            Icon(
                imageVector = MindTagIcons.ExpandMore,
                contentDescription = "Expand",
                tint = MindTagColors.TextTertiary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (questionResult.isExpanded) 180f else 0f),
            )
        }

        // Expanded content
        if (questionResult.isExpanded) {
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
                // All options
                Text(
                    text = "OPTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MindTagColors.TextTertiary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = MindTagSpacing.xs),
                )
                Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.sm)) {
                    questionResult.options.forEach { option ->
                        val isCorrectOption = option == questionResult.correctAnswer
                        val isUserWrongAnswer = option == questionResult.userAnswer && !questionResult.isCorrect

                        val bgColor = when {
                            isCorrectOption -> Color(0x1A22C55E)
                            isUserWrongAnswer -> Color(0x1AEF4444)
                            else -> Color.Transparent
                        }
                        val borderColor = when {
                            isCorrectOption -> Color(0x4D22C55E)
                            isUserWrongAnswer -> Color(0x4DEF4444)
                            else -> MindTagColors.BorderMedium.copy(alpha = 0.3f)
                        }
                        val textColor = when {
                            isCorrectOption -> Color(0xFFBBF7D0)
                            isUserWrongAnswer -> Color(0xFFFECACA)
                            else -> MindTagColors.TextSlate300
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MindTagShapes.md)
                                .background(bgColor)
                                .border(1.dp, borderColor, MindTagShapes.md)
                                .padding(MindTagSpacing.lg),
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor,
                            )
                        }
                    }
                }

                // Explanation
                if (questionResult.explanation != null) {
                    Spacer(modifier = Modifier.height(MindTagSpacing.sm))
                    Text(
                        text = "EXPLANATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MindTagColors.TextTertiary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = MindTagSpacing.xs),
                    )
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
                            text = questionResult.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MindTagColors.TextSlate300,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackendQuizResultsError(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
            modifier = Modifier.padding(MindTagSpacing.xxxxl),
        ) {
            Text(
                text = "Failed to load results",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MindTagColors.TextTertiary,
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier
                    .clip(MindTagShapes.lg)
                    .background(MindTagColors.Primary)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = MindTagSpacing.xxxxl, vertical = MindTagSpacing.lg),
            ) {
                Text(
                    text = "Retry",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun BackendQuizResultsShimmer() {
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
            ShimmerBox(modifier = Modifier.size(40.dp), shape = MindTagShapes.md)
            Spacer(modifier = Modifier.weight(1f))
            ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 20.dp))
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        // Score ring shimmer
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ShimmerBox(modifier = Modifier.size(160.dp), shape = MindTagShapes.full)
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        // Feedback shimmer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShimmerBox(modifier = Modifier.size(width = 200.dp, height = 24.dp))
            Spacer(modifier = Modifier.height(MindTagSpacing.md))
            ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 16.dp))
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxxl))

        // Question cards shimmer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            ShimmerBox(modifier = Modifier.size(width = 160.dp, height = 20.dp))
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
