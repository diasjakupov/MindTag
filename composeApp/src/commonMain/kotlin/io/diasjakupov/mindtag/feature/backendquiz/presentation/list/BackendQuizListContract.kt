package io.diasjakupov.mindtag.feature.backendquiz.presentation.list

import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus

data class QuizListItemUi(
    val quizId: Long,
    val noteTitleSnapshot: String,
    val status: QuizStatus,
    val questionCount: Int,
    val createdAt: String,
)

data class BackendQuizListState(
    val quizzes: List<QuizListItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val noteId: Long? = null,
)

sealed interface BackendQuizListIntent {
    data class TapQuiz(val quizId: Long) : BackendQuizListIntent
    data class DeleteQuiz(val quizId: Long) : BackendQuizListIntent
    data object Refresh : BackendQuizListIntent
    data object NavigateBack : BackendQuizListIntent
}

sealed interface BackendQuizListEffect {
    data class NavigateToAttempt(val quizId: Long, val attemptId: Long) : BackendQuizListEffect
    data object NavigateBack : BackendQuizListEffect
    data class ShowError(val message: String) : BackendQuizListEffect
}
