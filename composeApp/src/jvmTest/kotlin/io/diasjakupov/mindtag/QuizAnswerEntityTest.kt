package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizAnswerEntityTest {

    private lateinit var database: MindTagDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        val now = System.currentTimeMillis()
        // Set up required parent records
        database.subjectEntityQueries.insert("subj-bio", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.studySessionEntityQueries.insert("session-1", "subj-bio", "QUICK_QUIZ", now, null, 10, null, "IN_PROGRESS")
        database.studySessionEntityQueries.insert("session-2", "subj-bio", "EXAM_MODE", now, null, 20, 600, "IN_PROGRESS")
        database.flashCardEntityQueries.insert("card-1", "Q1", "FACT_CHECK", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-2", "Q2", "MULTIPLE_CHOICE", "MEDIUM", "subj-bio", "A2", null, null, null, 2.5, 0, 0, null, now)
    }

    @Test
    fun insertAndSelectById() {
        val now = System.currentTimeMillis()
        database.quizAnswerEntityQueries.insert(
            id = "answer-1",
            session_id = "session-1",
            card_id = "card-1",
            user_answer = "Prophase, Metaphase, Anaphase, Telophase",
            is_correct = 1,
            confidence_rating = "EASY",
            time_spent_seconds = 15,
            answered_at = now,
        )

        val answer = database.quizAnswerEntityQueries.selectById("answer-1").executeAsOneOrNull()
        assertNotNull(answer)
        assertEquals("session-1", answer.session_id)
        assertEquals("card-1", answer.card_id)
        assertEquals("Prophase, Metaphase, Anaphase, Telophase", answer.user_answer)
        assertEquals(1L, answer.is_correct)
        assertEquals("EASY", answer.confidence_rating)
        assertEquals(15L, answer.time_spent_seconds)
    }

    @Test
    fun selectBySessionIdOrderedByAnsweredAt() {
        val now = System.currentTimeMillis()
        database.quizAnswerEntityQueries.insert("a1", "session-1", "card-1", "wrong", 0, null, 10, now)
        database.quizAnswerEntityQueries.insert("a2", "session-1", "card-2", "correct", 1, "EASY", 8, now + 1000)
        database.quizAnswerEntityQueries.insert("a3", "session-2", "card-1", "answer", 1, null, 12, now)

        val session1Answers = database.quizAnswerEntityQueries.selectBySessionId("session-1").executeAsList()
        assertEquals(2, session1Answers.size)
        // Ordered by answered_at ASC
        assertEquals("a1", session1Answers[0].id)
        assertEquals("a2", session1Answers[1].id)
    }

    @Test
    fun countCorrectBySession() {
        val now = System.currentTimeMillis()
        database.quizAnswerEntityQueries.insert("a1", "session-1", "card-1", "wrong", 0, null, 10, now)
        database.quizAnswerEntityQueries.insert("a2", "session-1", "card-2", "correct", 1, "EASY", 8, now + 1000)

        val correctCount = database.quizAnswerEntityQueries.countCorrectBySession("session-1").executeAsOne()
        assertEquals(1L, correctCount)
    }

    @Test
    fun countCorrectBySessionWithAllCorrect() {
        val now = System.currentTimeMillis()
        database.quizAnswerEntityQueries.insert("a1", "session-1", "card-1", "A1", 1, "EASY", 10, now)
        database.quizAnswerEntityQueries.insert("a2", "session-1", "card-2", "A2", 1, null, 8, now + 1000)

        val correctCount = database.quizAnswerEntityQueries.countCorrectBySession("session-1").executeAsOne()
        assertEquals(2L, correctCount)
    }

    @Test
    fun countCorrectBySessionWithNoneCorrect() {
        val now = System.currentTimeMillis()
        database.quizAnswerEntityQueries.insert("a1", "session-1", "card-1", "wrong1", 0, null, 10, now)
        database.quizAnswerEntityQueries.insert("a2", "session-1", "card-2", "wrong2", 0, null, 8, now + 1000)

        val correctCount = database.quizAnswerEntityQueries.countCorrectBySession("session-1").executeAsOne()
        assertEquals(0L, correctCount)
    }

    @Test
    fun confidenceRatingCanBeNull() {
        val now = System.currentTimeMillis()
        database.quizAnswerEntityQueries.insert("a1", "session-1", "card-1", "answer", 1, null, 10, now)

        val answer = database.quizAnswerEntityQueries.selectById("a1").executeAsOneOrNull()
        assertNotNull(answer)
        assertNull(answer.confidence_rating)
    }

    @Test
    fun deleteAnswer() {
        val now = System.currentTimeMillis()
        database.quizAnswerEntityQueries.insert("a1", "session-1", "card-1", "answer", 1, null, 10, now)

        database.quizAnswerEntityQueries.delete("a1")

        val result = database.quizAnswerEntityQueries.selectById("a1").executeAsOneOrNull()
        assertNull(result)
    }

    @Test
    fun deleteBySessionId() {
        val now = System.currentTimeMillis()
        database.quizAnswerEntityQueries.insert("a1", "session-1", "card-1", "a", 1, null, 10, now)
        database.quizAnswerEntityQueries.insert("a2", "session-1", "card-2", "b", 0, null, 8, now + 1000)
        database.quizAnswerEntityQueries.insert("a3", "session-2", "card-1", "c", 1, null, 12, now)

        database.quizAnswerEntityQueries.deleteBySessionId("session-1")

        val session1Answers = database.quizAnswerEntityQueries.selectBySessionId("session-1").executeAsList()
        assertTrue(session1Answers.isEmpty())

        // Session-2 answers should remain
        val session2Answers = database.quizAnswerEntityQueries.selectBySessionId("session-2").executeAsList()
        assertEquals(1, session2Answers.size)
    }

    @Test
    fun deleteAllAnswers() {
        val now = System.currentTimeMillis()
        database.quizAnswerEntityQueries.insert("a1", "session-1", "card-1", "a", 1, null, 10, now)
        database.quizAnswerEntityQueries.insert("a2", "session-2", "card-2", "b", 0, null, 8, now)

        database.quizAnswerEntityQueries.deleteAll()

        val answers = database.quizAnswerEntityQueries.selectAll().executeAsList()
        assertTrue(answers.isEmpty())
    }
}
