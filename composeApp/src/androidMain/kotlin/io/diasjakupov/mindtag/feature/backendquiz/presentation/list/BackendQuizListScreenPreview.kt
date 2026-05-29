package io.diasjakupov.mindtag.feature.backendquiz.presentation.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus

@Preview(showBackground = true, backgroundColor = 0xFF101622, name = "Quiz list — scheduled reviews")
@Composable
private fun BackendQuizListScreenPreview() {
    MindTagTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        BackendQuizListScreenContent(
            state = BackendQuizListState(
                quizzes = listOf(
                    QuizListItemUi(
                        quizId = 1L,
                        noteTitleSnapshot = "Cell Division and Mitosis",
                        status = QuizStatus.READY,
                        questionCount = 5,
                        createdAt = "Apr 24, 2026",
                    ),
                    QuizListItemUi(
                        quizId = 2L,
                        noteTitleSnapshot = "Photosynthesis: Light-Dependent Reactions",
                        status = QuizStatus.READY,
                        questionCount = 8,
                        createdAt = "Apr 22, 2026",
                    ),
                    QuizListItemUi(
                        quizId = 3L,
                        noteTitleSnapshot = "Newton's Second Law of Motion",
                        status = QuizStatus.PENDING,
                        questionCount = 0,
                        createdAt = "Apr 26, 2026",
                    ),
                    QuizListItemUi(
                        quizId = 4L,
                        noteTitleSnapshot = "Binary Search Algorithm",
                        status = QuizStatus.READY,
                        questionCount = 6,
                        createdAt = "Apr 20, 2026",
                    ),
                    QuizListItemUi(
                        quizId = 5L,
                        noteTitleSnapshot = "Acids, Bases, and pH",
                        status = QuizStatus.ERROR,
                        questionCount = 0,
                        createdAt = "Apr 18, 2026",
                    ),
                    QuizListItemUi(
                        quizId = 6L,
                        noteTitleSnapshot = "Diffusion and Osmosis",
                        status = QuizStatus.READY,
                        questionCount = 5,
                        createdAt = "Apr 15, 2026",
                    ),
                ),
                isLoading = false,
                errorMessage = null,
                noteId = null,
            ),
            onIntent = {},
            snackbarHostState = snackbarHostState,
        )
    }
}
