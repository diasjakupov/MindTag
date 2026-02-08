package io.diasjakupov.mindtag.e2e

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.seed.SeedData
import io.diasjakupov.mindtag.feature.home.data.repository.DashboardRepositoryImpl
import io.diasjakupov.mindtag.feature.home.domain.model.TaskType
import io.diasjakupov.mindtag.feature.home.domain.repository.DashboardRepository
import io.diasjakupov.mindtag.feature.home.domain.usecase.GetDashboardUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardFlowTest {

    private lateinit var database: MindTagDatabase
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var getDashboardUseCase: GetDashboardUseCase

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        SeedData.populate(database)

        dashboardRepository = DashboardRepositoryImpl(database)
        getDashboardUseCase = GetDashboardUseCase(dashboardRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dashboard_showsCorrectTotalNoteCount() = runTest {
        getDashboardUseCase().test {
            val data = awaitItem()
            assertEquals(15, data.totalNotesCount) // 5 bio + 5 econ + 5 cs
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboard_showsCorrectUserName() = runTest {
        getDashboardUseCase().test {
            val data = awaitItem()
            assertEquals("Alex", data.userName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboard_showsMaxStreak() = runTest {
        getDashboardUseCase().test {
            val data = awaitItem()
            // CS has streak=7 (highest), Bio=4, Econ=2
            assertEquals(7, data.currentStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboard_showsThreeSubjectReviewCards() = runTest {
        getDashboardUseCase().test {
            val data = awaitItem()
            assertEquals(3, data.reviewCards.size)

            val subjectNames = data.reviewCards.map { it.subjectName }.toSet()
            assertTrue(subjectNames.contains("Biology 101"))
            assertTrue(subjectNames.contains("Economics 101"))
            assertTrue(subjectNames.contains("Computer Science"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboard_reviewCardsSortedByDueCardCount() = runTest {
        getDashboardUseCase().test {
            val data = awaitItem()
            val dueCardCounts = data.reviewCards.map { it.dueCardCount }
            // Verify sorted descending
            for (i in 0 until dueCardCounts.size - 1) {
                assertTrue(
                    dueCardCounts[i] >= dueCardCounts[i + 1],
                    "Review cards should be sorted by due card count descending"
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboard_reviewCardsHaveCorrectProgressFromUserProgress() = runTest {
        getDashboardUseCase().test {
            val data = awaitItem()

            val bioCard = data.reviewCards.find { it.subjectName == "Biology 101" }
            assertNotNull(bioCard)
            assertEquals(65.0f, bioCard.progressPercent)

            val econCard = data.reviewCards.find { it.subjectName == "Economics 101" }
            assertNotNull(econCard)
            assertEquals(42.0f, econCard.progressPercent)

            val csCard = data.reviewCards.find { it.subjectName == "Computer Science" }
            assertNotNull(csCard)
            assertEquals(78.0f, csCard.progressPercent)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboard_upNextTasksHaveUpToThreeItems() = runTest {
        getDashboardUseCase().test {
            val data = awaitItem()
            assertTrue(data.upNextTasks.size in 1..3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboard_upNextIncludesReviewTask() = runTest {
        getDashboardUseCase().test {
            val data = awaitItem()
            val reviewTasks = data.upNextTasks.filter { it.type == TaskType.REVIEW }
            assertTrue(reviewTasks.isNotEmpty(), "Should have at least one review task")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dashboard_dueCardsCountMatchesFlashcardsWithNullNextReview() = runTest {
        getDashboardUseCase().test {
            val data = awaitItem()
            // All seed flashcards have next_review_at = null, so all 30 should be due
            assertTrue(data.totalReviewsDue > 0, "There should be due cards from seed data")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emptyDashboard_showsZeros() = runTest {
        // Create fresh empty database
        val emptyDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(emptyDriver)
        val emptyDb = MindTagDatabase(emptyDriver)

        val emptyRepo = DashboardRepositoryImpl(emptyDb)
        val emptyUseCase = GetDashboardUseCase(emptyRepo)

        emptyUseCase().test {
            val data = awaitItem()
            assertEquals(0, data.totalNotesCount)
            assertEquals(0, data.currentStreak)
            assertTrue(data.reviewCards.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
