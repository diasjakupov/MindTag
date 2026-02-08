package io.diasjakupov.mindtag.feature.home.domain.usecase

import app.cash.turbine.test
import io.diasjakupov.mindtag.feature.home.domain.model.DashboardData
import io.diasjakupov.mindtag.feature.home.domain.model.TaskType
import io.diasjakupov.mindtag.feature.home.domain.model.UpNextTask
import io.diasjakupov.mindtag.test.FakeDashboardRepository
import io.diasjakupov.mindtag.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDashboardUseCaseTest {

    private val repository = FakeDashboardRepository()
    private val useCase = GetDashboardUseCase(repository)

    @Test
    fun returnsDashboardData() = runTest {
        useCase().test {
            val data = awaitItem()
            assertEquals("Test User", data.userName)
            assertEquals(3, data.totalNotesCount)
            assertEquals(5, data.totalReviewsDue)
            assertEquals(3, data.currentStreak)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun dashboardContainsReviewCards() = runTest {
        useCase().test {
            val data = awaitItem()
            assertEquals(1, data.reviewCards.size)
            val card = data.reviewCards.first()
            assertEquals("note-1", card.noteId)
            assertEquals("Linear Algebra Basics", card.noteTitle)
            assertEquals(3, card.dueCardCount)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun dashboardContainsUpNextTasks() = runTest {
        useCase().test {
            val data = awaitItem()
            assertEquals(1, data.upNextTasks.size)
            val task = data.upNextTasks.first()
            assertEquals("Review Linear Algebra", task.title)
            assertEquals(TaskType.REVIEW, task.type)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun emitsUpdatesWhenDashboardDataChanges() = runTest {
        useCase().test {
            val initial = awaitItem()
            assertEquals(3, initial.totalNotesCount)

            val updated = TestData.dashboardData.copy(
                totalNotesCount = 10,
                totalReviewsDue = 8,
            )
            repository.setDashboardData(updated)

            val next = awaitItem()
            assertEquals(10, next.totalNotesCount)
            assertEquals(8, next.totalReviewsDue)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun dashboardWithEmptyReviewCardsAndTasks() = runTest {
        val emptyDashboard = DashboardData(
            userName = "New User",
            totalNotesCount = 0,
            totalReviewsDue = 0,
            currentStreak = 0,
            reviewCards = emptyList(),
            upNextTasks = emptyList(),
        )
        repository.setDashboardData(emptyDashboard)

        useCase().test {
            val data = awaitItem()
            assertEquals("New User", data.userName)
            assertEquals(0, data.totalNotesCount)
            assertTrue(data.reviewCards.isEmpty())
            assertTrue(data.upNextTasks.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun dashboardWithMultipleUpNextTasks() = runTest {
        val multiTaskDashboard = TestData.dashboardData.copy(
            upNextTasks = listOf(
                UpNextTask("t1", "Review Algebra", "3 cards", TaskType.REVIEW),
                UpNextTask("t2", "Physics Quiz", "5 questions", TaskType.QUIZ),
                UpNextTask("t3", "New Note", "Mechanics", TaskType.NOTE),
            ),
        )
        repository.setDashboardData(multiTaskDashboard)

        useCase().test {
            val data = awaitItem()
            assertEquals(3, data.upNextTasks.size)
            assertEquals(TaskType.REVIEW, data.upNextTasks[0].type)
            assertEquals(TaskType.QUIZ, data.upNextTasks[1].type)
            assertEquals(TaskType.NOTE, data.upNextTasks[2].type)
            cancelAndConsumeRemainingEvents()
        }
    }
}
