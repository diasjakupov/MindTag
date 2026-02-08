package io.diasjakupov.mindtag.feature.home.domain.model

data class DashboardData(
    val userName: String,
    val totalNotesCount: Int,
    val totalReviewsDue: Int,
    val currentStreak: Int,
    val reviewCards: List<ReviewCard>,
    val upNextTasks: List<UpNextTask>,
)

data class UpNextTask(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: TaskType,
)

enum class TaskType { REVIEW, QUIZ, NOTE }
