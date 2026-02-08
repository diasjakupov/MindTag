package io.diasjakupov.mindtag.e2e

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.notes.data.repository.NoteRepositoryImpl
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import io.diasjakupov.mindtag.feature.notes.domain.usecase.CreateNoteUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetNoteWithConnectionsUseCase
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NoteCreationFlowTest {

    private lateinit var database: MindTagDatabase
    private lateinit var noteRepository: NoteRepository
    private lateinit var createNoteUseCase: CreateNoteUseCase
    private lateinit var getNotesUseCase: GetNotesUseCase
    private lateinit var getNoteWithConnectionsUseCase: GetNoteWithConnectionsUseCase
    private lateinit var getSubjectsUseCase: GetSubjectsUseCase

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        noteRepository = NoteRepositoryImpl(database)
        createNoteUseCase = CreateNoteUseCase(noteRepository)
        getNotesUseCase = GetNotesUseCase(noteRepository)
        getNoteWithConnectionsUseCase = GetNoteWithConnectionsUseCase(noteRepository)
        getSubjectsUseCase = GetSubjectsUseCase(noteRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun seedSubject(id: String = "subj-test", name: String = "Test Subject") {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert(
            id = id,
            name = name,
            color_hex = "#FF0000",
            icon_name = "book",
            progress = 0.0,
            total_notes = 0,
            reviewed_notes = 0,
            created_at = now,
            updated_at = now,
        )
    }

    @Test
    fun createNote_thenAppearsInNotesList() = runTest {
        seedSubject()

        val created = createNoteUseCase(
            title = "E2E Test Note",
            content = "This is a note created during an end-to-end test.",
            subjectId = "subj-test",
        )

        assertNotNull(created)
        assertEquals("E2E Test Note", created.title)
        assertEquals("subj-test", created.subjectId)

        getNotesUseCase().test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("E2E Test Note", notes[0].title)
            assertEquals("subj-test", notes[0].subjectId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createNote_thenVerifyDetailView() = runTest {
        seedSubject()

        val created = createNoteUseCase(
            title = "Detail View Note",
            content = "Detailed content for end-to-end testing.",
            subjectId = "subj-test",
        )

        getNoteWithConnectionsUseCase(created.id).test {
            val noteWithConnections = awaitItem()
            assertNotNull(noteWithConnections)
            assertEquals("Detail View Note", noteWithConnections.note.title)
            assertEquals("Detailed content for end-to-end testing.", noteWithConnections.note.content)
            assertTrue(noteWithConnections.relatedNotes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createMultipleNotes_allAppearInList() = runTest {
        seedSubject("subj-a", "Subject A")
        seedSubject("subj-b", "Subject B")

        createNoteUseCase(title = "Note Alpha", content = "Content A", subjectId = "subj-a")
        createNoteUseCase(title = "Note Beta", content = "Content B", subjectId = "subj-b")
        createNoteUseCase(title = "Note Gamma", content = "Content C", subjectId = "subj-a")

        getNotesUseCase().test {
            val notes = awaitItem()
            assertEquals(3, notes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createNote_filterBySubject_showsOnlyMatchingNotes() = runTest {
        seedSubject("subj-a", "Subject A")
        seedSubject("subj-b", "Subject B")

        createNoteUseCase(title = "Note A1", content = "Content", subjectId = "subj-a")
        createNoteUseCase(title = "Note A2", content = "Content", subjectId = "subj-a")
        createNoteUseCase(title = "Note B1", content = "Content", subjectId = "subj-b")

        getNotesUseCase(subjectId = "subj-a").test {
            val notes = awaitItem()
            assertEquals(2, notes.size)
            assertTrue(notes.all { it.subjectId == "subj-a" })
            cancelAndIgnoreRemainingEvents()
        }

        getNotesUseCase(subjectId = "subj-b").test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("Note B1", notes[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createNote_verifySummaryGenerated() = runTest {
        seedSubject()

        val longContent = "This is a very long content string that should be truncated. " +
            "It contains more than 150 characters to test the summary generation logic. " +
            "The summary should end with ellipsis when the content exceeds the limit."

        val created = createNoteUseCase(
            title = "Summary Test",
            content = longContent,
            subjectId = "subj-test",
        )

        assertTrue(created.summary.length <= 153) // 150 chars + "..."
        assertTrue(created.summary.endsWith("..."))
    }

    @Test
    fun createNote_verifyReadTimeCalculated() = runTest {
        seedSubject()

        // ~200 words = 1 minute read time
        val words = (1..400).joinToString(" ") { "word" }
        val created = createNoteUseCase(
            title = "Read Time Test",
            content = words,
            subjectId = "subj-test",
        )

        assertEquals(2, created.readTimeMinutes) // 400 words / 200 = 2 minutes
    }

    @Test
    fun createNote_withSemanticLinks_appearsInRelatedNotes() = runTest {
        seedSubject()

        val noteA = createNoteUseCase(title = "Note A", content = "Content A", subjectId = "subj-test")
        val noteB = createNoteUseCase(title = "Note B", content = "Content B", subjectId = "subj-test")

        // SemanticAnalyzer auto-generates links during createNote, but also
        // manually insert a semantic link to ensure manual links work too
        val now = System.currentTimeMillis()
        database.semanticLinkEntityQueries.insert(
            id = "link-1",
            source_note_id = noteA.id,
            target_note_id = noteB.id,
            similarity_score = 0.85,
            link_type = "RELATED",
            strength = 0.9,
            created_at = now,
        )

        getNoteWithConnectionsUseCase(noteA.id).test {
            val result = awaitItem()
            assertNotNull(result)
            assertTrue(result.relatedNotes.isNotEmpty(), "Expected related notes")
            assertTrue(result.relatedNotes.any { it.noteId == noteB.id }, "Expected Note B in related notes")
            assertEquals("Note B", result.relatedNotes.first { it.noteId == noteB.id }.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun subjects_loadedCorrectly() = runTest {
        seedSubject("subj-a", "Alpha Subject")
        seedSubject("subj-b", "Beta Subject")

        getSubjectsUseCase().test {
            val subjects = awaitItem()
            assertEquals(2, subjects.size)
            assertEquals("Alpha Subject", subjects[0].name) // ordered by name ASC
            assertEquals("Beta Subject", subjects[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
