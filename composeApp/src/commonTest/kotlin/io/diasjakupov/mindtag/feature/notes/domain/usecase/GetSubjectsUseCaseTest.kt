package io.diasjakupov.mindtag.feature.notes.domain.usecase

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

        val subjects = useCase()
        assertEquals(2, subjects.size)
        assertEquals(TestData.subjects, subjects)
    }

    @Test
    fun returnsEmptyListWhenNoSubjectsExist() = runTest {
        val subjects = useCase()
        assertTrue(subjects.isEmpty())
    }
}
