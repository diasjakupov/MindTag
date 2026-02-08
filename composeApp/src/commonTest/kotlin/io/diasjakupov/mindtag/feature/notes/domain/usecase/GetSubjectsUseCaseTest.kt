package io.diasjakupov.mindtag.feature.notes.domain.usecase

import app.cash.turbine.test
import io.diasjakupov.mindtag.test.FakeNoteRepository
import io.diasjakupov.mindtag.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetSubjectsUseCaseTest {

    private val repository = FakeNoteRepository()
    private val useCase = GetSubjectsUseCase(repository)

    @Test
    fun returnsAllSubjects() = runTest {
        repository.setSubjects(TestData.subjects)

        useCase().test {
            val subjects = awaitItem()
            assertEquals(2, subjects.size)
            assertEquals(TestData.subjects, subjects)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun returnsEmptyListWhenNoSubjectsExist() = runTest {
        useCase().test {
            val subjects = awaitItem()
            assertTrue(subjects.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun emitsUpdatesWhenSubjectsChange() = runTest {
        useCase().test {
            assertEquals(emptyList(), awaitItem())

            repository.setSubjects(listOf(TestData.mathSubject))
            assertEquals(listOf(TestData.mathSubject), awaitItem())

            repository.setSubjects(TestData.subjects)
            assertEquals(TestData.subjects, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }
}
