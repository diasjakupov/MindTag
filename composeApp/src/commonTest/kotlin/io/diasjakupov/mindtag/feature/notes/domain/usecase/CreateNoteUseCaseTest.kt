package io.diasjakupov.mindtag.feature.notes.domain.usecase

import app.cash.turbine.test
import io.diasjakupov.mindtag.test.FakeNoteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CreateNoteUseCaseTest {

    private val repository = FakeNoteRepository()
    private val useCase = CreateNoteUseCase(repository)

    @Test
    fun createsNoteWithValidInput() = runTest {
        val note = useCase("My Title", "Some content", "subj-1")

        assertEquals("My Title", note.title)
        assertEquals("Some content", note.content)
        assertEquals("subj-1", note.subjectId)
    }

    @Test
    fun trimsNoteTitle() = runTest {
        val note = useCase("  Trimmed Title  ", "content", "subj-1")

        assertEquals("Trimmed Title", note.title)
    }

    @Test
    fun createdNoteAppearsInRepository() = runTest {
        useCase("Test Note", "content", "subj-1")

        repository.getNotes().test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("Test Note", notes.first().title)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun throwsExceptionForBlankTitle() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase("", "content", "subj-1")
        }
    }

    @Test
    fun throwsExceptionForWhitespaceOnlyTitle() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase("   ", "content", "subj-1")
        }
    }

    @Test
    fun allowsEmptyContent() = runTest {
        val note = useCase("Title", "", "subj-1")
        assertEquals("", note.content)
    }

    @Test
    fun assignsUniqueIdsToMultipleNotes() = runTest {
        val note1 = useCase("Note 1", "content 1", "subj-1")
        val note2 = useCase("Note 2", "content 2", "subj-1")

        assertTrue(note1.id != note2.id)
    }
}
