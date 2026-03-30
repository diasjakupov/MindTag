package io.diasjakupov.mindtag.feature.notes.domain.usecase

import io.diasjakupov.mindtag.test.FakeNoteRepository
import io.diasjakupov.mindtag.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetNoteWithConnectionsUseCaseTest {

    private val repository = FakeNoteRepository()
    private val useCase = GetNoteWithConnectionsUseCase(repository)

    @Test
    fun returnsNoteWithRelatedNotes() = runTest {
        repository.setNotes(TestData.notes)
        repository.setRelatedNotes(TestData.algebraNote.id, listOf(TestData.relatedNote))

        val result = useCase(TestData.algebraNote.id)
        assertNotNull(result)
        assertEquals(TestData.algebraNote, result.note)
        assertEquals(1, result.relatedNotes.size)
        assertEquals(TestData.relatedNote, result.relatedNotes.first())
    }

    @Test
    fun returnsNoteWithEmptyRelatedNotesWhenNoneExist() = runTest {
        repository.setNotes(TestData.notes)

        val result = useCase(TestData.algebraNote.id)
        assertNotNull(result)
        assertEquals(TestData.algebraNote, result.note)
        assertTrue(result.relatedNotes.isEmpty())
    }

    @Test
    fun returnsNullWhenNoteDoesNotExist() = runTest {
        val result = useCase(99999L)
        assertNull(result)
    }

    @Test
    fun returnsNullWhenNoteIdDoesNotMatchEvenWithRelatedNotes() = runTest {
        repository.setRelatedNotes(99999L, listOf(TestData.relatedNote))

        val result = useCase(99999L)
        assertNull(result)
    }
}
