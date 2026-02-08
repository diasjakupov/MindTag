package io.diasjakupov.mindtag.feature.study.presentation.results

data class ResultsState(
    val scorePercent: Int = 0,
    val feedbackMessage: String = "",
    val feedbackSubtext: String = "",
    val timeSpent: String = "",
    val streak: Int = 0,
    val xpEarned: Int = 0,
    val answers: List<AnswerDetailUi> = emptyList(),
    val expandedAnswerId: String? = null,
    val isLoading: Boolean = true,
)

data class AnswerDetailUi(
    val cardId: String,
    val questionText: String,
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val aiInsight: String? = null,
)

sealed interface ResultsIntent {
    data class ToggleAnswer(val cardId: String) : ResultsIntent
    data object TapReviewNotes : ResultsIntent
    data object TapClose : ResultsIntent
}

sealed interface ResultsEffect {
    data object NavigateToLibrary : ResultsEffect
    data object NavigateBack : ResultsEffect
}
