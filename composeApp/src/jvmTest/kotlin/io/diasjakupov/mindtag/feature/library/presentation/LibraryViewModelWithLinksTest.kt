package io.diasjakupov.mindtag.feature.library.presentation

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.test.FakeNoteRepository
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
class LibraryViewModelWithLinksTest {

    private lateinit var viewModel: LibraryViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        val fakeNoteRepository = FakeNoteRepository()
        fakeNoteRepository.setNotes(TestData.notes)
        fakeNoteRepository.setSubjects(TestData.subjects)

        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        val db = MindTagDatabase(driver)

        // Seed semantic links BEFORE creating the ViewModel
        db.semanticLinkEntityQueries.insert(
            id = "link-1",
            source_note_id = "note-1",
            target_note_id = "note-2",
            similarity_score = 0.85,
            link_type = "similar",
            strength = 0.9,
            created_at = 1700000000000L,
        )
        db.semanticLinkEntityQueries.insert(
            id = "link-2",
            source_note_id = "note-1",
            target_note_id = "note-3",
            similarity_score = 0.6,
            link_type = "prerequisite",
            strength = 0.7,
            created_at = 1700000000000L,
        )

        viewModel = LibraryViewModel(fakeNoteRepository, db)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun graphEdgesPopulateFromSemanticLinks() = runTest {
        viewModel.state.test {
            // The combine flow may emit multiple times as the SQLDelight flow
            // arrives on Dispatchers.IO. Wait for a state that has edges loaded.
            var state = awaitItem()
            if (state.graphEdges.isEmpty()) {
                state = awaitItem()
            }
            assertFalse(state.isLoading)
            assertEquals(2, state.graphEdges.size)
        }
    }

    @Test
    fun graphEdgesHaveCorrectSourceAndTarget() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.graphEdges.isEmpty()) {
                state = awaitItem()
            }
            val link1 = state.graphEdges.first { it.type == "similar" }
            assertEquals("note-1", link1.sourceNoteId)
            assertEquals("note-2", link1.targetNoteId)
            assertEquals(0.9f, link1.strength)
        }
    }

    @Test
    fun graphEdgesHaveCorrectTypes() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.graphEdges.isEmpty()) {
                state = awaitItem()
            }
            val types = state.graphEdges.map { it.type }.toSet()
            assertTrue(types.contains("similar"))
            assertTrue(types.contains("prerequisite"))
        }
    }
}
