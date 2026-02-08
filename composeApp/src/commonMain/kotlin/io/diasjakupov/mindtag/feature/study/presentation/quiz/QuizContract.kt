package io.diasjakupov.mindtag.feature.study.presentation.quiz

data class QuizState(
    val sessionType: String = "QUICK_QUIZ",
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val progressPercent: Float = 0f,
    val currentQuestion: String = "",
    val currentOptions: List<QuizOptionUi> = emptyList(),
    val selectedOptionId: String? = null,
    val isAnswerSubmitted: Boolean = false,
    val timeRemainingSeconds: Int? = null,
    val timeRemainingFormatted: String = "",
    val isLoading: Boolean = true,
    val isLastQuestion: Boolean = false,
)

data class QuizOptionUi(
    val id: String,
    val text: String,
)

sealed interface QuizIntent {
    data class SelectOption(val optionId: String) : QuizIntent
    data object TapNext : QuizIntent
    data object TapExit : QuizIntent
    data object TimerTick : QuizIntent
}

sealed interface QuizEffect {
    data class NavigateToResults(val sessionId: String) : QuizEffect
    data object NavigateBack : QuizEffect
    data object ShowExitConfirmation : QuizEffect
}
