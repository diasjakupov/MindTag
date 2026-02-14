package io.diasjakupov.mindtag.feature.study.presentation.hub

data class StudyHubState(
    val subjects: List<SubjectUi> = emptyList(),
    val selectedSubjectId: String? = null,
    val questionCount: Int = 10,
    val timerEnabled: Boolean = false,
    val timerMinutes: Int = 15,
    val cardsDueCount: Int = 0,
    val isCreatingSession: Boolean = false,
    val errorMessage: String? = null,
)

data class SubjectUi(
    val id: String,
    val name: String,
)

sealed interface StudyHubIntent {
    data class SelectSubject(val subjectId: String?) : StudyHubIntent
    data class SelectQuestionCount(val count: Int) : StudyHubIntent
    data class ToggleTimer(val enabled: Boolean) : StudyHubIntent
    data class SelectTimerDuration(val minutes: Int) : StudyHubIntent
    data object StartQuiz : StudyHubIntent
    data object DismissError : StudyHubIntent
}

sealed interface StudyHubEffect {
    data class NavigateToQuiz(val sessionId: String) : StudyHubEffect
}
