package io.diasjakupov.mindtag.feature.planner.presentation

object PlannerContract {
    data class State(
        val isLoading: Boolean = true,
        val viewMode: ViewMode = ViewMode.LIST,
        val weeks: List<WeekData> = emptyList(),
        val expandedWeekId: String? = null,
        val overallProgress: Float = 0f,
    )

    enum class ViewMode { CALENDAR, LIST }

    data class WeekData(
        val id: String,
        val weekNumber: Int,
        val title: String,
        val dateRange: String,
        val progress: Float,
        val tasks: List<PlannerTask>,
        val isCurrentWeek: Boolean,
    )

    data class PlannerTask(
        val id: String,
        val title: String,
        val subjectName: String,
        val subjectColorHex: String,
        val type: PlannerTaskType,
        val isCompleted: Boolean,
    )

    enum class PlannerTaskType { LECTURE, READING, QUIZ, ASSIGNMENT }

    sealed interface Intent {
        data class SwitchView(val mode: ViewMode) : Intent
        data class ToggleWeek(val weekId: String) : Intent
        data class ToggleTask(val taskId: String) : Intent
    }

    sealed interface Effect
}
