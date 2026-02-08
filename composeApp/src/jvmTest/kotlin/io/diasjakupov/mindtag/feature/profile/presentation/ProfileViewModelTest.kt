package io.diasjakupov.mindtag.feature.profile.presentation

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

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var db: MindTagDatabase
    private lateinit var viewModel: ProfileViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        db = MindTagDatabase(driver)

        // Seed some test data
        val now = System.currentTimeMillis()
        db.subjectEntityQueries.insert("subj-1", "Bio", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        db.noteEntityQueries.insert("n1", "Note 1", "Content", "Sum", "subj-1", null, 5, now, now)
        db.noteEntityQueries.insert("n2", "Note 2", "Content", "Sum", "subj-1", null, 3, now, now)
        db.studySessionEntityQueries.insert("s1", "subj-1", "QUICK_QUIZ", now, null, 10, null, "COMPLETED")
        db.userProgressEntityQueries.insert("subj-1", 0.65, 3, 5, 0.8, 7, 500, now)

        viewModel = ProfileViewModel(db)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun statsLoadFromDatabase() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            assertFalse(state.isLoading)
            assertEquals(2, state.totalNotes)
            assertEquals(1, state.totalStudySessions)
            assertEquals(7, state.currentStreak)
            assertEquals(500, state.totalXp)
            assertEquals("Alex Johnson", state.userName)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun profileHasStaticUserInfo() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Alex Johnson", state.userName)
            assertEquals("alex.johnson@university.edu", state.email)
            assertEquals("January 2026", state.memberSince)
            cancelAndConsumeRemainingEvents()
        }
    }
}
