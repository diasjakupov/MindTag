package io.diasjakupov.mindtag.e2e

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.seed.SeedData
import io.diasjakupov.mindtag.feature.notes.data.repository.NoteRepositoryImpl
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetNotesUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetSubjectsUseCase
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibrarySearchFilterFlowTest {

    private lateinit var database: MindTagDatabase
    private lateinit var noteRepository: NoteRepository
    private lateinit var getNotesUseCase: GetNotesUseCase
    private lateinit var getSubjectsUseCase: GetSubjectsUseCase

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        SeedData.populate(database)

        noteRepository = NoteRepositoryImpl(database)
        getNotesUseCase = GetNotesUseCase(noteRepository)
        getSubjectsUseCase = GetSubjectsUseCase(noteRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAllNotes_returns15Notes() = runTest {
        getNotesUseCase().test {
            val notes = awaitItem()
            assertEquals(15, notes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun filterByBiology_returns5Notes() = runTest {
        getNotesUseCase(subjectId = "subj-bio-101").test {
            val notes = awaitItem()
            assertEquals(5, notes.size)
            assertTrue(notes.all { it.subjectId == "subj-bio-101" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun filterByEconomics_returns5Notes() = runTest {
        getNotesUseCase(subjectId = "subj-econ-101").test {
            val notes = awaitItem()
            assertEquals(5, notes.size)
            assertTrue(notes.all { it.subjectId == "subj-econ-101" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun filterByComputerScience_returns5Notes() = runTest {
        getNotesUseCase(subjectId = "subj-cs-101").test {
            val notes = awaitItem()
            assertEquals(5, notes.size)
            assertTrue(notes.all { it.subjectId == "subj-cs-101" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun filterByNonExistentSubject_returnsEmpty() = runTest {
        getNotesUseCase(subjectId = "subj-nonexistent").test {
            val notes = awaitItem()
            assertTrue(notes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByTitle_matchesCorrectNotes() = runTest {
        // The library search is done at the ViewModel level via filterNotes.
        // At the repository level we can verify the raw data is there.
        getNotesUseCase().test {
            val allNotes = awaitItem()

            // Simulate search filtering (same logic as LibraryViewModel.filterNotes)
            val query = "Binary"
            val filtered = allNotes.filter { it.title.contains(query, ignoreCase = true) }
            assertEquals(1, filtered.size)
            assertEquals("Binary Search Trees", filtered[0].title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByTitle_caseInsensitive() = runTest {
        getNotesUseCase().test {
            val allNotes = awaitItem()

            val query = "dna"
            val filtered = allNotes.filter { it.title.contains(query, ignoreCase = true) }
            assertEquals(1, filtered.size)
            assertTrue(filtered[0].title.contains("DNA", ignoreCase = true))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchWithSubjectFilter_combinesFilters() = runTest {
        getNotesUseCase(subjectId = "subj-cs-101").test {
            val csNotes = awaitItem()
            assertEquals(5, csNotes.size)

            // Further filter by search
            val query = "Sort"
            val filtered = csNotes.filter { it.title.contains(query, ignoreCase = true) }
            assertEquals(1, filtered.size)
            assertEquals("Sorting Algorithms Compared", filtered[0].title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadSubjects_returns3Subjects() = runTest {
        getSubjectsUseCase().test {
            val subjects = awaitItem()
            assertEquals(3, subjects.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun subjects_orderedAlphabeticallyByName() = runTest {
        getSubjectsUseCase().test {
            val subjects = awaitItem()
            // SubjectEntity.sq selectAll orders by name ASC
            assertEquals("Biology 101", subjects[0].name)
            assertEquals("Computer Science", subjects[1].name)
            assertEquals("Economics 101", subjects[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun notesOrderedByUpdatedAtDescending() = runTest {
        getNotesUseCase().test {
            val notes = awaitItem()
            for (i in 0 until notes.size - 1) {
                assertTrue(
                    notes[i].updatedAt >= notes[i + 1].updatedAt,
                    "Notes should be ordered by updatedAt descending"
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun semanticLinks_loadedForGraphView() = runTest {
        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        // 5 bio + 5 econ + 5 cs internal + 5 cross-subject = 20 links
        assertEquals(20, links.size)
    }

    @Test
    fun semanticLinks_haveValidNoteReferences() = runTest {
        val links = database.semanticLinkEntityQueries.selectAll().executeAsList()
        val noteIds = database.noteEntityQueries.selectAll().executeAsList().map { it.id }.toSet()

        links.forEach { link ->
            assertTrue(
                link.source_note_id in noteIds,
                "Source note ${link.source_note_id} should exist"
            )
            assertTrue(
                link.target_note_id in noteIds,
                "Target note ${link.target_note_id} should exist"
            )
        }
    }

    @Test
    fun searchNoMatch_returnsEmpty() = runTest {
        getNotesUseCase().test {
            val allNotes = awaitItem()
            val query = "zzzzNonExistentTopic"
            val filtered = allNotes.filter { it.title.contains(query, ignoreCase = true) }
            assertTrue(filtered.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emptySearch_returnsAllNotes() = runTest {
        getNotesUseCase().test {
            val allNotes = awaitItem()
            val query = ""
            val filtered = allNotes.filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
            assertEquals(15, filtered.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
