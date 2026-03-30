package io.diasjakupov.mindtag

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.test.FakeNoteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the NoteRepository contract behaviour via FakeNoteRepository.
 *
 * Note: The real NoteRepositoryImpl uses the backend API + a local SQLite cache
 * and requires live network infrastructure. These tests verify the NoteRepository
 * interface contract in isolation using a fake implementation.
 */
class NoteRepositoryImplTest {

    private lateinit var repository: FakeNoteRepository

    @BeforeTest
    fun setup() {
        repository = FakeNoteRepository()
        repository.setSubjects(
            listOf(
                Subject(id = "subj-bio", name = "Biology",        colorHex = "#22C55E"),
                Subject(id = "subj-cs",  name = "Computer Science", colorHex = "#135BEC"),
            )
        )
    }

    @Test
    fun createNoteReturnsNoteWithGeneratedId() = runTest {
        val note = repository.createNote(
            title = "Cell Division",
            content = "Mitosis is the process of cell division.",
            subjectName = "Biology",
        )

        assertTrue(note.id > 0L)
        assertEquals("Cell Division", note.title)
        assertEquals("Mitosis is the process of cell division.", note.content)
    }

    @Test
    fun createNote_multipleNotes_haveUniqueIds() = runTest {
        val note1 = repository.createNote("Note 1", "Content 1", "Biology")
        val note2 = repository.createNote("Note 2", "Content 2", "Biology")

        assertTrue(note1.id != note2.id)
    }

    @Test
    fun getNotesReturnsAllNotes() = runTest {
        repository.createNote("Note 1", "Content 1", "Biology")
        repository.createNote("Note 2", "Content 2", "Computer Science")

        val notes = repository.getNotes()
        assertEquals(2, notes.size)
    }

    @Test
    fun getNotesFiltersBySubjectId() = runTest {
        repository.createNote("Bio Note", "Bio content", "Biology")
        repository.createNote("CS Note",  "CS content",  "Computer Science")

        val bioNotes = repository.getNotes(subjectFilter = "Biology")
        assertEquals(1, bioNotes.size)
        assertEquals("Bio Note", bioNotes[0].title)
    }

    @Test
    fun getNoteByIdReturnsCorrectNote() = runTest {
        val created = repository.createNote("Test Note", "Test content", "Biology")

        val found = repository.getNoteById(created.id)
        assertNotNull(found)
        assertEquals(created.id, found.id)
        assertEquals("Test Note", found.title)
    }

    @Test
    fun getNoteByIdReturnsNullForNonExistent() = runTest {
        val note = repository.getNoteById(99999L)
        assertNull(note)
    }

    @Test
    fun updateNoteChangesTitle() = runTest {
        val created = repository.createNote("Original", "Original content", "Biology")

        repository.updateNote(created.id, "Updated Title", "Updated content", "Biology")

        val updated = repository.getNoteById(created.id)
        assertNotNull(updated)
        assertEquals("Updated Title", updated.title)
        assertEquals("Updated content", updated.content)
    }

    @Test
    fun deleteNoteRemovesNote() = runTest {
        val created = repository.createNote("To Delete", "Content", "Biology")

        repository.deleteNote(created.id)

        val deleted = repository.getNoteById(created.id)
        assertNull(deleted)
    }

    @Test
    fun deleteNote_doesNotAffectOtherNotes() = runTest {
        val keep   = repository.createNote("Keep", "Content", "Biology")
        val remove = repository.createNote("Remove", "Content", "Biology")

        repository.deleteNote(remove.id)

        val remaining = repository.getNotes()
        assertEquals(1, remaining.size)
        assertEquals(keep.id, remaining[0].id)
    }

    @Test
    fun getSubjectsReturnsAllSubjects() = runTest {
        val subjects = repository.getSubjects()
        assertEquals(2, subjects.size)
    }

    @Test
    fun getRelatedNotesReturnsConfiguredRelatedNotes() = runTest {
        val created = repository.createNote("Main Note", "Content", "Biology")
        repository.setRelatedNotes(
            created.id,
            listOf(
                RelatedNote(
                    noteId = 2L,
                    title = "Related Note",
                    subjectName = "Biology",
                    subjectColorHex = "#22C55E",
                )
            )
        )

        val related = repository.getRelatedNotes(created.id)
        assertEquals(1, related.size)
        assertEquals("Related Note", related[0].title)
    }

    @Test
    fun getRelatedNotesReturnsEmptyForUnlinkedNote() = runTest {
        val created = repository.createNote("Isolated Note", "No connections", "Biology")

        val related = repository.getRelatedNotes(created.id)
        assertTrue(related.isEmpty())
    }

    @Test
    fun searchNotesReturnsMatchingNotes() = runTest {
        repository.createNote("Binary Search Trees", "Trees content", "Computer Science")
        repository.createNote("Linear Algebra",      "Math content",  "Biology")

        val results = repository.searchNotes("Binary", page = 0, size = 20)
        assertEquals(1, results.notes.size)
        assertEquals("Binary Search Trees", results.notes[0].title)
    }

    @Test
    fun searchNotes_noMatch_returnsEmpty() = runTest {
        repository.createNote("Cell Biology", "Cells content", "Biology")

        val results = repository.searchNotes("Quantum Physics", page = 0, size = 20)
        assertTrue(results.notes.isEmpty())
    }
}
