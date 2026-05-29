package io.diasjakupov.mindtag.feature.backendquiz.presentation.list

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.hoverBorder
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BackendQuizListScreen(
    noteId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToAttempt: (quizId: Long, attemptId: Long) -> Unit,
) {
    val viewModel: BackendQuizListViewModel = koinViewModel(parameters = { parametersOf(noteId) })
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackendQuizListEffect.NavigateBack -> onNavigateBack()
                is BackendQuizListEffect.NavigateToAttempt -> onNavigateToAttempt(effect.quizId, effect.attemptId)
                is BackendQuizListEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    BackendQuizListScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun BackendQuizListScreenContent(
    state: BackendQuizListState,
    onIntent: (BackendQuizListIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val windowSizeClass = LocalWindowSizeClass.current

    Scaffold(
        containerColor = MindTagColors.BackgroundDark,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MindTagColors.CardDark,
                    contentColor = MindTagColors.Error,
                )
            }
        },
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MindTagSpacing.topAppBarHeight)
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onIntent(BackendQuizListIntent.NavigateBack) }) {
                Icon(
                    imageVector = MindTagIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Text(
                text = "Quizzes",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            // Placeholder to balance the back button
            Spacer(modifier = Modifier.size(MindTagSpacing.iconButtonSize))
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MindTagColors.Primary,
                    )
                }
            }
            state.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MindTagSpacing.screenHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindTagColors.Error,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            state.quizzes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MindTagSpacing.screenHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No quizzes yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindTagColors.TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> {
                val columns = when (windowSizeClass) {
                    WindowSizeClass.Compact -> 1
                    WindowSizeClass.Medium -> 2
                    WindowSizeClass.Expanded -> 3
                }

                if (columns == 1) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = MindTagSpacing.screenHorizontalPadding,
                            vertical = MindTagSpacing.md,
                        ),
                    ) {
                        items(state.quizzes, key = { it.quizId }) { quiz ->
                            QuizListItem(
                                item = quiz,
                                onTap = { onIntent(BackendQuizListIntent.TapQuiz(quiz.quizId)) },
                                onDelete = { onIntent(BackendQuizListIntent.DeleteQuiz(quiz.quizId)) },
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = MindTagSpacing.screenHorizontalPadding,
                            vertical = MindTagSpacing.md,
                        ),
                    ) {
                        items(state.quizzes, key = { it.quizId }) { quiz ->
                            QuizListItem(
                                item = quiz,
                                onTap = { onIntent(BackendQuizListIntent.TapQuiz(quiz.quizId)) },
                                onDelete = { onIntent(BackendQuizListIntent.DeleteQuiz(quiz.quizId)) },
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
private fun QuizListItem(
    item: QuizListItemUi,
    onTap: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.lg)
            .background(MindTagColors.CardDark)
            .hoverBorder()
            .clickable(onClick = onTap)
            .padding(horizontal = MindTagSpacing.xl, vertical = MindTagSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.noteTitleSnapshot,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MindTagColors.TextSlate300,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.xs))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
            ) {
                QuizStatusChip(status = item.status)
                if (item.status == QuizStatus.READY) {
                    Text(
                        text = "${item.questionCount} questions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MindTagColors.TextSecondary,
                    )
                }
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = MindTagIcons.Delete,
                contentDescription = "Delete quiz",
                tint = MindTagColors.TextSlate500,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun QuizStatusChip(status: QuizStatus) {
    val (label, textColor, bgColor) = when (status) {
        QuizStatus.READY -> Triple("Ready", MindTagColors.Success, MindTagColors.SuccessBg)
        QuizStatus.PENDING -> Triple("Generating...", MindTagColors.ProgressYellow, Color(0x1AEAB308))
        QuizStatus.ERROR -> Triple("Error", MindTagColors.Error, MindTagColors.ErrorBg)
    }
    Box(
        modifier = Modifier
            .clip(MindTagShapes.full)
            .background(bgColor)
            .padding(horizontal = MindTagSpacing.md, vertical = MindTagSpacing.xxs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}
