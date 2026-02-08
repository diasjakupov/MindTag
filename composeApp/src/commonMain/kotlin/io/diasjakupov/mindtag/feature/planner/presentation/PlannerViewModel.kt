package io.diasjakupov.mindtag.feature.planner.presentation

import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.Effect
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.Intent
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.PlannerTask
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.PlannerTaskType
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.State
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.WeekData

class PlannerViewModel : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "PlannerVM"

    init {
        updateState { copy(weeks = buildMockWeeks(), expandedWeekId = "week_4") }
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
                updateState {
                    copy(
                        weeks = weeks.map { week ->
                            val updatedTasks = week.tasks.map { task ->
                                if (task.id == intent.taskId) task.copy(isCompleted = !task.isCompleted) else task
                            }
                            val completedCount = updatedTasks.count { it.isCompleted }
                            val newProgress = if (updatedTasks.isEmpty()) 0f
                                else completedCount.toFloat() / updatedTasks.size
                            week.copy(tasks = updatedTasks, progress = newProgress)
                        },
                        overallProgress = run {
                            val allTasks = weeks.flatMap { it.tasks }.map { task ->
                                if (task.id == intent.taskId) !task.isCompleted else task.isCompleted
                            }
                            if (allTasks.isEmpty()) 0f
                            else allTasks.count { it }.toFloat() / allTasks.size
                        },
                    )
                }
                Logger.d(tag, "ToggleTask: overall progress=${state.value.overallProgress}")
            }
        }
    }

    private fun buildMockWeeks(): List<WeekData> = listOf(
        WeekData(
            id = "week_1",
            weekNumber = 1,
            title = "Introduction & Study Methods",
            dateRange = "Jan 6 - Jan 12",
            progress = 1.0f,
            isCurrentWeek = false,
            tasks = listOf(
                PlannerTask("t1_1", "Intro to Cognitive Science", "Psychology 101", "#3B82F6", PlannerTaskType.LECTURE, true),
                PlannerTask("t1_2", "Chapter 1: Learning Fundamentals", "Psychology 101", "#3B82F6", PlannerTaskType.READING, true),
                PlannerTask("t1_3", "Supply & Demand Basics", "Economics 101", "#F97316", PlannerTaskType.LECTURE, true),
                PlannerTask("t1_4", "Week 1 Review Quiz", "Psychology 101", "#3B82F6", PlannerTaskType.QUIZ, true),
            ),
        ),
        WeekData(
            id = "week_2",
            weekNumber = 2,
            title = "Core Concepts Deep Dive",
            dateRange = "Jan 13 - Jan 19",
            progress = 0.8f,
            isCurrentWeek = false,
            tasks = listOf(
                PlannerTask("t2_1", "Memory & Retention Models", "Psychology 101", "#3B82F6", PlannerTaskType.LECTURE, true),
                PlannerTask("t2_2", "Organic Compound Structures", "Chemistry 201", "#22C55E", PlannerTaskType.LECTURE, true),
                PlannerTask("t2_3", "Market Equilibrium Analysis", "Economics 101", "#F97316", PlannerTaskType.READING, true),
                PlannerTask("t2_4", "Lab Report: Molecules", "Chemistry 201", "#22C55E", PlannerTaskType.ASSIGNMENT, false),
                PlannerTask("t2_5", "Concepts Quiz", "Psychology 101", "#3B82F6", PlannerTaskType.QUIZ, true),
            ),
        ),
        WeekData(
            id = "week_3",
            weekNumber = 3,
            title = "Applied Frameworks",
            dateRange = "Jan 20 - Jan 26",
            progress = 0.75f,
            isCurrentWeek = false,
            tasks = listOf(
                PlannerTask("t3_1", "Behavioral Economics Intro", "Economics 101", "#F97316", PlannerTaskType.LECTURE, true),
                PlannerTask("t3_2", "Chapter 4: Decision Making", "Psychology 101", "#3B82F6", PlannerTaskType.READING, true),
                PlannerTask("t3_3", "Reaction Kinetics Lab", "Chemistry 201", "#22C55E", PlannerTaskType.ASSIGNMENT, true),
                PlannerTask("t3_4", "Applied Frameworks Quiz", "Economics 101", "#F97316", PlannerTaskType.QUIZ, false),
            ),
        ),
        WeekData(
            id = "week_4",
            weekNumber = 4,
            title = "Midterm Preparation",
            dateRange = "Jan 27 - Feb 2",
            progress = 0.5f,
            isCurrentWeek = true,
            tasks = listOf(
                PlannerTask("t4_1", "Review: Psych Chapters 1-4", "Psychology 101", "#3B82F6", PlannerTaskType.READING, true),
                PlannerTask("t4_2", "Thermodynamics Lecture", "Chemistry 201", "#22C55E", PlannerTaskType.LECTURE, true),
                PlannerTask("t4_3", "Macro vs Micro Economics", "Economics 101", "#F97316", PlannerTaskType.LECTURE, false),
                PlannerTask("t4_4", "Practice Midterm Exam", "Psychology 101", "#3B82F6", PlannerTaskType.QUIZ, false),
            ),
        ),
        WeekData(
            id = "week_5",
            weekNumber = 5,
            title = "Advanced Topics",
            dateRange = "Feb 3 - Feb 9",
            progress = 0f,
            isCurrentWeek = false,
            tasks = listOf(
                PlannerTask("t5_1", "Neuroplasticity & Learning", "Psychology 101", "#3B82F6", PlannerTaskType.LECTURE, false),
                PlannerTask("t5_2", "Electrochemistry Basics", "Chemistry 201", "#22C55E", PlannerTaskType.LECTURE, false),
                PlannerTask("t5_3", "International Trade Theory", "Economics 101", "#F97316", PlannerTaskType.READING, false),
                PlannerTask("t5_4", "Chem Lab: Electrochemistry", "Chemistry 201", "#22C55E", PlannerTaskType.ASSIGNMENT, false),
                PlannerTask("t5_5", "Advanced Topics Quiz", "Psychology 101", "#3B82F6", PlannerTaskType.QUIZ, false),
            ),
        ),
        WeekData(
            id = "week_6",
            weekNumber = 6,
            title = "Final Review & Synthesis",
            dateRange = "Feb 10 - Feb 16",
            progress = 0f,
            isCurrentWeek = false,
            tasks = listOf(
                PlannerTask("t6_1", "Comprehensive Review Session", "Psychology 101", "#3B82F6", PlannerTaskType.LECTURE, false),
                PlannerTask("t6_2", "Final Lab Submission", "Chemistry 201", "#22C55E", PlannerTaskType.ASSIGNMENT, false),
                PlannerTask("t6_3", "Economics Policy Analysis", "Economics 101", "#F97316", PlannerTaskType.READING, false),
            ),
        ),
    )
}
