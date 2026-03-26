package io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt

data class QuizOptionUi(
    val label: String,      // "A", "B", "C", "D"
    val text: String,
    val isSelected: Boolean,
)

data class BackendQuizAttemptState(
    val quizId: Long = 0,
    val attemptId: Long = 0,
    val noteTitleSnapshot: String = "",
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val questionText: String = "",
    val options: List<QuizOptionUi> = emptyList(),
    val selectedAnswer: String? = null,   // "A", "B", "C", "D"
    val answers: Map<Long, String> = emptyMap(), // questionId -> answer
    val questionIds: List<Long> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface BackendQuizAttemptIntent {
    data class SelectOption(val label: String) : BackendQuizAttemptIntent
    data object TapNext : BackendQuizAttemptIntent
    data object TapPrevious : BackendQuizAttemptIntent
    data object TapSubmit : BackendQuizAttemptIntent
    data object TapBack : BackendQuizAttemptIntent
}

sealed interface BackendQuizAttemptEffect {
    data class NavigateToResults(val quizId: Long, val attemptId: Long) : BackendQuizAttemptEffect
    data object NavigateBack : BackendQuizAttemptEffect
}
