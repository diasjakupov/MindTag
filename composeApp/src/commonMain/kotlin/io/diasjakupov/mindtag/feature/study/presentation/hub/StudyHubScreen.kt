package io.diasjakupov.mindtag.feature.study.presentation.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    StudyHubScreenContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun StudyHubScreenContent(
    state: StudyHubState,
    onIntent: (StudyHubIntent) -> Unit,
) {
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
                text = "Study",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }

        // Cards due badge
        if (state.cardsDueCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MindTagShapes.md)
                    .background(MindTagColors.Primary.copy(alpha = 0.1f))
                    .border(1.dp, MindTagColors.Primary.copy(alpha = 0.2f), MindTagShapes.md)
                    .padding(MindTagSpacing.lg),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                ) {
                    Icon(
                        imageVector = MindTagIcons.MoreHoriz,
                        contentDescription = null,
                        tint = MindTagColors.Primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "${state.cardsDueCount} cards due for review",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MindTagColors.Primary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(MindTagSpacing.xl))
        }

        // Error banner
        if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MindTagShapes.md)
                    .background(MindTagColors.ErrorBg)
                    .clickable { onIntent(StudyHubIntent.DismissError) }
                    .padding(MindTagSpacing.lg),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                ) {
                    Icon(
                        imageVector = MindTagIcons.Close,
                        contentDescription = "Dismiss",
                        tint = MindTagColors.Error,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = state.errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindTagColors.Error,
                    )
                }
            }
            Spacer(modifier = Modifier.height(MindTagSpacing.xl))
        }

        // Subject filter
        MindTagCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = MindTagSpacing.xxl,
        ) {
            Text(
                text = "Subject",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.lg))

            SubjectChips(
                subjects = state.subjects,
                selectedSubjectId = state.selectedSubjectId,
                onSelect = { onIntent(StudyHubIntent.SelectSubject(it)) },
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Question count
        MindTagCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = MindTagSpacing.xxl,
        ) {
            Text(
                text = "Questions",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.lg))

            CountChips(
                counts = listOf(5, 10, 15, 20),
                selected = state.questionCount,
                onSelect = { onIntent(StudyHubIntent.SelectQuestionCount(it)) },
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Timer toggle
        MindTagCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = MindTagSpacing.xxl,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Timer",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Switch(
                    checked = state.timerEnabled,
                    onCheckedChange = { onIntent(StudyHubIntent.ToggleTimer(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MindTagColors.Primary,
                        uncheckedThumbColor = MindTagColors.TextSecondary,
                        uncheckedTrackColor = MindTagColors.SurfaceDarkAlt,
                    ),
                )
            }

            if (state.timerEnabled) {
                Spacer(modifier = Modifier.height(MindTagSpacing.lg))

                CountChips(
                    counts = listOf(5, 10, 15, 30),
                    selected = state.timerMinutes,
                    onSelect = { onIntent(StudyHubIntent.SelectTimerDuration(it)) },
                    suffix = "min",
                )
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        // Start button
        if (state.isCreatingSession) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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
                text = "Start Quiz",
                onClick = { onIntent(StudyHubIntent.StartQuiz) },
                variant = MindTagButtonVariant.PrimaryMedium,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubjectChips(
    subjects: List<SubjectUi>,
    selectedSubjectId: String?,
    onSelect: (String?) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
    ) {
        // "All" chip
        SelectableChip(
            text = "All",
            isSelected = selectedSubjectId == null,
            onClick = { onSelect(null) },
        )
        subjects.forEach { subject ->
            SelectableChip(
                text = subject.name,
                isSelected = selectedSubjectId == subject.id,
                onClick = { onSelect(subject.id) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CountChips(
    counts: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    suffix: String = "",
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
    ) {
        counts.forEach { count ->
            val label = if (suffix.isNotEmpty()) "$count $suffix" else "$count"
            SelectableChip(
                text = label,
                isSelected = selected == count,
                onClick = { onSelect(count) },
            )
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) MindTagColors.Primary else MindTagColors.SurfaceDarkAlt
    val textColor = if (isSelected) Color.White else MindTagColors.TextSecondary
    val borderColor = if (isSelected) MindTagColors.Primary else MindTagColors.BorderMedium

    Box(
        modifier = Modifier
            .clip(MindTagShapes.full)
            .background(bgColor.copy(alpha = if (isSelected) 1f else 0.5f))
            .border(1.dp, borderColor, MindTagShapes.full)
            .clickable(onClick = onClick)
            .padding(horizontal = MindTagSpacing.xl, vertical = MindTagSpacing.md),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
    }
}
