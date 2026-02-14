package io.diasjakupov.mindtag.feature.study.presentation.quiz

import io.diasjakupov.mindtag.feature.study.domain.model.CardType

data class QuizState(
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
    val cardType: CardType = CardType.MULTIPLE_CHOICE,
    val isFlipped: Boolean = false,
    val flashcardAnswer: String = "",
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
    data object FlipCard : QuizIntent
    data class SelfAssess(val quality: Int) : QuizIntent
}

sealed interface QuizEffect {
    data class NavigateToResults(val sessionId: String) : QuizEffect
    data object NavigateBack : QuizEffect
    data object ShowExitConfirmation : QuizEffect
}
