package io.diasjakupov.mindtag.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButton
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButtonVariant
import io.diasjakupov.mindtag.core.designsystem.components.MindTagChip
import io.diasjakupov.mindtag.core.designsystem.components.MindTagChipVariant
import io.diasjakupov.mindtag.core.designsystem.components.ShimmerBox
import io.diasjakupov.mindtag.feature.home.domain.model.ReviewCard
import io.diasjakupov.mindtag.feature.home.domain.model.TaskType
import io.diasjakupov.mindtag.feature.home.domain.model.UpNextTask
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToNote: (String) -> Unit = {},
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeContract.Effect.NavigateToNote -> onNavigateToNote(effect.noteId)
                is HomeContract.Effect.NavigateToQuiz -> { /* future */ }
            }
        }
    }

    if (state.isLoading) {
        HomeShimmerSkeleton()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark)
            .verticalScroll(rememberScrollState()),
    ) {
        // Header
        HeaderSection(
            userName = state.userName,
            streak = state.currentStreak,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        // Due for Review
        DueForReviewSection(
            reviewCards = state.reviewCards,
            onCardClick = { noteId ->
                viewModel.onIntent(HomeContract.Intent.TapReviewCard(noteId))
            },
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xxl))

        // Up Next
        UpNextSection(
            tasks = state.upNextTasks,
            reviewCards = state.reviewCards,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding))
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    streak: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = MindTagSpacing.screenHorizontalPadding, end = MindTagSpacing.screenHorizontalPadding),
    ) {
        // Avatar row with settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(MindTagSpacing.avatarSize)
                    .clip(MindTagShapes.full)
                    .background(MindTagColors.Primary.copy(alpha = 0.2f))
                    .border(2.dp, MindTagColors.Primary.copy(alpha = 0.2f), MindTagShapes.full),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MindTagIcons.Profile,
                    contentDescription = "Profile",
                    tint = MindTagColors.Primary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Icon(
                imageVector = MindTagIcons.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Greeting
        val greeting = getGreeting()
        Text(
            text = "$greeting, $userName",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xs))

        // AI status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.sm),
        ) {
            Icon(
                imageVector = MindTagIcons.CheckCircle,
                contentDescription = null,
                tint = MindTagColors.Primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "AI Plan Updated",
                style = MaterialTheme.typography.bodySmall,
                color = MindTagColors.Primary,
            )
        }
    }
}

@Composable
private fun DueForReviewSection(
    reviewCards: List<ReviewCard>,
    onCardClick: (String) -> Unit,
) {
    Column {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Due for Review",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Text(
                text = "See All",
                style = MaterialTheme.typography.bodySmall,
                color = MindTagColors.TextSecondary,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        if (reviewCards.isEmpty()) {
            // Empty state
            ReviewEmptyState()
        } else {
            // Horizontal carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = MindTagSpacing.screenHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
            ) {
                items(reviewCards, key = { it.noteId }) { card ->
                    ReviewCardItem(
                        card = card,
                        onClick = { onCardClick(card.noteId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
            .clip(MindTagShapes.lg)
            .background(MindTagColors.CardDark)
            .padding(vertical = MindTagSpacing.xxxxl, horizontal = MindTagSpacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(MindTagShapes.full)
                    .background(Color(0xFF135BEC).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MindTagColors.Primary,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(modifier = Modifier.height(MindTagSpacing.xl))

            Text(
                text = "All caught up!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.md))

            Text(
                text = "No notes due for review right now. Great job!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF92A4C9),
            )
        }
    }
}

@Composable
private fun ReviewCardItem(
    card: ReviewCard,
    onClick: () -> Unit,
) {
    val subjectColor = parseHexColor(card.subjectColorHex)

    Column(
        modifier = Modifier
            .width(260.dp)
            .clip(MindTagShapes.lg)
            .background(MindTagColors.CardDark)
            .border(1.dp, MindTagColors.BorderSubtle, MindTagShapes.lg)
            .clickable(onClick = onClick)
            .padding(MindTagSpacing.lg),
    ) {
        // Subject image placeholder with subject tag
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(MindTagShapes.md)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            subjectColor.copy(alpha = 0.3f),
                            MindTagColors.SurfaceDark,
                        ),
                    ),
                ),
        ) {
            // Subject tag in top-right
            MindTagChip(
                text = card.subjectName,
                variant = MindTagChipVariant.SubjectTag,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(MindTagSpacing.md),
            )
            // Decorative circle (representing subject visual)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .clip(MindTagShapes.full)
                    .background(subjectColor.copy(alpha = 0.2f)),
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        // Note title
        Text(
            text = card.noteTitle,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        // Progress bar + percent
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            LinearProgressIndicator(
                progress = { card.progressPercent / 100f },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(MindTagShapes.full),
                color = progressColor(card.progressPercent),
                trackColor = MindTagColors.Divider,
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = "${card.progressPercent.toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MindTagColors.TextSecondary,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        // Review Now button
        MindTagButton(
            text = "Review Now",
            onClick = onClick,
            variant = MindTagButtonVariant.PrimaryMedium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun UpNextSection(
    tasks: List<UpNextTask>,
    reviewCards: List<ReviewCard>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
    ) {
        // Section header with divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            Text(
                text = "Up Next",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MindTagColors.Divider),
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        // Syllabus focus banner
        SyllabusFocusBanner(reviewCards = reviewCards)

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Task list
        tasks.forEachIndexed { index, task ->
            val isLocked = index == tasks.lastIndex && tasks.size >= 3
            TaskItem(task = task, isLocked = isLocked)
            if (index < tasks.lastIndex) {
                Spacer(modifier = Modifier.height(MindTagSpacing.lg))
            }
        }
    }
}

@Composable
private fun SyllabusFocusBanner(
    reviewCards: List<ReviewCard>,
) {
    val topCard = reviewCards.firstOrNull()
    val weekLabel = topCard?.weekNumber?.let { "Week $it" } ?: "This Week"
    val subjectLabel = topCard?.subjectName ?: "Study"
    val dueCount = reviewCards.sumOf { it.dueCardCount }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MindTagColors.Primary.copy(alpha = 0.8f),
                        Color(0xFF1E3A8A).copy(alpha = 0.8f), // blue-900
                    ),
                ),
            )
            .padding(MindTagSpacing.xxl),
    ) {
        Column {
            Text(
                text = "CURRENT SYLLABUS FOCUS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color(0xFFBFDBFE), // blue-100
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.xs))

            Text(
                text = "$weekLabel: $subjectLabel",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.lg))

            Row(
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
            ) {
                MindTagChip(
                    text = "$dueCount Cards Due",
                    variant = MindTagChipVariant.Status,
                )
                MindTagChip(
                    text = "Due Friday",
                    variant = MindTagChipVariant.Status,
                )
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: UpNextTask,
    isLocked: Boolean,
) {
    val taskAlpha = if (isLocked) 0.6f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(taskAlpha)
            .clip(MindTagShapes.md)
            .background(MindTagColors.CardDark)
            .border(1.dp, MindTagColors.BorderSubtle, MindTagShapes.md)
            .padding(MindTagSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
    ) {
        // Checkbox / lock
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(
                    2.dp,
                    if (isLocked) MindTagColors.TextSecondary.copy(alpha = 0.3f)
                    else MindTagColors.TextSecondary.copy(alpha = 0.5f),
                    RoundedCornerShape(4.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isLocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = MindTagColors.TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        // Title + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (isLocked) "Locked until previous tasks complete" else task.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MindTagColors.TextSecondary,
            )
        }

        // Estimated time
        val timeEstimate = when (task.type) {
            TaskType.REVIEW -> "20 min"
            TaskType.QUIZ -> "10 min"
            TaskType.NOTE -> "15 min"
        }
        Box(
            modifier = Modifier
                .clip(MindTagShapes.default)
                .background(MindTagColors.BackgroundDark)
                .padding(horizontal = MindTagSpacing.md, vertical = MindTagSpacing.xs),
        ) {
            Text(
                text = timeEstimate,
                style = MaterialTheme.typography.labelMedium,
                color = MindTagColors.TextSecondary,
            )
        }
    }
}

private fun getGreeting(): String {
    val hour = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    return try {
        Color(("FF$cleaned").toLong(16))
    } catch (_: Exception) {
        Color.White
    }
}

private fun progressColor(percent: Float): Color = when {
    percent >= 70f -> MindTagColors.Success
    percent >= 40f -> MindTagColors.ProgressYellow
    else -> MindTagColors.ProgressRed
}

@Composable
private fun HomeShimmerSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp),
    ) {
        // Header skeleton: avatar + text lines
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ShimmerBox(
                modifier = Modifier.size(48.dp),
                shape = MindTagShapes.full,
            )
            ShimmerBox(
                modifier = Modifier.size(28.dp),
                shape = MindTagShapes.md,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Greeting text lines
        Column(
            modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(28.dp),
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.md))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(16.dp),
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        // Section title shimmer
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                .fillMaxWidth(0.4f)
                .height(20.dp),
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        // Horizontal card shimmer row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = MindTagSpacing.screenHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
        ) {
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .width(260.dp)
                        .height(280.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxl))

        // Up Next task shimmer rows
        Column(
            modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(20.dp),
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.md))
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = MindTagShapes.md,
                )
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding))
    }
}
