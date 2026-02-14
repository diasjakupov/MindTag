package io.diasjakupov.mindtag.feature.planner.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagCard
import io.diasjakupov.mindtag.core.designsystem.components.MindTagChip
import io.diasjakupov.mindtag.core.designsystem.components.MindTagChipVariant
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlannerScreen() {
    val viewModel: PlannerViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            PlannerHeader()

            // Segmented control
            SegmentedControl(
                selectedMode = state.viewMode,
                onModeSelected = { viewModel.onIntent(PlannerContract.Intent.SwitchView(it)) },
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.xl))

            // Curriculum title
            Column(
                modifier = Modifier.padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            ) {
                Text(
                    text = "Curriculum",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(MindTagSpacing.xs))
                Text(
                    text = "${(state.overallProgress * 100).toInt()}% Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MindTagColors.TextSecondary,
                )
            }

            Spacer(modifier = Modifier.height(MindTagSpacing.xl))

            // Week cards
            LazyColumn(
                contentPadding = PaddingValues(
                    start = MindTagSpacing.screenHorizontalPadding,
                    end = MindTagSpacing.screenHorizontalPadding,
                    bottom = MindTagSpacing.bottomContentPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
            ) {
                items(state.weeks, key = { it.id }) { week ->
                    WeekCard(
                        week = week,
                        isExpanded = state.expandedWeekId == week.id,
                        onToggleExpand = { viewModel.onIntent(PlannerContract.Intent.ToggleWeek(week.id)) },
                        onToggleTask = { taskId -> viewModel.onIntent(PlannerContract.Intent.ToggleTask(taskId)) },
                    )
                }
            }
        }

        // FAB overlay
        FloatingActionButton(
            onClick = { /* non-functional shell */ },
            containerColor = MindTagColors.Primary,
            contentColor = Color.White,
            shape = MindTagShapes.full,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = MindTagSpacing.screenHorizontalPadding, bottom = MindTagSpacing.xl),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Syllabus",
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(MindTagSpacing.md))
                Text(
                    text = "Add Syllabus",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun PlannerHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MindTagSpacing.topAppBarHeight)
            .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Weekly Planner",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
    }
}

@Composable
private fun SegmentedControl(
    selectedMode: PlannerContract.ViewMode,
    onModeSelected: (PlannerContract.ViewMode) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(MindTagShapes.md)
                .background(MindTagColors.SegmentedControlBg)
                .padding(MindTagSpacing.xs),
        ) {
            PlannerContract.ViewMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                val label = when (mode) {
                    PlannerContract.ViewMode.CALENDAR -> "Calendar"
                    PlannerContract.ViewMode.LIST -> "List"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(MindTagShapes.sm)
                        .then(
                            if (isSelected) Modifier.background(MindTagColors.SegmentedControlActiveBg)
                            else Modifier
                        )
                        .clickable { onModeSelected(mode) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) Color.White else MindTagColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekCard(
    week: PlannerContract.WeekData,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleTask: (String) -> Unit,
) {
    val progressColor = when {
        week.progress >= 0.7f -> MindTagColors.Success
        week.progress >= 0.3f -> MindTagColors.ProgressYellow
        week.progress > 0f -> MindTagColors.ProgressRed
        else -> MindTagColors.TextSecondary
    }

    val completedCount = week.tasks.count { it.isCompleted }
    val totalCount = week.tasks.size

    MindTagCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 0.dp,
    ) {
        // Collapsed header (always visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(MindTagSpacing.xl),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Week label + date range
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                ) {
                    MindTagChip(
                        text = "Week ${week.weekNumber}",
                        variant = if (week.isCurrentWeek) MindTagChipVariant.WeekLabel else MindTagChipVariant.Metadata,
                    )
                    Text(
                        text = week.dateRange,
                        style = MaterialTheme.typography.labelSmall,
                        color = MindTagColors.TextSecondary,
                    )
                    if (week.isCurrentWeek) {
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MindTagColors.Primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MindTagSpacing.md))

                // Title
                Text(
                    text = week.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(MindTagSpacing.md))

                // Progress bar + count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                ) {
                    LinearProgressIndicator(
                        progress = { week.progress },
                        modifier = Modifier
                            .width(96.dp)
                            .height(4.dp),
                        color = progressColor,
                        trackColor = MindTagColors.QuizProgressTrack,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        text = "$completedCount/$totalCount Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = MindTagColors.TextSecondary,
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MindTagColors.TextSecondary,
                modifier = Modifier.size(24.dp),
            )
        }

        // Expanded task list
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MindTagSpacing.xl,
                        end = MindTagSpacing.xl,
                        bottom = MindTagSpacing.xl,
                    ),
                verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
            ) {
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MindTagColors.Divider),
                )

                Spacer(modifier = Modifier.height(MindTagSpacing.xs))

                week.tasks.forEach { task ->
                    TaskRow(
                        task = task,
                        onToggle = { onToggleTask(task.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: PlannerContract.PlannerTask,
    onToggle: () -> Unit,
) {
    val subjectColor = parseHexColor(task.subjectColorHex)

    val (typeLabel, typeTextColor) = when (task.type) {
        PlannerContract.PlannerTaskType.LECTURE -> "LECTURE" to MindTagColors.Info
        PlannerContract.PlannerTaskType.READING -> "READING" to MindTagColors.AccentPurple
        PlannerContract.PlannerTaskType.QUIZ -> "QUIZ" to MindTagColors.Warning
        PlannerContract.PlannerTaskType.ASSIGNMENT -> "ASSIGNMENT" to MindTagColors.Success
    }
    val typeBgColor = typeTextColor.copy(alpha = 0.15f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.md)
            .background(Color.Black.copy(alpha = 0.2f))
            .clickable { onToggle() }
            .padding(MindTagSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
    ) {
        // Checkbox circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(MindTagShapes.full)
                .background(
                    if (task.isCompleted) MindTagColors.Success
                    else MindTagColors.QuizProgressTrack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        // Task info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (task.isCompleted) MindTagColors.TextSecondary else Color.White,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.xs))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
            ) {
                // Subject color dot + name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xs),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(MindTagShapes.full)
                            .background(subjectColor),
                    )
                    Text(
                        text = task.subjectName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MindTagColors.TextSecondary,
                    )
                }
            }
        }

        // Type badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(typeBgColor)
                .padding(horizontal = MindTagSpacing.md, vertical = MindTagSpacing.xxs),
        ) {
            Text(
                text = typeLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                ),
                color = typeTextColor,
            )
        }
    }
}

private fun parseHexColor(hex: String): Color {
    val sanitized = hex.removePrefix("#")
    return try {
        val colorLong = sanitized.toLong(16)
        Color(colorLong or 0xFF000000)
    } catch (_: Exception) {
        MindTagColors.Primary
    }
}
