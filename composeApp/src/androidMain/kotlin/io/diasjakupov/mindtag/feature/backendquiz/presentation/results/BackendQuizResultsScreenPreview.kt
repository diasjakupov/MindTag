package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622, heightDp = 1100, name = "Quiz results — post-attempt summary")
@Composable
private fun BackendQuizResultsContentSummaryPreview() {
    MindTagTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        BackendQuizResultsContent(
            state = BackendQuizResultsState(
                noteTitleSnapshot = "Cell Division and Mitosis",
                score = 80,
                totalQuestions = 5,
                correctAnswers = 4,
                feedbackMessage = "Strong grasp of the phases — one slip on chromatid separation. Spaced-repetition schedule updated.",
                questionResults = listOf(
                    QuestionResultUi(
                        questionId = 1L,
                        questionText = "Which phase comes first in mitosis?",
                        options = listOf("Prophase", "Metaphase", "Anaphase", "Telophase"),
                        userAnswer = "A",
                        correctAnswer = "A",
                        isCorrect = true,
                        explanation = "Prophase is the first phase: chromatin condenses into chromosomes and the nuclear envelope begins to break down.",
                    ),
                    QuestionResultUi(
                        questionId = 2L,
                        questionText = "During which phase of mitosis do sister chromatids separate and move toward opposite poles of the cell?",
                        options = listOf("Prophase", "Metaphase", "Anaphase", "Telophase"),
                        userAnswer = "B",
                        correctAnswer = "C",
                        isCorrect = false,
                        explanation = "Sister chromatids separate during anaphase. Metaphase is when chromosomes align along the metaphase plate — they have not yet separated.",
                    ),
                    QuestionResultUi(
                        questionId = 3L,
                        questionText = "What structure pulls chromosomes apart during mitosis?",
                        options = listOf("Nucleolus", "Spindle fibers", "Ribosomes", "Golgi apparatus"),
                        userAnswer = "B",
                        correctAnswer = "B",
                        isCorrect = true,
                        explanation = "Spindle fibers attach to the centromeres of chromosomes and pull sister chromatids to opposite poles.",
                    ),
                    QuestionResultUi(
                        questionId = 4L,
                        questionText = "In which phase does the nuclear envelope reform around each set of chromosomes?",
                        options = listOf("Prophase", "Metaphase", "Anaphase", "Telophase"),
                        userAnswer = "D",
                        correctAnswer = "D",
                        isCorrect = true,
                        explanation = "Telophase: nuclear envelopes reform and chromosomes begin to decondense back into chromatin.",
                    ),
                    QuestionResultUi(
                        questionId = 5L,
                        questionText = "Mitosis produces how many daughter cells from one parent cell?",
                        options = listOf("One", "Two", "Three", "Four"),
                        userAnswer = "B",
                        correctAnswer = "B",
                        isCorrect = true,
                        explanation = "Mitosis produces two genetically identical daughter cells from a single parent cell.",
                    ),
                ),
                isLoading = false,
                errorMessage = null,
                showSaveDialog = false,
                isSaving = false,
                hasSaved = false,
                subjectId = "1",
                noteId = 50L,
            ),
            onIntent = {},
            snackbarHostState = snackbarHostState,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, heightDp = 1300, name = "Quiz results — score + expanded card")
@Composable
private fun BackendQuizResultsContentPreview() {
    MindTagTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        BackendQuizResultsContent(
            state = BackendQuizResultsState(
                noteTitleSnapshot = "Cell Division and Mitosis",
                score = 80,
                totalQuestions = 5,
                correctAnswers = 4,
                feedbackMessage = "Strong grasp of the phases — one slip on chromatid separation.",
                questionResults = listOf(
                    QuestionResultUi(
                        questionId = 1L,
                        questionText = "Which phase comes first in mitosis?",
                        options = listOf("Prophase", "Metaphase", "Anaphase", "Telophase"),
                        userAnswer = "A",
                        correctAnswer = "A",
                        isCorrect = true,
                        explanation = "Prophase is the first phase: chromatin condenses into chromosomes and the nuclear envelope begins to break down.",
                        isExpanded = false,
                    ),
                    QuestionResultUi(
                        questionId = 2L,
                        questionText = "During which phase of mitosis do sister chromatids separate and move toward opposite poles of the cell?",
                        options = listOf("Prophase", "Metaphase", "Anaphase", "Telophase"),
                        userAnswer = "B",
                        correctAnswer = "C",
                        isCorrect = false,
                        explanation = "Sister chromatids separate during anaphase. Metaphase is when the chromosomes align along the metaphase plate at the cell's equator — they have not yet separated.",
                        isExpanded = true,
                    ),
                    QuestionResultUi(
                        questionId = 3L,
                        questionText = "What structure pulls chromosomes apart during mitosis?",
                        options = listOf("Nucleolus", "Spindle fibers", "Ribosomes", "Golgi apparatus"),
                        userAnswer = "B",
                        correctAnswer = "B",
                        isCorrect = true,
                        explanation = "Spindle fibers attach to the centromeres of chromosomes and pull sister chromatids to opposite poles.",
                        isExpanded = false,
                    ),
                    QuestionResultUi(
                        questionId = 4L,
                        questionText = "In which phase does the nuclear envelope reform around each set of chromosomes?",
                        options = listOf("Prophase", "Metaphase", "Anaphase", "Telophase"),
                        userAnswer = "D",
                        correctAnswer = "D",
                        isCorrect = true,
                        explanation = "Telophase: nuclear envelopes reform and chromosomes begin to decondense back into chromatin.",
                        isExpanded = false,
                    ),
                    QuestionResultUi(
                        questionId = 5L,
                        questionText = "Mitosis produces how many daughter cells from one parent cell?",
                        options = listOf("One", "Two", "Three", "Four"),
                        userAnswer = "B",
                        correctAnswer = "B",
                        isCorrect = true,
                        explanation = "Mitosis produces two genetically identical daughter cells from a single parent cell.",
                        isExpanded = false,
                    ),
                ),
                isLoading = false,
                errorMessage = null,
                showSaveDialog = false,
                isSaving = false,
                hasSaved = false,
                subjectId = "1",
                noteId = 50L,
            ),
            onIntent = {},
            snackbarHostState = snackbarHostState,
        )
    }
}
