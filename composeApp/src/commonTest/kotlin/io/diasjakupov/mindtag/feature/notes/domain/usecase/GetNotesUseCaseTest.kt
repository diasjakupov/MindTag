package io.diasjakupov.mindtag.feature.notes.domain.usecase

import app.cash.turbine.test
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

        useCase().test {
            val notes = awaitItem()
            assertEquals(3, notes.size)
            assertEquals(TestData.notes, notes)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun returnsFilteredNotesBySubjectId() = runTest {
        repository.setNotes(TestData.notes)

        useCase(subjectId = "subj-1").test {
            val notes = awaitItem()
            assertEquals(2, notes.size)
            assertTrue(notes.all { it.subjectId == "subj-1" })
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun returnsEmptyListWhenNoNotesExist() = runTest {
        useCase().test {
            val notes = awaitItem()
            assertTrue(notes.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun returnsEmptyListForUnknownSubjectId() = runTest {
        repository.setNotes(TestData.notes)

        useCase(subjectId = "nonexistent").test {
            val notes = awaitItem()
            assertTrue(notes.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun emitsUpdatesWhenNotesChange() = runTest {
        useCase().test {
            assertEquals(emptyList(), awaitItem())

            repository.setNotes(listOf(TestData.algebraNote))
            assertEquals(listOf(TestData.algebraNote), awaitItem())

            repository.setNotes(TestData.notes)
            assertEquals(TestData.notes, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }
}
