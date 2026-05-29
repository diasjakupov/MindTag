package io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622, name = "Quiz attempt — option selected")
@Composable
private fun BackendQuizAttemptContentPreview() {
    MindTagTheme {
        BackendQuizAttemptContent(
            state = BackendQuizAttemptState(
                quizId = 1L,
                attemptId = 100L,
                noteTitleSnapshot = "Cell Division and Mitosis",
                currentIndex = 1,
                totalQuestions = 5,
                questionText = "During which phase of mitosis do sister chromatids separate and move toward opposite poles of the cell?",
                options = listOf(
                    QuizOptionUi(label = "A", text = "Prophase", isSelected = false),
                    QuizOptionUi(label = "B", text = "Metaphase", isSelected = false),
                    QuizOptionUi(label = "C", text = "Anaphase", isSelected = true),
                    QuizOptionUi(label = "D", text = "Telophase", isSelected = false),
                ),
                selectedAnswer = "C",
                questionIds = listOf(1L, 2L, 3L, 4L, 5L),
                isLoading = false,
                isSubmitting = false,
                errorMessage = null,
            ),
            onIntent = {},
        )
    }
}
