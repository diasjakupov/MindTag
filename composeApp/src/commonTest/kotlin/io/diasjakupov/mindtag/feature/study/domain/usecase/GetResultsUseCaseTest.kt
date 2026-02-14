package io.diasjakupov.mindtag.feature.study.domain.usecase

import app.cash.turbine.test
import io.diasjakupov.mindtag.test.FakeQuizRepository
import io.diasjakupov.mindtag.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetResultsUseCaseTest {

    private val repository = FakeQuizRepository()
    private val useCase = GetResultsUseCase(repository)

    @Test
    fun returnsSessionResultWhenExists() = runTest {
        repository.setSessionResult("session-2", TestData.sessionResult)

        useCase("session-2").test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(80, result.scorePercent)
            assertEquals(4, result.totalCorrect)
            assertEquals(5, result.totalQuestions)
            assertEquals("15:00", result.timeSpentFormatted)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun returnsNullWhenSessionDoesNotExist() = runTest {
        useCase("nonexistent").test {
            val result = awaitItem()
            assertNull(result)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun resultContainsAnswerDetails() = runTest {
        repository.setSessionResult("session-2", TestData.sessionResult)

        useCase("session-2").test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(1, result.answers.size)
            val answer = result.answers.first()
            assertEquals("card-1", answer.cardId)
            assertEquals("What is a vector?", answer.question)
            assertEquals(true, answer.isCorrect)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun resultContainsSessionInfo() = runTest {
        repository.setSessionResult("session-2", TestData.sessionResult)

        useCase("session-2").test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(TestData.completedSession, result.session)
            cancelAndConsumeRemainingEvents()
        }
    }
}
