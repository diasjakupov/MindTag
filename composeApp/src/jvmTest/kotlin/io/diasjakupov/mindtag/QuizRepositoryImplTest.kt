package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.study.data.repository.QuizRepositoryImpl
import io.diasjakupov.mindtag.feature.study.domain.model.ConfidenceRating
import io.diasjakupov.mindtag.feature.study.domain.model.QuizAnswer
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizRepositoryImplTest {

    private lateinit var database: MindTagDatabase
    private lateinit var repository: QuizRepositoryImpl

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)
        repository = QuizRepositoryImpl(database)

        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-bio", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.flashCardEntityQueries.insert("card-1", "What are the stages of mitosis?", "MULTIPLE_CHOICE", "MEDIUM", "subj-bio", "PMAT", null, null, "Remember PMAT", 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-2", "What is DNA?", "MULTIPLE_CHOICE", "EASY", "subj-bio", "Deoxyribonucleic acid", null, null, "The molecule of heredity", 2.5, 0, 0, null, now)
        database.studySessionEntityQueries.insert("session-1", "subj-bio", "QUIZ", now, null, 10, null, "IN_PROGRESS")
    }

    @Test
    fun submitAnswerPersistsToDatabase() = runTest {
        val now = System.currentTimeMillis()
        val answer = QuizAnswer(
            id = "answer-1",
            sessionId = "session-1",
            cardId = "card-1",
            userAnswer = "PMAT",
            isCorrect = true,
            confidenceRating = ConfidenceRating.EASY,
            timeSpentSeconds = 15,
            answeredAt = now,
        )

        repository.submitAnswer(answer)

        val stored = database.quizAnswerEntityQueries.selectById("answer-1").executeAsOneOrNull()
        assertNotNull(stored)
        assertEquals("session-1", stored.session_id)
        assertEquals("card-1", stored.card_id)
        assertEquals("PMAT", stored.user_answer)
        assertEquals(1L, stored.is_correct)
        assertEquals("EASY", stored.confidence_rating)
        assertEquals(15L, stored.time_spent_seconds)
    }

    @Test
    fun submitIncorrectAnswer() = runTest {
        val now = System.currentTimeMillis()
        val answer = QuizAnswer(
            id = "answer-1",
            sessionId = "session-1",
            cardId = "card-1",
            userAnswer = "Wrong answer",
            isCorrect = false,
            confidenceRating = null,
            timeSpentSeconds = 20,
            answeredAt = now,
        )

        repository.submitAnswer(answer)

        val stored = database.quizAnswerEntityQueries.selectById("answer-1").executeAsOneOrNull()
        assertNotNull(stored)
        assertEquals(0L, stored.is_correct)
        assertNull(stored.confidence_rating)
    }

    @Test
    fun getSessionResultsReturnsCompleteResult() = runTest {
        val now = System.currentTimeMillis()

        // Complete the session
        database.studySessionEntityQueries.finish(
            finished_at = now + 120_000,
            status = "COMPLETED",
            id = "session-1",
        )

        // Submit answers
        repository.submitAnswer(QuizAnswer("a1", "session-1", "card-1", "PMAT", true, ConfidenceRating.EASY, 15, now))
        repository.submitAnswer(QuizAnswer("a2", "session-1", "card-2", "Wrong", false, null, 20, now + 1000))

        repository.getSessionResults("session-1").test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(50, result.scorePercent) // 1 out of 2
            assertEquals(1, result.totalCorrect)
            assertEquals(2, result.totalQuestions)
            assertEquals(2, result.answers.size)

            // Check answer details
            val correctAnswer = result.answers.find { it.cardId == "card-1" }
            assertNotNull(correctAnswer)
            assertTrue(correctAnswer.isCorrect)
            assertEquals("What are the stages of mitosis?", correctAnswer.question)
            assertEquals("PMAT", correctAnswer.correctAnswer)

            val wrongAnswer = result.answers.find { it.cardId == "card-2" }
            assertNotNull(wrongAnswer)
            assertTrue(!wrongAnswer.isCorrect)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSessionResultsReturnsNullForNonExistentSession() = runTest {
        repository.getSessionResults("nonexistent").test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSessionResultsWithNoAnswers() = runTest {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.finish(
            finished_at = now + 60_000,
            status = "COMPLETED",
            id = "session-1",
        )

        repository.getSessionResults("session-1").test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(0, result.totalCorrect)
            assertTrue(result.answers.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateCardScheduleCorrectAnswerEasy() = runTest {
        repository.updateCardSchedule("card-1", isCorrect = true, confidence = ConfidenceRating.EASY)

        val card = database.flashCardEntityQueries.selectById("card-1").executeAsOneOrNull()
        assertNotNull(card)
        assertEquals(1L, card.repetitions)
        assertEquals(1L, card.interval_days)
        assertNotNull(card.next_review_at)
        // Ease factor should increase for quality=5
        assertTrue(card.ease_factor > 2.5)
    }

    @Test
    fun updateCardScheduleCorrectAnswerHard() = runTest {
        repository.updateCardSchedule("card-1", isCorrect = true, confidence = ConfidenceRating.HARD)

        val card = database.flashCardEntityQueries.selectById("card-1").executeAsOneOrNull()
        assertNotNull(card)
        assertEquals(1L, card.repetitions)
        assertNotNull(card.next_review_at)
    }

    @Test
    fun updateCardScheduleIncorrectResets() = runTest {
        // First, simulate a card that has been reviewed before
        database.flashCardEntityQueries.updateSpacedRepetition(
            ease_factor = 2.6,
            interval_days = 6,
            repetitions = 2,
            next_review_at = System.currentTimeMillis() + 86_400_000,
            id = "card-1",
        )

        repository.updateCardSchedule("card-1", isCorrect = false, confidence = null)

        val card = database.flashCardEntityQueries.selectById("card-1").executeAsOneOrNull()
        assertNotNull(card)
        // SM-2: incorrect answer resets repetitions to 0 and interval to 1
        assertEquals(0L, card.repetitions)
        assertEquals(1L, card.interval_days)
    }

    @Test
    fun updateCardScheduleForNonExistentCardDoesNothing() = runTest {
        // Should not throw, just return early
        repository.updateCardSchedule("nonexistent", isCorrect = true, confidence = ConfidenceRating.EASY)
    }

    @Test
    fun getSessionResultsFormatsTimeCorrectly() = runTest {
        val now = System.currentTimeMillis()
        // Session lasted 2 minutes 30 seconds
        database.studySessionEntityQueries.finish(
            finished_at = now + 150_000, // 150 seconds
            status = "COMPLETED",
            id = "session-1",
        )

        repository.getSessionResults("session-1").test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("2m 30s", result.timeSpentFormatted)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
