package io.diasjakupov.mindtag.feature.home.presentation

import io.diasjakupov.mindtag.feature.home.domain.model.ReviewCard
import io.diasjakupov.mindtag.feature.home.domain.model.UpNextTask

object HomeContract {
    data class State(
        val userName: String = "",
        val totalNotesCount: Int = 0,
        val currentStreak: Int = 0,
        val reviewCards: List<ReviewCard> = emptyList(),
        val upNextTasks: List<UpNextTask> = emptyList(),
        val isLoading: Boolean = true,
    )

    sealed interface Intent {
        data class TapReviewCard(val noteId: Long) : Intent
        data class TapTask(val taskId: String) : Intent
        data object Refresh : Intent
    }

    sealed interface Effect {
        data class NavigateToNote(val noteId: Long) : Effect
        data class NavigateToQuiz(val sessionId: String) : Effect
    }
}
