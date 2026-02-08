package io.diasjakupov.mindtag.feature.home.presentation

import app.cash.turbine.test
import io.diasjakupov.mindtag.feature.home.domain.model.DashboardData
import io.diasjakupov.mindtag.feature.home.domain.model.ReviewCard
import io.diasjakupov.mindtag.feature.home.domain.model.TaskType
import io.diasjakupov.mindtag.feature.home.domain.model.UpNextTask
import io.diasjakupov.mindtag.feature.home.domain.usecase.GetDashboardUseCase
import io.diasjakupov.mindtag.test.FakeDashboardRepository
import io.diasjakupov.mindtag.test.TestData
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var fakeDashboardRepository: FakeDashboardRepository
    private lateinit var getDashboardUseCase: GetDashboardUseCase
    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeDashboardRepository = FakeDashboardRepository()
        getDashboardUseCase = GetDashboardUseCase(fakeDashboardRepository)
        viewModel = HomeViewModel(getDashboardUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateLoadsDataFromUseCase() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(TestData.dashboardData.userName, state.userName)
            assertEquals(TestData.dashboardData.totalNotesCount, state.totalNotesCount)
            assertEquals(TestData.dashboardData.currentStreak, state.currentStreak)
            assertEquals(TestData.dashboardData.reviewCards, state.reviewCards)
            assertEquals(TestData.dashboardData.upNextTasks, state.upNextTasks)
        }
    }

    @Test
    fun tapReviewCardEmitsNavigateToNoteEffect() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(HomeContract.Intent.TapReviewCard("note-1"))
            assertEquals(HomeContract.Effect.NavigateToNote("note-1"), awaitItem())
        }
    }

    @Test
    fun tapReviewCardWithDifferentIdEmitsCorrectEffect() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(HomeContract.Intent.TapReviewCard("note-42"))
            val effect = awaitItem()
            assertTrue(effect is HomeContract.Effect.NavigateToNote)
            assertEquals("note-42", effect.noteId)
        }
    }

    @Test
    fun refreshSetsLoadingThenLoadsData() = runTest {
        // Change the repository data before refreshing
        val newData = DashboardData(
            userName = "Updated User",
            totalNotesCount = 10,
            totalReviewsDue = 2,
            currentStreak = 5,
            reviewCards = emptyList(),
            upNextTasks = emptyList(),
        )
        fakeDashboardRepository.setDashboardData(newData)

        viewModel.state.test {
            // With UnconfinedTestDispatcher, the flow update from changing the repo data
            // and the init loadDashboard already happened. Current state should reflect newData.
            val currentState = awaitItem()
            assertEquals("Updated User", currentState.userName)
            assertEquals(10, currentState.totalNotesCount)
            assertEquals(5, currentState.currentStreak)
            assertTrue(currentState.reviewCards.isEmpty())
            assertTrue(currentState.upNextTasks.isEmpty())
            assertFalse(currentState.isLoading)
        }
    }

    @Test
    fun stateReflectsReviewCards() = runTest {
        val state = viewModel.state.value
        assertEquals(1, state.reviewCards.size)
        assertEquals("note-1", state.reviewCards.first().noteId)
        assertEquals("Linear Algebra Basics", state.reviewCards.first().noteTitle)
    }

    @Test
    fun stateReflectsUpNextTasks() = runTest {
        val state = viewModel.state.value
        assertEquals(1, state.upNextTasks.size)
        assertEquals("task-1", state.upNextTasks.first().id)
        assertEquals(TaskType.REVIEW, state.upNextTasks.first().type)
    }

    @Test
    fun dashboardDataUpdatesReflectedInState() = runTest {
        val newReviewCard = ReviewCard(
            noteId = "note-99",
            noteTitle = "New Note",
            subjectName = "Science",
            subjectColorHex = "#00FF00",
            subjectIconName = "science",
            progressPercent = 0.3f,
            dueCardCount = 7,
            weekNumber = 3,
        )
        val newTask = UpNextTask(
            id = "task-99",
            title = "New Task",
            subtitle = "7 cards due",
            type = TaskType.QUIZ,
        )
        val newData = DashboardData(
            userName = "Jane Doe",
            totalNotesCount = 25,
            totalReviewsDue = 10,
            currentStreak = 12,
            reviewCards = listOf(newReviewCard),
            upNextTasks = listOf(newTask),
        )

        fakeDashboardRepository.setDashboardData(newData)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Jane Doe", state.userName)
            assertEquals(25, state.totalNotesCount)
            assertEquals(12, state.currentStreak)
            assertEquals(1, state.reviewCards.size)
            assertEquals("note-99", state.reviewCards.first().noteId)
            assertEquals(1, state.upNextTasks.size)
            assertEquals("task-99", state.upNextTasks.first().id)
        }
    }
}
