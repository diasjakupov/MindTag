package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.seed.SeedData
import io.diasjakupov.mindtag.feature.home.data.repository.DashboardRepositoryImpl
import io.diasjakupov.mindtag.feature.home.domain.model.TaskType
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DashboardRepositoryImplTest {

    private lateinit var database: MindTagDatabase
    private lateinit var repository: DashboardRepositoryImpl

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)
        repository = DashboardRepositoryImpl(database)
    }

    @Test
    fun getDashboardDataWithEmptyDatabase() = runTest {
        repository.getDashboardData().test {
            val data = awaitItem()
            assertEquals("Alex", data.userName)
            assertEquals(0, data.totalNotesCount)
            assertEquals(0, data.totalReviewsDue)
            assertEquals(0, data.currentStreak)
            assertTrue(data.reviewCards.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDashboardDataWithSeedData() = runTest {
        SeedData.populate(database)

        repository.getDashboardData().test {
            val data = awaitItem()
            assertEquals("Alex", data.userName)
            assertEquals(15, data.totalNotesCount) // 5 bio + 5 econ + 5 cs
            assertEquals(3, data.reviewCards.size) // 3 subjects
            assertTrue(data.currentStreak > 0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDashboardDataReturnsCorrectTotalReviewsDue() = runTest {
        SeedData.populate(database)

        repository.getDashboardData().test {
            val data = awaitItem()
            // All cards in seed data have null next_review_at, so all are due
            assertTrue(data.totalReviewsDue > 0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reviewCardsContainSubjectInfo() = runTest {
        SeedData.populate(database)

        repository.getDashboardData().test {
            val data = awaitItem()
            val reviewCards = data.reviewCards
            assertTrue(reviewCards.isNotEmpty())

            // Each review card should have subject info
            for (card in reviewCards) {
                assertTrue(card.subjectName.isNotBlank())
                assertTrue(card.subjectColorHex.isNotBlank())
                assertTrue(card.subjectIconName.isNotBlank())
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reviewCardsSortedByDueCardCountDescending() = runTest {
        SeedData.populate(database)

        repository.getDashboardData().test {
            val data = awaitItem()
            val dueCardCounts = data.reviewCards.map { it.dueCardCount }
            // Should be in descending order
            for (i in 0 until dueCardCounts.size - 1) {
                assertTrue(dueCardCounts[i] >= dueCardCounts[i + 1])
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upNextTasksContainAtMostThreeItems() = runTest {
        SeedData.populate(database)

        repository.getDashboardData().test {
            val data = awaitItem()
            assertTrue(data.upNextTasks.size <= 3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upNextTasksIncludeReviewTaskWhenCardsDue() = runTest {
        SeedData.populate(database)

        repository.getDashboardData().test {
            val data = awaitItem()
            val hasReview = data.upNextTasks.any { it.type == TaskType.REVIEW }
            assertTrue(hasReview)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upNextTasksAlwaysIncludeNoteTask() = runTest {
        SeedData.populate(database)

        repository.getDashboardData().test {
            val data = awaitItem()
            val hasNote = data.upNextTasks.any { it.type == TaskType.NOTE }
            assertTrue(hasNote)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDashboardDataWithSingleSubject() = runTest {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-1", "Biology", "#22C55E", "leaf", 0.5, 2, 1, now, now)
        database.noteEntityQueries.insert("note-1", "Cell Division", "content", "summary", "subj-1", 1, 3, now, now)
        database.userProgressEntityQueries.insert("subj-1", 50.0, 1, 2, 70.0, 3, 500, now)

        repository.getDashboardData().test {
            val data = awaitItem()
            assertEquals(1, data.totalNotesCount)
            assertEquals(1, data.reviewCards.size)
            assertEquals("Biology", data.reviewCards[0].subjectName)
            assertEquals(3, data.currentStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDashboardDataCurrentStreakIsMaxAcrossSubjects() = runTest {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-1", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-2", "CS", "#135BEC", "code", 0.0, 0, 0, now, now)
        database.userProgressEntityQueries.insert("subj-1", 50.0, 1, 2, 70.0, 3, 500, now)
        database.userProgressEntityQueries.insert("subj-2", 80.0, 4, 5, 90.0, 7, 2000, now)

        repository.getDashboardData().test {
            val data = awaitItem()
            assertEquals(7, data.currentStreak) // max of 3 and 7
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reviewCardProgressComesFromUserProgress() = runTest {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-1", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.noteEntityQueries.insert("note-1", "Note", "c", "s", "subj-1", 1, 3, now, now)
        database.userProgressEntityQueries.insert("subj-1", 65.0, 3, 5, 72.0, 4, 1250, now)

        repository.getDashboardData().test {
            val data = awaitItem()
            assertEquals(1, data.reviewCards.size)
            assertEquals(65.0f, data.reviewCards[0].progressPercent)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
