package io.diasjakupov.mindtag.feature.study.presentation.results

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun ResultsScreenPreview() {
    MindTagTheme {
        ResultsScreenContent(
            state = ResultsState(
                scorePercent = 78,
                feedbackMessage = "Great Progress!",
                feedbackSubtext = "You're building a strong foundation. Review the questions you missed to reinforce your understanding.",
                timeSpent = "4m 32s",
                isLoading = false,
                answers = listOf(
                    AnswerDetailUi(
                        cardId = "1",
                        questionText = "What is the primary function of mitochondria?",
                        isCorrect = true,
                        userAnswer = "ATP production through cellular respiration",
                        correctAnswer = "ATP production through cellular respiration",
                    ),
                    AnswerDetailUi(
                        cardId = "2",
                        questionText = "Which phase of mitosis involves chromosome alignment at the cell equator?",
                        isCorrect = false,
                        userAnswer = "Anaphase",
                        correctAnswer = "Metaphase",
                        aiInsight = "Metaphase is characterized by chromosomes lining up along the metaphase plate. Anaphase is when sister chromatids separate and move to opposite poles.",
                    ),
                    AnswerDetailUi(
                        cardId = "3",
                        questionText = "What molecule carries genetic information?",
                        isCorrect = true,
                        userAnswer = "DNA",
                        correctAnswer = "DNA",
                    ),
                ),
                expandedAnswerId = "2",
            ),
            onIntent = {},
        )
    }
}
