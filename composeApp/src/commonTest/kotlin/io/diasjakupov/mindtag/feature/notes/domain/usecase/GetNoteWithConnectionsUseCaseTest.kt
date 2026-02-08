package io.diasjakupov.mindtag.feature.notes.domain.usecase

import app.cash.turbine.test
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
        repository.setRelatedNotes("note-1", listOf(TestData.relatedNote))

        useCase("note-1").test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(TestData.algebraNote, result.note)
            assertEquals(1, result.relatedNotes.size)
            assertEquals(TestData.relatedNote, result.relatedNotes.first())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun returnsNoteWithEmptyRelatedNotesWhenNoneExist() = runTest {
        repository.setNotes(TestData.notes)

        useCase("note-1").test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(TestData.algebraNote, result.note)
            assertTrue(result.relatedNotes.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun returnsNullWhenNoteDoesNotExist() = runTest {
        useCase("nonexistent").test {
            val result = awaitItem()
            assertNull(result)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun returnsNullWhenNoteIdDoesNotMatchEvenWithRelatedNotes() = runTest {
        repository.setRelatedNotes("nonexistent", listOf(TestData.relatedNote))

        useCase("nonexistent").test {
            val result = awaitItem()
            assertNull(result)
            cancelAndConsumeRemainingEvents()
        }
    }
}
