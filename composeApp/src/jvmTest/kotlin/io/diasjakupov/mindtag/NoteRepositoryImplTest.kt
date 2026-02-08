package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.notes.data.repository.NoteRepositoryImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoteRepositoryImplTest {

    private lateinit var database: MindTagDatabase
    private lateinit var repository: NoteRepositoryImpl

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)
        repository = NoteRepositoryImpl(database)

        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-bio", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-cs", "CS", "#135BEC", "code", 0.0, 0, 0, now, now)
    }

    @Test
    fun createNoteReturnsNoteWithGeneratedId() = runTest {
        val note = repository.createNote(
            title = "Cell Division",
            content = "Mitosis is the process of cell division.",
            subjectId = "subj-bio",
        )

        assertNotNull(note.id)
        assertTrue(note.id.isNotBlank())
        assertEquals("Cell Division", note.title)
        assertEquals("Mitosis is the process of cell division.", note.content)
        assertEquals("subj-bio", note.subjectId)
        assertEquals(1, note.readTimeMinutes) // short content = 1 min minimum
    }

    @Test
    fun createNoteCalculatesReadTimeFromWordCount() = runTest {
        // 200+ words should yield > 1 min
        val longContent = (1..250).joinToString(" ") { "word" }
        val note = repository.createNote(
            title = "Long Note",
            content = longContent,
            subjectId = "subj-bio",
        )

        assertEquals(1, note.readTimeMinutes) // 250/200 = 1
    }

    @Test
    fun createNoteGeneratesSummary() = runTest {
        val content = "A".repeat(200)
        val note = repository.createNote(
            title = "Long Content Note",
            content = content,
            subjectId = "subj-bio",
        )

        assertTrue(note.summary.length <= 153) // 150 + "..."
        assertTrue(note.summary.endsWith("..."))
    }

    @Test
    fun getNotesReturnsAllNotes() = runTest {
        repository.createNote("Note 1", "Content 1", "subj-bio")
        repository.createNote("Note 2", "Content 2", "subj-cs")

        repository.getNotes().test {
            val notes = awaitItem()
            assertEquals(2, notes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getNotesFiltersBySubjectId() = runTest {
        repository.createNote("Bio Note", "Bio content", "subj-bio")
        repository.createNote("CS Note", "CS content", "subj-cs")

        repository.getNotes(subjectId = "subj-bio").test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("Bio Note", notes[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getNoteByIdReturnsCorrectNote() = runTest {
        val created = repository.createNote("Test Note", "Test content", "subj-bio")

        repository.getNoteById(created.id).test {
            val note = awaitItem()
            assertNotNull(note)
            assertEquals(created.id, note.id)
            assertEquals("Test Note", note.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getNoteByIdReturnsNullForNonExistent() = runTest {
        repository.getNoteById("nonexistent").test {
            val note = awaitItem()
            assertNull(note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateNoteChangesFields() = runTest {
        val created = repository.createNote("Original", "Original content", "subj-bio")

        repository.updateNote(created.id, "Updated Title", "Updated content")

        repository.getNoteById(created.id).test {
            val note = awaitItem()
            assertNotNull(note)
            assertEquals("Updated Title", note.title)
            assertEquals("Updated content", note.content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteNoteRemovesNote() = runTest {
        val created = repository.createNote("To Delete", "Content", "subj-bio")

        repository.deleteNote(created.id)

        repository.getNoteById(created.id).test {
            val note = awaitItem()
            assertNull(note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSubjectsReturnsAllSubjects() = runTest {
        repository.getSubjects().test {
            val subjects = awaitItem()
            assertEquals(2, subjects.size)
            // Ordered by name ASC
            assertEquals("Biology", subjects[0].name)
            assertEquals("CS", subjects[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRelatedNotesReturnsConnectedNotes() = runTest {
        val now = System.currentTimeMillis()
        // Insert notes directly for precise control
        database.noteEntityQueries.insert("note-1", "Cell Division", "content1", "sum1", "subj-bio", 1, 3, now, now)
        database.noteEntityQueries.insert("note-2", "DNA Replication", "content2", "sum2", "subj-bio", 2, 4, now, now)

        // Create a semantic link
        database.semanticLinkEntityQueries.insert("link-1", "note-1", "note-2", 0.88, "PREREQUISITE", 0.9, now)

        repository.getRelatedNotes("note-1", limit = 10).test {
            val related = awaitItem()
            assertEquals(1, related.size)
            assertEquals("note-2", related[0].noteId)
            assertEquals("DNA Replication", related[0].title)
            assertEquals("Biology", related[0].subjectName)
            assertEquals(0.88f, related[0].similarityScore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRelatedNotesReturnsEmptyForUnlinkedNote() = runTest {
        val created = repository.createNote("Isolated Note", "No connections", "subj-bio")

        repository.getRelatedNotes(created.id).test {
            val related = awaitItem()
            assertTrue(related.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createNoteAutoGeneratesSemanticLinks() = runTest {
        // Create two related biology notes
        repository.createNote(
            title = "Cell Division",
            content = "Mitosis is the process of cell division where chromosomes replicate and separate into two identical daughter cells.",
            subjectId = "subj-bio",
        )
        val note2 = repository.createNote(
            title = "Meiosis Process",
            content = "Meiosis is a type of cell division that produces four daughter cells each with half the chromosomes of the parent cell.",
            subjectId = "subj-bio",
        )

        // Check semantic links were created
        val links = database.semanticLinkEntityQueries.selectBySourceNoteId(note2.id).executeAsList()
        assertTrue(links.isNotEmpty(), "Expected auto-generated semantic links")
        assertEquals("subj-bio", note2.subjectId)
    }

    @Test
    fun createNoteDoesNotLinkUnrelatedNotes() = runTest {
        repository.createNote(
            title = "Photosynthesis",
            content = "Plants convert sunlight into glucose through the process of photosynthesis using chlorophyll.",
            subjectId = "subj-bio",
        )
        val note2 = repository.createNote(
            title = "Binary Search",
            content = "Binary search algorithm divides the sorted array in half to find target element in logarithmic time.",
            subjectId = "subj-cs",
        )

        val links = database.semanticLinkEntityQueries.selectBySourceNoteId(note2.id).executeAsList()
        assertTrue(links.isEmpty(), "Expected no links between unrelated notes, got ${links.size}")
    }

    @Test
    fun createNoteAutoGeneratesFlashcards() = runTest {
        val note = repository.createNote(
            title = "Photosynthesis",
            content = "Photosynthesis is the process by which green plants convert sunlight into chemical energy. " +
                "Chlorophyll absorbs light energy primarily from the red and blue wavelengths of visible light. " +
                "The light reactions occur in the thylakoid membranes and produce ATP and NADPH molecules.",
            subjectId = "subj-bio",
        )

        val cards = database.flashCardEntityQueries.selectAll().executeAsList()
        assertTrue(cards.isNotEmpty(), "Expected auto-generated flashcards")
        assertTrue(cards.all { it.subject_id == "subj-bio" })
        assertTrue(cards.all { it.source_note_ids_json?.contains(note.id) == true })
    }
}
