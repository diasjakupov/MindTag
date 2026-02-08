package io.diasjakupov.mindtag.feature.planner.presentation

import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.Effect
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.Intent
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.PlannerTask
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.PlannerTaskType
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.State
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.WeekData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PlannerViewModel(
    private val db: MindTagDatabase,
) : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "PlannerVM"

    init {
        observeTasks()
    }

    private fun observeTasks() {
        db.plannerTaskEntityQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                val weeks = entities
                    .groupBy { it.week_id }
                    .map { (weekId, tasks) ->
                        val first = tasks.first()
                        val completedCount = tasks.count { it.is_completed == 1L }
                        val progress = if (tasks.isEmpty()) 0f
                            else completedCount.toFloat() / tasks.size
                        WeekData(
                            id = weekId,
                            weekNumber = first.week_number.toInt(),
                            title = first.week_title,
                            dateRange = first.week_date_range,
                            progress = progress,
                            isCurrentWeek = first.is_current_week == 1L,
                            tasks = tasks.map { e ->
                                PlannerTask(
                                    id = e.id,
                                    title = e.title,
                                    subjectName = e.subject_name,
                                    subjectColorHex = e.subject_color_hex,
                                    type = PlannerTaskType.valueOf(e.type),
                                    isCompleted = e.is_completed == 1L,
                                )
                            },
                        )
                    }
                    .sortedBy { it.weekNumber }

                val allTasks = weeks.flatMap { it.tasks }
                val overallProgress = if (allTasks.isEmpty()) 0f
                    else allTasks.count { it.isCompleted }.toFloat() / allTasks.size

                val currentWeekId = weeks.firstOrNull { it.isCurrentWeek }?.id

                updateState {
                    copy(
                        isLoading = false,
                        weeks = weeks,
                        overallProgress = overallProgress,
                        expandedWeekId = if (expandedWeekId == null && isLoading) currentWeekId else expandedWeekId,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is Intent.SwitchView -> updateState { copy(viewMode = intent.mode) }
            is Intent.ToggleWeek -> {
                val newExpanded = if (state.value.expandedWeekId == intent.weekId) null else intent.weekId
                Logger.d(tag, "ToggleWeek: ${intent.weekId} -> expanded=$newExpanded")
                updateState { copy(expandedWeekId = newExpanded) }
            }
            is Intent.ToggleTask -> {
                Logger.d(tag, "ToggleTask: ${intent.taskId}")
                viewModelScope.launch(Dispatchers.IO) {
                    db.plannerTaskEntityQueries.toggleCompleted(intent.taskId)
                }
            }
        }
    }
}
