package io.diasjakupov.mindtag.feature.study.presentation.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButton
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButtonVariant
import io.diasjakupov.mindtag.core.designsystem.components.MindTagCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StudyHubScreen(
    onNavigateToQuiz: (String) -> Unit,
) {
    val viewModel: StudyHubViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StudyHubEffect.NavigateToQuiz -> onNavigateToQuiz(effect.sessionId)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MindTagSpacing.topAppBarHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Practice Center",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Quick Quiz card
        StudyActionCard(
            categoryLabel = "ACTIVE RECALL",
            categoryColor = MindTagColors.Primary,
            title = "Quick Quiz",
            description = "Test yourself with 10 questions from your notes",
            buttonText = "Start Quiz",
            isLoading = state.isCreatingSession,
            onClick = { viewModel.onIntent(StudyHubIntent.TapStartQuiz) },
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Exam Mode card
        StudyActionCard(
            categoryLabel = "SIMULATION",
            categoryColor = MindTagColors.Warning,
            title = "Exam Mode",
            description = "Timed 50-question exam to prepare for your test",
            buttonText = "Begin Exam",
            isLoading = state.isCreatingSession,
            onClick = { viewModel.onIntent(StudyHubIntent.TapBeginExam) },
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        // Weekly Performance section
        WeeklyPerformanceSection()

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Weakest Topic card
        WeakestTopicCard()

        Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding))
    }
}

@Composable
private fun StudyActionCard(
    categoryLabel: String,
    categoryColor: Color,
    title: String,
    description: String,
    buttonText: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    MindTagCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = MindTagSpacing.xxl,
    ) {
        // Category label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(MindTagShapes.full)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MindTagIcons.MoreHoriz, // placeholder
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = categoryColor,
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MindTagColors.TextSecondary,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = MindTagColors.Primary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            }
        } else {
            MindTagButton(
                text = buttonText,
                onClick = onClick,
                variant = MindTagButtonVariant.PrimaryMedium,
            )
        }
    }
}

@Composable
private fun WeeklyPerformanceSection() {
    // Section header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Weekly Performance",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Text(
            text = "View All",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MindTagColors.Primary,
        )
    }

    Spacer(modifier = Modifier.height(MindTagSpacing.xl))

    MindTagCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = MindTagSpacing.xxl,
    ) {
        // Summary stats
        Row(
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xxxxl),
        ) {
            Column {
                Text(
                    text = "AVG. SCORE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MindTagColors.TextSecondary,
                )
                Spacer(modifier = Modifier.height(MindTagSpacing.xs))
                Text(
                    text = "87%",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
            Column {
                Text(
                    text = "STREAK",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MindTagColors.TextSecondary,
                )
                Spacer(modifier = Modifier.height(MindTagSpacing.xs))
                Text(
                    text = "5",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxl))

        // Bar chart
        PerformanceBarChart()
    }
}

@Composable
private fun PerformanceBarChart() {
    val days = listOf("M", "T", "W", "T", "F")
    val scores = listOf(70f, 85f, 60f, 90f, 75f)
    val maxScore = 100f
    val primaryColor = MindTagColors.Primary
    val trackColor = MindTagColors.QuizProgressTrack

    Column {
        // Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            scores.forEach { score ->
                val fraction = score / maxScore

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(28.dp)
                            .height(120.dp),
                    ) {
                        val barWidth = size.width
                        val totalHeight = size.height
                        val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                        // Track (full height background)
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset.Zero,
                            size = Size(barWidth, totalHeight),
                            cornerRadius = cornerRadius,
                        )

                        // Filled bar
                        val filledHeight = totalHeight * fraction
                        val alpha = 0.3f + (fraction * 0.7f)
                        drawRoundRect(
                            color = primaryColor.copy(alpha = alpha),
                            topLeft = Offset(0f, totalHeight - filledHeight),
                            size = Size(barWidth, filledHeight),
                            cornerRadius = cornerRadius,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            days.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MindTagColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeakestTopicCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(MindTagColors.Primary.copy(alpha = 0.1f))
            .padding(MindTagSpacing.xl),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(MindTagShapes.full)
                        .background(MindTagColors.BackgroundDark),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MindTagColors.Primary,
                    )
                }

                Column {
                    Text(
                        text = "Review: Economics 101",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(MindTagSpacing.xxs))
                    Text(
                        text = "Mastery: 42%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MindTagColors.TextSecondary,
                    )
                }
            }

            Icon(
                imageVector = MindTagIcons.ArrowForward,
                contentDescription = "Practice Now",
                tint = MindTagColors.TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
