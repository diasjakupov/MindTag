package io.diasjakupov.mindtag.feature.notes.domain.usecase

import io.diasjakupov.mindtag.test.FakeNoteRepository
import io.diasjakupov.mindtag.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetNotesUseCaseTest {

    private val repository = FakeNoteRepository()
    private val useCase = GetNotesUseCase(repository)

    @Test
    fun returnsAllNotesWhenNoSubjectFilter() = runTest {
        repository.setNotes(TestData.notes)

        val notes = useCase()
        assertEquals(3, notes.size)
        assertEquals(TestData.notes, notes)
    }

    @Test
    fun returnsFilteredNotesBySubjectId() = runTest {
        repository.setNotes(TestData.notes)

        val notes = useCase(subjectFilter = "subj-1")
        assertEquals(2, notes.size)
        assertTrue(notes.all { it.subjectId == "subj-1" })
    }

    @Test
    fun returnsEmptyListWhenNoNotesExist() = runTest {
        val notes = useCase()
        assertTrue(notes.isEmpty())
    }

    @Test
    fun returnsEmptyListForUnknownSubjectId() = runTest {
        repository.setNotes(TestData.notes)

        val notes = useCase(subjectFilter = "nonexistent")
        assertTrue(notes.isEmpty())
    }
}
