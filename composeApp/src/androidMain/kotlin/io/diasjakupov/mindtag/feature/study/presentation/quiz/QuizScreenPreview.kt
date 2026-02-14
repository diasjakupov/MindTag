package io.diasjakupov.mindtag.feature.study.presentation.quiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme
import io.diasjakupov.mindtag.feature.study.domain.model.CardType

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun QuizScreenPreview() {
    MindTagTheme {
        QuizScreenContent(
            state = QuizState(
                currentQuestionIndex = 2,
                totalQuestions = 10,
                progressPercent = 30f,
                currentQuestion = "What is the primary function of mitochondria in eukaryotic cells?",
                currentOptions = listOf(
                    QuizOptionUi("a", "Protein synthesis"),
                    QuizOptionUi("b", "ATP production through cellular respiration"),
                    QuizOptionUi("c", "DNA replication"),
                    QuizOptionUi("d", "Cell division"),
                ),
                selectedOptionId = "b",
                isAnswerSubmitted = false,
                timeRemainingSeconds = 540,
                timeRemainingFormatted = "9:00",
                isLoading = false,
                isLastQuestion = false,
                cardType = CardType.MULTIPLE_CHOICE,
                isFlipped = false,
                flashcardAnswer = "",
            ),
            onIntent = {},
        )
    }
}
