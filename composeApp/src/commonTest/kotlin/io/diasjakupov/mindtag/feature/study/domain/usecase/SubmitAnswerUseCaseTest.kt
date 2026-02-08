package io.diasjakupov.mindtag.feature.study.domain.usecase

import app.cash.turbine.test
import io.diasjakupov.mindtag.feature.study.domain.model.ConfidenceRating
import io.diasjakupov.mindtag.feature.study.domain.model.SessionStatus
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.test.FakeQuizRepository
import io.diasjakupov.mindtag.test.FakeStudyRepository
import io.diasjakupov.mindtag.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class SubmitAnswerUseCaseTest {

    private val quizRepository = FakeQuizRepository()
    private val studyRepository = FakeStudyRepository()
    private val useCase = SubmitAnswerUseCase(quizRepository, studyRepository)

    @Test
    fun submitsAnswerToQuizRepository() = runTest {
        useCase(
            sessionId = "session-1",
            cardId = "card-1",
            userAnswer = "answer",
            isCorrect = true,
            confidenceRating = ConfidenceRating.EASY,
            timeSpentSeconds = 10,
            currentQuestionIndex = 0,
            totalQuestions = 5,
        )

        val answers = quizRepository.getSubmittedAnswers()
        assertEquals(1, answers.size)
        assertEquals("session-1", answers.first().sessionId)
        assertEquals("card-1", answers.first().cardId)
        assertEquals("answer", answers.first().userAnswer)
        assertTrue(answers.first().isCorrect)
    }

    @Test
    fun updatesCardSchedule() = runTest {
        useCase(
            sessionId = "session-1",
            cardId = "card-1",
            userAnswer = "answer",
            isCorrect = true,
            confidenceRating = ConfidenceRating.EASY,
            timeSpentSeconds = 10,
            currentQuestionIndex = 0,
            totalQuestions = 5,
        )

        val schedules = quizRepository.getUpdatedSchedules()
        assertEquals(1, schedules.size)
        assertEquals("card-1", schedules.first().first)
        assertTrue(schedules.first().second)
        assertEquals(ConfidenceRating.EASY, schedules.first().third)
    }

    @Test
    fun returnsFalseWhenNotLastQuestion() = runTest {
        val isLast = useCase(
            sessionId = "session-1",
            cardId = "card-1",
            userAnswer = "answer",
            isCorrect = true,
            confidenceRating = null,
            timeSpentSeconds = 10,
            currentQuestionIndex = 0,
            totalQuestions = 5,
        )

        assertFalse(isLast)
    }

    @Test
    fun returnsTrueWhenLastQuestion() = runTest {
        val isLast = useCase(
            sessionId = "session-1",
            cardId = "card-1",
            userAnswer = "answer",
            isCorrect = true,
            confidenceRating = null,
            timeSpentSeconds = 10,
            currentQuestionIndex = 4,
            totalQuestions = 5,
        )

        assertTrue(isLast)
    }

    @Test
    fun completesSessionOnLastQuestion() = runTest {
        val session = studyRepository.createSession(
            type = SessionType.QUICK_QUIZ,
            subjectId = null,
            questionCount = 2,
        )

        useCase(
            sessionId = session.id,
            cardId = "card-1",
            userAnswer = "answer",
            isCorrect = true,
            confidenceRating = null,
            timeSpentSeconds = 10,
            currentQuestionIndex = 1,
            totalQuestions = 2,
        )

        studyRepository.getSession(session.id).test {
            assertEquals(SessionStatus.COMPLETED, awaitItem()?.status)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun doesNotCompleteSessionOnMiddleQuestion() = runTest {
        val session = studyRepository.createSession(
            type = SessionType.QUICK_QUIZ,
            subjectId = null,
            questionCount = 5,
        )

        useCase(
            sessionId = session.id,
            cardId = "card-1",
            userAnswer = "answer",
            isCorrect = false,
            confidenceRating = ConfidenceRating.HARD,
            timeSpentSeconds = 20,
            currentQuestionIndex = 2,
            totalQuestions = 5,
        )

        studyRepository.getSession(session.id).test {
            assertEquals(SessionStatus.IN_PROGRESS, awaitItem()?.status)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun handlesNullConfidenceRating() = runTest {
        useCase(
            sessionId = "session-1",
            cardId = "card-1",
            userAnswer = "answer",
            isCorrect = false,
            confidenceRating = null,
            timeSpentSeconds = 15,
            currentQuestionIndex = 0,
            totalQuestions = 5,
        )

        val schedules = quizRepository.getUpdatedSchedules()
        assertEquals(null, schedules.first().third)
    }

    @Test
    fun submitsIncorrectAnswer() = runTest {
        useCase(
            sessionId = "session-1",
            cardId = "card-2",
            userAnswer = "wrong",
            isCorrect = false,
            confidenceRating = ConfidenceRating.HARD,
            timeSpentSeconds = 25,
            currentQuestionIndex = 0,
            totalQuestions = 5,
        )

        val answers = quizRepository.getSubmittedAnswers()
        assertFalse(answers.first().isCorrect)
        assertEquals("wrong", answers.first().userAnswer)
    }

    @Test
    fun generatesUniqueAnswerIds() = runTest {
        useCase(
            sessionId = "session-1",
            cardId = "card-1",
            userAnswer = "a1",
            isCorrect = true,
            confidenceRating = null,
            timeSpentSeconds = 5,
            currentQuestionIndex = 0,
            totalQuestions = 3,
        )
        useCase(
            sessionId = "session-1",
            cardId = "card-2",
            userAnswer = "a2",
            isCorrect = false,
            confidenceRating = null,
            timeSpentSeconds = 8,
            currentQuestionIndex = 1,
            totalQuestions = 3,
        )

        val answers = quizRepository.getSubmittedAnswers()
        assertEquals(2, answers.size)
        assertTrue(answers[0].id != answers[1].id)
    }

    @Test
    fun boundaryLastQuestionIndexEqualsTotal() = runTest {
        // currentQuestionIndex >= totalQuestions - 1 means last question
        val isLast = useCase(
            sessionId = "session-1",
            cardId = "card-1",
            userAnswer = "answer",
            isCorrect = true,
            confidenceRating = null,
            timeSpentSeconds = 5,
            currentQuestionIndex = 5,
            totalQuestions = 5,
        )

        assertTrue(isLast)
    }
}
