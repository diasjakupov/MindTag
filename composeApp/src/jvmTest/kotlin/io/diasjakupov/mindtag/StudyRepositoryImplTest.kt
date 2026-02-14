package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.study.data.repository.StudyRepositoryImpl
import io.diasjakupov.mindtag.feature.study.domain.model.SessionStatus
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StudyRepositoryImplTest {

    private lateinit var database: MindTagDatabase
    private lateinit var repository: StudyRepositoryImpl

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)
        repository = StudyRepositoryImpl(database)

        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-bio", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-cs", "CS", "#135BEC", "code", 0.0, 0, 0, now, now)
    }

    @Test
    fun createSessionReturnsSessionWithGeneratedId() = runTest {
        val session = repository.createSession(
            type = SessionType.QUIZ,
            subjectId = "subj-bio",
            questionCount = 10,
            timeLimitSeconds = null,
        )

        assertNotNull(session.id)
        assertTrue(session.id.isNotBlank())
        assertEquals("subj-bio", session.subjectId)
        assertEquals(SessionType.QUIZ, session.sessionType)
        assertEquals(10, session.totalQuestions)
        assertNull(session.timeLimitSeconds)
        assertEquals(SessionStatus.IN_PROGRESS, session.status)
        assertNull(session.finishedAt)
    }

    @Test
    fun createExamModeSessionWithTimeLimit() = runTest {
        val session = repository.createSession(
            type = SessionType.QUIZ,
            subjectId = "subj-bio",
            questionCount = 20,
            timeLimitSeconds = 600,
        )

        assertEquals(SessionType.QUIZ, session.sessionType)
        assertEquals(20, session.totalQuestions)
        assertEquals(600, session.timeLimitSeconds)
    }

    @Test
    fun createSessionWithNullSubjectId() = runTest {
        val session = repository.createSession(
            type = SessionType.QUIZ,
            subjectId = null,
            questionCount = 5,
        )

        assertNull(session.subjectId)
    }

    @Test
    fun getSessionReturnsCreatedSession() = runTest {
        val created = repository.createSession(
            type = SessionType.QUIZ,
            subjectId = "subj-bio",
            questionCount = 10,
        )

        repository.getSession(created.id).test {
            val session = awaitItem()
            assertNotNull(session)
            assertEquals(created.id, session.id)
            assertEquals(SessionType.QUIZ, session.sessionType)
            assertEquals(SessionStatus.IN_PROGRESS, session.status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSessionReturnsNullForNonExistent() = runTest {
        repository.getSession("nonexistent").test {
            val session = awaitItem()
            assertNull(session)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun completeSessionUpdatesStatus() = runTest {
        val created = repository.createSession(
            type = SessionType.QUIZ,
            subjectId = "subj-bio",
            questionCount = 10,
        )

        repository.completeSession(created.id)

        repository.getSession(created.id).test {
            val session = awaitItem()
            assertNotNull(session)
            assertEquals(SessionStatus.COMPLETED, session.status)
            assertNotNull(session.finishedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCardsForSessionReturnsDueCards() = runTest {
        val now = System.currentTimeMillis()
        // Insert flash cards - all with null next_review_at (never reviewed, so all are due)
        database.flashCardEntityQueries.insert("card-1", "Q1", "MULTIPLE_CHOICE", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-2", "Q2", "MULTIPLE_CHOICE", "MEDIUM", "subj-bio", "A2", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-3", "Q3", "TRUE_FALSE", "HARD", "subj-cs", "A3", null, null, null, 2.5, 0, 0, null, now)

        repository.getCardsForSession(subjectId = "subj-bio", count = 5).test {
            val cards = awaitItem()
            assertEquals(2, cards.size)
            assertTrue(cards.all { it.subjectId == "subj-bio" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCardsForSessionWithNullSubjectReturnsAllDueCards() = runTest {
        val now = System.currentTimeMillis()
        database.flashCardEntityQueries.insert("card-1", "Q1", "MULTIPLE_CHOICE", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-2", "Q2", "MULTIPLE_CHOICE", "EASY", "subj-cs", "A2", null, null, null, 2.5, 0, 0, null, now)

        repository.getCardsForSession(subjectId = null, count = 10).test {
            val cards = awaitItem()
            assertEquals(2, cards.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCardsForSessionRespectsCount() = runTest {
        val now = System.currentTimeMillis()
        database.flashCardEntityQueries.insert("card-1", "Q1", "MULTIPLE_CHOICE", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-2", "Q2", "MULTIPLE_CHOICE", "EASY", "subj-bio", "A2", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-3", "Q3", "MULTIPLE_CHOICE", "EASY", "subj-bio", "A3", null, null, null, 2.5, 0, 0, null, now)

        repository.getCardsForSession(subjectId = "subj-bio", count = 2).test {
            val cards = awaitItem()
            assertEquals(2, cards.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCardsForSessionFillsWithNonDueWhenNotEnoughDue() = runTest {
        val now = System.currentTimeMillis()
        val future = now + 86_400_000L * 30 // 30 days in the future

        // 1 due card, 2 not due
        database.flashCardEntityQueries.insert("card-due", "Q1", "MULTIPLE_CHOICE", "EASY", "subj-bio", "A1", null, null, null, 2.5, 0, 0, null, now)
        database.flashCardEntityQueries.insert("card-notdue-1", "Q2", "MULTIPLE_CHOICE", "EASY", "subj-bio", "A2", null, null, null, 2.5, 6, 2, future, now)
        database.flashCardEntityQueries.insert("card-notdue-2", "Q3", "MULTIPLE_CHOICE", "EASY", "subj-bio", "A3", null, null, null, 2.5, 6, 2, future, now)

        repository.getCardsForSession(subjectId = "subj-bio", count = 3).test {
            val cards = awaitItem()
            assertEquals(3, cards.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun flashCardDomainModelIsCorrectlyMapped() = runTest {
        val now = System.currentTimeMillis()
        val optionsJson = """[{"id":"1","text":"Option A","isCorrect":true},{"id":"2","text":"Option B","isCorrect":false}]"""
        database.flashCardEntityQueries.insert(
            "card-1", "What is 1+1?", "MULTIPLE_CHOICE", "EASY", "subj-bio", "2",
            optionsJson, "[\"note-1\"]", "Basic math", 2.5, 0, 0, null, now,
        )

        repository.getCardsForSession(subjectId = "subj-bio", count = 1).test {
            val cards = awaitItem()
            assertEquals(1, cards.size)
            val card = cards[0]
            assertEquals("What is 1+1?", card.question)
            assertEquals("2", card.correctAnswer)
            assertEquals(2, card.options.size)
            assertEquals("Option A", card.options[0].text)
            assertEquals(true, card.options[0].isCorrect)
            assertEquals(listOf("note-1"), card.sourceNoteIds)
            assertEquals("Basic math", card.aiExplanation)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
