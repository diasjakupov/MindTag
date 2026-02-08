package io.diasjakupov.mindtag.feature.planner.presentation

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelTest {

    private lateinit var db: MindTagDatabase
    private lateinit var viewModel: PlannerViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        db = MindTagDatabase(driver)

        // Seed planner tasks matching SeedData
        val q = db.plannerTaskEntityQueries

        // Week 1 — all completed
        q.insert("t1_1", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Intro to Cognitive Science", "Psychology 101", "#3B82F6", "LECTURE", 1)
        q.insert("t1_2", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Chapter 1: Learning Fundamentals", "Psychology 101", "#3B82F6", "READING", 1)
        q.insert("t1_3", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Supply & Demand Basics", "Economics 101", "#F97316", "LECTURE", 1)
        q.insert("t1_4", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Week 1 Review Quiz", "Psychology 101", "#3B82F6", "QUIZ", 1)

        // Week 2 — mostly completed
        q.insert("t2_1", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Memory & Retention Models", "Psychology 101", "#3B82F6", "LECTURE", 1)
        q.insert("t2_2", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Organic Compound Structures", "Chemistry 201", "#22C55E", "LECTURE", 1)
        q.insert("t2_3", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Market Equilibrium Analysis", "Economics 101", "#F97316", "READING", 1)
        q.insert("t2_4", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Lab Report: Molecules", "Chemistry 201", "#22C55E", "ASSIGNMENT", 0)
        q.insert("t2_5", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Concepts Quiz", "Psychology 101", "#3B82F6", "QUIZ", 1)

        // Week 3
        q.insert("t3_1", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Behavioral Economics Intro", "Economics 101", "#F97316", "LECTURE", 1)
        q.insert("t3_2", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Chapter 4: Decision Making", "Psychology 101", "#3B82F6", "READING", 1)
        q.insert("t3_3", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Reaction Kinetics Lab", "Chemistry 201", "#22C55E", "ASSIGNMENT", 1)
        q.insert("t3_4", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Applied Frameworks Quiz", "Economics 101", "#F97316", "QUIZ", 0)

        // Week 4 — current week, partially done
        q.insert("t4_1", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Review: Psych Chapters 1-4", "Psychology 101", "#3B82F6", "READING", 1)
        q.insert("t4_2", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Thermodynamics Lecture", "Chemistry 201", "#22C55E", "LECTURE", 1)
        q.insert("t4_3", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Macro vs Micro Economics", "Economics 101", "#F97316", "LECTURE", 0)
        q.insert("t4_4", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Practice Midterm Exam", "Psychology 101", "#3B82F6", "QUIZ", 0)

        // Week 5 — not started
        q.insert("t5_1", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Neuroplasticity & Learning", "Psychology 101", "#3B82F6", "LECTURE", 0)
        q.insert("t5_2", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Electrochemistry Basics", "Chemistry 201", "#22C55E", "LECTURE", 0)
        q.insert("t5_3", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "International Trade Theory", "Economics 101", "#F97316", "READING", 0)
        q.insert("t5_4", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Chem Lab: Electrochemistry", "Chemistry 201", "#22C55E", "ASSIGNMENT", 0)
        q.insert("t5_5", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Advanced Topics Quiz", "Psychology 101", "#3B82F6", "QUIZ", 0)

        // Week 6 — not started
        q.insert("t6_1", "week_6", 6, "Final Review & Synthesis", "Feb 10 - Feb 16", 0, "Comprehensive Review Session", "Psychology 101", "#3B82F6", "LECTURE", 0)
        q.insert("t6_2", "week_6", 6, "Final Review & Synthesis", "Feb 10 - Feb 16", 0, "Final Lab Submission", "Chemistry 201", "#22C55E", "ASSIGNMENT", 0)
        q.insert("t6_3", "week_6", 6, "Final Review & Synthesis", "Feb 10 - Feb 16", 0, "Economics Policy Analysis", "Economics 101", "#F97316", "READING", 0)

        viewModel = PlannerViewModel(db)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasListViewModeAndWeeks() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            assertFalse(state.isLoading)
            assertEquals(PlannerContract.ViewMode.LIST, state.viewMode)
            assertTrue(state.weeks.isNotEmpty())
            assertEquals("week_4", state.expandedWeekId)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun initialStateContainsSixWeeks() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            assertEquals(6, state.weeks.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun switchViewToCalendar() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            viewModel.onIntent(PlannerContract.Intent.SwitchView(PlannerContract.ViewMode.CALENDAR))
            state = awaitItem()

            assertEquals(PlannerContract.ViewMode.CALENDAR, state.viewMode)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun switchViewBackToList() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            viewModel.onIntent(PlannerContract.Intent.SwitchView(PlannerContract.ViewMode.CALENDAR))
            state = awaitItem()
            viewModel.onIntent(PlannerContract.Intent.SwitchView(PlannerContract.ViewMode.LIST))
            state = awaitItem()

            assertEquals(PlannerContract.ViewMode.LIST, state.viewMode)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun toggleWeekExpandsNewWeek() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            viewModel.onIntent(PlannerContract.Intent.ToggleWeek("week_1"))
            state = awaitItem()

            assertEquals("week_1", state.expandedWeekId)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun toggleWeekCollapsesCurrentlyExpandedWeek() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            assertEquals("week_4", state.expandedWeekId)

            viewModel.onIntent(PlannerContract.Intent.ToggleWeek("week_4"))
            state = awaitItem()

            assertNull(state.expandedWeekId)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun toggleWeekSwitchesFromOneToAnother() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            assertEquals("week_4", state.expandedWeekId)

            viewModel.onIntent(PlannerContract.Intent.ToggleWeek("week_2"))
            state = awaitItem()

            assertEquals("week_2", state.expandedWeekId)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun toggleTaskTogglesCompletionStatus() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            // Find an incomplete task in week_4
            val week4 = state.weeks.first { it.id == "week_4" }
            val incompleteTask = week4.tasks.first { !it.isCompleted }
            assertFalse(incompleteTask.isCompleted)

            viewModel.onIntent(PlannerContract.Intent.ToggleTask(incompleteTask.id))
            state = awaitItem()

            val updatedWeek = state.weeks.first { it.id == "week_4" }
            val updatedTask = updatedWeek.tasks.first { it.id == incompleteTask.id }
            assertTrue(updatedTask.isCompleted)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun toggleTaskTogglesCompletedToIncomplete() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            val week4 = state.weeks.first { it.id == "week_4" }
            val completedTask = week4.tasks.first { it.isCompleted }
            assertTrue(completedTask.isCompleted)

            viewModel.onIntent(PlannerContract.Intent.ToggleTask(completedTask.id))
            state = awaitItem()

            val updatedWeek = state.weeks.first { it.id == "week_4" }
            val updatedTask = updatedWeek.tasks.first { it.id == completedTask.id }
            assertFalse(updatedTask.isCompleted)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun toggleTaskUpdatesWeekProgress() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            val week4Before = state.weeks.first { it.id == "week_4" }
            val progressBefore = week4Before.progress

            // Toggle an incomplete task to complete
            val incompleteTask = week4Before.tasks.first { !it.isCompleted }
            viewModel.onIntent(PlannerContract.Intent.ToggleTask(incompleteTask.id))
            state = awaitItem()

            val week4After = state.weeks.first { it.id == "week_4" }
            assertTrue(week4After.progress > progressBefore)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun toggleTaskUpdatesOverallProgress() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            // Toggle an incomplete task in week_4 to complete
            val week4 = state.weeks.first { it.id == "week_4" }
            val incompleteTask = week4.tasks.first { !it.isCompleted }
            viewModel.onIntent(PlannerContract.Intent.ToggleTask(incompleteTask.id))
            state = awaitItem()
            val progressAfterFirstToggle = state.overallProgress

            // Toggle it back to incomplete - progress should decrease
            viewModel.onIntent(PlannerContract.Intent.ToggleTask(incompleteTask.id))
            state = awaitItem()
            val progressAfterSecondToggle = state.overallProgress

            assertTrue(progressAfterFirstToggle > progressAfterSecondToggle)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun week1HasAllTasksCompleted() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            val week1 = state.weeks.first { it.id == "week_1" }
            assertTrue(week1.tasks.all { it.isCompleted })
            assertEquals(1.0f, week1.progress)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun currentWeekIsWeek4() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            val currentWeek = state.weeks.first { it.isCurrentWeek }
            assertEquals("week_4", currentWeek.id)
            assertEquals(4, currentWeek.weekNumber)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun weekTasksHaveCorrectTypes() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            val week1 = state.weeks.first { it.id == "week_1" }
            val types = week1.tasks.map { it.type }
            assertTrue(PlannerContract.PlannerTaskType.LECTURE in types)
            assertTrue(PlannerContract.PlannerTaskType.READING in types)
            assertTrue(PlannerContract.PlannerTaskType.QUIZ in types)
            cancelAndConsumeRemainingEvents()
        }
    }
}
