package io.diasjakupov.mindtag.feature.study.presentation.hub

data class StudyHubState(
    val isCreatingSession: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface StudyHubIntent {
    data object TapStartQuiz : StudyHubIntent
    data object TapBeginExam : StudyHubIntent
    data object DismissError : StudyHubIntent
}

sealed interface StudyHubEffect {
    data class NavigateToQuiz(val sessionId: String) : StudyHubEffect
}
