package io.diasjakupov.mindtag.feature.planner.presentation

import app.cash.turbine.test
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelTest {

    private lateinit var viewModel: PlannerViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = PlannerViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasListViewModeAndWeeks() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PlannerContract.ViewMode.LIST, state.viewMode)
            assertTrue(state.weeks.isNotEmpty())
            assertEquals("week_4", state.expandedWeekId)
        }
    }

    @Test
    fun initialStateContainsSixWeeks() = runTest {
        assertEquals(6, viewModel.state.value.weeks.size)
    }

    @Test
    fun switchViewToCalendar() = runTest {
        viewModel.onIntent(PlannerContract.Intent.SwitchView(PlannerContract.ViewMode.CALENDAR))

        assertEquals(PlannerContract.ViewMode.CALENDAR, viewModel.state.value.viewMode)
    }

    @Test
    fun switchViewBackToList() = runTest {
        viewModel.onIntent(PlannerContract.Intent.SwitchView(PlannerContract.ViewMode.CALENDAR))
        viewModel.onIntent(PlannerContract.Intent.SwitchView(PlannerContract.ViewMode.LIST))

        assertEquals(PlannerContract.ViewMode.LIST, viewModel.state.value.viewMode)
    }

    @Test
    fun toggleWeekExpandsNewWeek() = runTest {
        viewModel.onIntent(PlannerContract.Intent.ToggleWeek("week_1"))

        assertEquals("week_1", viewModel.state.value.expandedWeekId)
    }

    @Test
    fun toggleWeekCollapsesCurrentlyExpandedWeek() = runTest {
        assertEquals("week_4", viewModel.state.value.expandedWeekId)

        viewModel.onIntent(PlannerContract.Intent.ToggleWeek("week_4"))

        assertNull(viewModel.state.value.expandedWeekId)
    }

    @Test
    fun toggleWeekSwitchesFromOneToAnother() = runTest {
        assertEquals("week_4", viewModel.state.value.expandedWeekId)

        viewModel.onIntent(PlannerContract.Intent.ToggleWeek("week_2"))

        assertEquals("week_2", viewModel.state.value.expandedWeekId)
    }

    @Test
    fun toggleTaskTogglesCompletionStatus() = runTest {
        // Find an incomplete task in week_4
        val week4 = viewModel.state.value.weeks.first { it.id == "week_4" }
        val incompleteTask = week4.tasks.first { !it.isCompleted }
        assertFalse(incompleteTask.isCompleted)

        viewModel.onIntent(PlannerContract.Intent.ToggleTask(incompleteTask.id))

        val updatedWeek = viewModel.state.value.weeks.first { it.id == "week_4" }
        val updatedTask = updatedWeek.tasks.first { it.id == incompleteTask.id }
        assertTrue(updatedTask.isCompleted)
    }

    @Test
    fun toggleTaskTogglesCompletedToIncomplete() = runTest {
        val week4 = viewModel.state.value.weeks.first { it.id == "week_4" }
        val completedTask = week4.tasks.first { it.isCompleted }
        assertTrue(completedTask.isCompleted)

        viewModel.onIntent(PlannerContract.Intent.ToggleTask(completedTask.id))

        val updatedWeek = viewModel.state.value.weeks.first { it.id == "week_4" }
        val updatedTask = updatedWeek.tasks.first { it.id == completedTask.id }
        assertFalse(updatedTask.isCompleted)
    }

    @Test
    fun toggleTaskUpdatesWeekProgress() = runTest {
        val week4Before = viewModel.state.value.weeks.first { it.id == "week_4" }
        val progressBefore = week4Before.progress

        // Toggle an incomplete task to complete
        val incompleteTask = week4Before.tasks.first { !it.isCompleted }
        viewModel.onIntent(PlannerContract.Intent.ToggleTask(incompleteTask.id))

        val week4After = viewModel.state.value.weeks.first { it.id == "week_4" }
        assertTrue(week4After.progress > progressBefore)
    }

    @Test
    fun toggleTaskUpdatesOverallProgress() = runTest {
        // First, recalculate the actual overall progress from data
        // The initial overallProgress is hardcoded to 0.58f in State()
        // and init doesn't recalculate it, so we need to first toggle to establish
        // a known baseline, then toggle again to verify change.

        // Toggle an incomplete task in week_4 to complete
        val week4 = viewModel.state.value.weeks.first { it.id == "week_4" }
        val incompleteTask = week4.tasks.first { !it.isCompleted }
        viewModel.onIntent(PlannerContract.Intent.ToggleTask(incompleteTask.id))
        val progressAfterFirstToggle = viewModel.state.value.overallProgress

        // Toggle it back to incomplete - progress should decrease
        viewModel.onIntent(PlannerContract.Intent.ToggleTask(incompleteTask.id))
        val progressAfterSecondToggle = viewModel.state.value.overallProgress

        assertTrue(progressAfterFirstToggle > progressAfterSecondToggle)
    }

    @Test
    fun week1HasAllTasksCompleted() = runTest {
        val week1 = viewModel.state.value.weeks.first { it.id == "week_1" }
        assertTrue(week1.tasks.all { it.isCompleted })
        assertEquals(1.0f, week1.progress)
    }

    @Test
    fun currentWeekIsWeek4() = runTest {
        val currentWeek = viewModel.state.value.weeks.first { it.isCurrentWeek }
        assertEquals("week_4", currentWeek.id)
        assertEquals(4, currentWeek.weekNumber)
    }

    @Test
    fun weekTasksHaveCorrectTypes() = runTest {
        val week1 = viewModel.state.value.weeks.first { it.id == "week_1" }
        val types = week1.tasks.map { it.type }
        assertTrue(PlannerContract.PlannerTaskType.LECTURE in types)
        assertTrue(PlannerContract.PlannerTaskType.READING in types)
        assertTrue(PlannerContract.PlannerTaskType.QUIZ in types)
    }
}
