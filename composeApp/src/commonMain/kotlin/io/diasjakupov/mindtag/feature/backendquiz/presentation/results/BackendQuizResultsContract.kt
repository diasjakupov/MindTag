package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

data class QuestionResultUi(
    val questionId: Long,
    val questionText: String,
    val options: List<String>,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val explanation: String?,
    val isExpanded: Boolean = false,
)

data class BackendQuizResultsState(
    val noteTitleSnapshot: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val feedbackMessage: String = "",
    val questionResults: List<QuestionResultUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Save to Study
    val showSaveDialog: Boolean = false,
    val isSaving: Boolean = false,
    val hasSaved: Boolean = false,
    val subjectId: String = "",
    val noteId: Long? = null,
)

sealed interface BackendQuizResultsIntent {
    data class ToggleQuestion(val questionId: Long) : BackendQuizResultsIntent
    data object TapClose : BackendQuizResultsIntent
    data object TapRetry : BackendQuizResultsIntent
    data object TapSaveToStudy : BackendQuizResultsIntent
    data class ConfirmSave(val saveAll: Boolean) : BackendQuizResultsIntent
    data object DismissSaveDialog : BackendQuizResultsIntent
}

sealed interface BackendQuizResultsEffect {
    data object NavigateBack : BackendQuizResultsEffect
    data class NavigateToQuizList(val noteId: Long?) : BackendQuizResultsEffect
    data class ShowSnackbar(val message: String) : BackendQuizResultsEffect
}
