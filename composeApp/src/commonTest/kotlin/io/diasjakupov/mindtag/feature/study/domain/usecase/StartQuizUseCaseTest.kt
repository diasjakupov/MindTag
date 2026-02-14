package io.diasjakupov.mindtag.feature.study.domain.usecase

import app.cash.turbine.test
import io.diasjakupov.mindtag.feature.study.domain.model.SessionStatus
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.test.FakeStudyRepository
import io.diasjakupov.mindtag.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StartQuizUseCaseTest {

    private val repository = FakeStudyRepository()
    private val useCase = StartQuizUseCase(repository)

    @Test
    fun createsSessionWithCorrectType() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ)

        assertEquals(SessionType.QUIZ, result.session.sessionType)
        assertEquals(SessionStatus.IN_PROGRESS, result.session.status)
    }

    @Test
    fun createsTimedSession() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ, timeLimitSeconds = 1800)

        assertEquals(SessionType.QUIZ, result.session.sessionType)
        assertEquals(1800, result.session.timeLimitSeconds)
    }

    @Test
    fun createsSessionWithDefaultQuestionCount() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ)

        assertEquals(10, result.session.totalQuestions)
    }

    @Test
    fun createsSessionWithCustomQuestionCount() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ, questionCount = 5)

        assertEquals(5, result.session.totalQuestions)
    }

    @Test
    fun createsSessionWithSubjectFilter() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ, subjectId = "subj-1")

        assertEquals("subj-1", result.session.subjectId)
    }

    @Test
    fun createsSessionWithNullSubject() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ)

        assertNull(result.session.subjectId)
    }

    @Test
    fun returnsFlashCardsFlow() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ, questionCount = 10)

        result.cards.test {
            val cards = awaitItem()
            assertEquals(2, cards.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun returnsFilteredFlashCardsBySubject() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ, subjectId = "subj-1", questionCount = 10)

        result.cards.test {
            val cards = awaitItem()
            assertEquals(1, cards.size)
            assertEquals("subj-1", cards.first().subjectId)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun sessionHasNoTimeLimitByDefault() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ)

        assertNull(result.session.timeLimitSeconds)
    }

    @Test
    fun sessionStartedAtIsSet() = runTest {
        repository.setFlashCards(TestData.flashCards)

        val result = useCase(type = SessionType.QUIZ)

        assertNotNull(result.session.startedAt)
        assertNull(result.session.finishedAt)
    }
}
