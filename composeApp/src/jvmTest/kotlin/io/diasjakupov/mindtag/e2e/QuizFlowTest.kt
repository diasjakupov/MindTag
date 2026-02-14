package io.diasjakupov.mindtag.e2e

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.study.data.repository.QuizRepositoryImpl
import io.diasjakupov.mindtag.feature.study.data.repository.StudyRepositoryImpl
import io.diasjakupov.mindtag.feature.study.domain.model.AnswerOption
import io.diasjakupov.mindtag.feature.study.domain.model.SessionStatus
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.repository.QuizRepository
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository
import io.diasjakupov.mindtag.feature.study.domain.usecase.GetResultsUseCase
import io.diasjakupov.mindtag.feature.study.domain.usecase.StartQuizUseCase
import io.diasjakupov.mindtag.feature.study.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QuizFlowTest {

    private lateinit var database: MindTagDatabase
    private lateinit var studyRepository: StudyRepository
    private lateinit var quizRepository: QuizRepository
    private lateinit var startQuizUseCase: StartQuizUseCase
    private lateinit var submitAnswerUseCase: SubmitAnswerUseCase
    private lateinit var getResultsUseCase: GetResultsUseCase

    private val json = Json

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        seedTestData()

        studyRepository = StudyRepositoryImpl(database)
        quizRepository = QuizRepositoryImpl(database)
        startQuizUseCase = StartQuizUseCase(studyRepository)
        submitAnswerUseCase = SubmitAnswerUseCase(quizRepository, studyRepository)
        getResultsUseCase = GetResultsUseCase(quizRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Seed test data with properly serialized AnswerOption JSON
     * so that StudyRepositoryImpl.toDomain() can parse options correctly.
     */
    private fun seedTestData() {
        val now = System.currentTimeMillis()

        // Insert a subject
        database.subjectEntityQueries.insert(
            id = "subj-test",
            name = "Test Subject",
            color_hex = "#FF0000",
            icon_name = "book",
            progress = 0.0,
            total_notes = 1,
            reviewed_notes = 0,
            created_at = now,
            updated_at = now,
        )

        // Insert a second subject for subject-specific tests
        database.subjectEntityQueries.insert(
            id = "subj-bio",
            name = "Biology",
            color_hex = "#00FF00",
            icon_name = "leaf",
            progress = 0.5,
            total_notes = 1,
            reviewed_notes = 0,
            created_at = now,
            updated_at = now,
        )

        // Insert a note
        database.noteEntityQueries.insert(
            id = "note-test-1",
            title = "Test Note",
            content = "Test content",
            summary = "Test summary",
            subject_id = "subj-test",
            week_number = 1,
            read_time_minutes = 1,
            created_at = now,
            updated_at = now,
        )

        // Insert flashcards with properly serialized AnswerOption lists
        for (i in 1..10) {
            val options = listOf(
                AnswerOption("opt-${i}-1", "Option A for card $i", false),
                AnswerOption("opt-${i}-2", "Option B for card $i", false),
                AnswerOption("opt-${i}-3", "Correct answer for card $i", true),
                AnswerOption("opt-${i}-4", "Option D for card $i", false),
            )
            val optionsJson = json.encodeToString(options)
            val subjectId = if (i <= 5) "subj-test" else "subj-bio"

            database.flashCardEntityQueries.insert(
                id = "card-$i",
                question = "Question $i?",
                type = "MULTIPLE_CHOICE",
                difficulty = "MEDIUM",
                subject_id = subjectId,
                correct_answer = "Correct answer for card $i",
                options_json = optionsJson,
                source_note_ids_json = "[\"note-test-1\"]",
                ai_explanation = "Explanation for card $i",
                ease_factor = 2.5,
                interval_days = 0,
                repetitions = 0,
                next_review_at = null,
                created_at = now,
            )
        }

    }

    @Test
    fun startQuiz_createsSessionAndLoadsCards() = runTest {
        val quizData = startQuizUseCase(
            type = SessionType.QUIZ,
            questionCount = 5,
        )

        assertNotNull(quizData.session)
        assertEquals(SessionType.QUIZ, quizData.session.sessionType)
        assertEquals(5, quizData.session.totalQuestions)
        assertEquals(SessionStatus.IN_PROGRESS, quizData.session.status)

        quizData.cards.test {
            val cards = awaitItem()
            assertEquals(5, cards.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fullQuizFlow_startAnswerAndViewResults() = runTest {
        val quizData = startQuizUseCase(
            type = SessionType.QUIZ,
            questionCount = 3,
        )
        val sessionId = quizData.session.id
        val cards = quizData.cards.firstOrNull() ?: emptyList()
        assertTrue(cards.isNotEmpty(), "Should have cards loaded")
        assertEquals(3, cards.size)

        // Answer all 3 questions: first correct, second wrong, third correct
        cards.forEachIndexed { index, card ->
            val isCorrect = index != 1 // Second answer is wrong
            val userAnswer = if (isCorrect) card.correctAnswer else "Wrong answer"

            submitAnswerUseCase(
                sessionId = sessionId,
                cardId = card.id,
                userAnswer = userAnswer,
                isCorrect = isCorrect,
                confidenceRating = null,
                timeSpentSeconds = 10,
                currentQuestionIndex = index,
                totalQuestions = 3,
            )
        }

        // Verify session is completed (last question triggers completion)
        studyRepository.getSession(sessionId).test {
            val session = awaitItem()
            assertNotNull(session)
            assertEquals(SessionStatus.COMPLETED, session.status)
            assertNotNull(session.finishedAt)
            cancelAndIgnoreRemainingEvents()
        }

        // Verify results
        getResultsUseCase(sessionId).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(3, result.totalQuestions)
            assertEquals(2, result.totalCorrect)
            assertEquals(66, result.scorePercent) // 2/3 = 66%
            assertEquals(3, result.answers.size)
            assertTrue(result.answers[0].isCorrect)
            assertTrue(!result.answers[1].isCorrect)
            assertTrue(result.answers[2].isCorrect)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun examMode_createsSessionWithTimeLimit() = runTest {
        val quizData = startQuizUseCase(
            type = SessionType.QUIZ,
            questionCount = 5,
            timeLimitSeconds = 45 * 60,
        )

        assertEquals(SessionType.QUIZ, quizData.session.sessionType)
        assertEquals(5, quizData.session.totalQuestions)
        assertEquals(45 * 60, quizData.session.timeLimitSeconds)
    }

    @Test
    fun subjectSpecificQuiz_onlyLoadsCardsForSubject() = runTest {
        val quizData = startQuizUseCase(
            type = SessionType.QUIZ,
            subjectId = "subj-test",
            questionCount = 5,
        )

        quizData.cards.test {
            val cards = awaitItem()
            assertEquals(5, cards.size)
            assertTrue(cards.all { it.subjectId == "subj-test" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun submitAnswer_updatesCardSpacedRepetition() = runTest {
        val quizData = startQuizUseCase(
            type = SessionType.QUIZ,
            questionCount = 1,
        )
        val sessionId = quizData.session.id
        val cards = quizData.cards.firstOrNull() ?: emptyList()
        assertTrue(cards.isNotEmpty())
        val card = cards.first()

        // Get initial card state
        val initialCard = database.flashCardEntityQueries.selectById(card.id).executeAsOneOrNull()
        assertNotNull(initialCard)
        assertEquals(0L, initialCard.repetitions)

        // Submit a correct answer
        submitAnswerUseCase(
            sessionId = sessionId,
            cardId = card.id,
            userAnswer = card.correctAnswer,
            isCorrect = true,
            confidenceRating = null,
            timeSpentSeconds = 10,
            currentQuestionIndex = 0,
            totalQuestions = 1,
        )

        // Verify card schedule was updated
        val updatedCard = database.flashCardEntityQueries.selectById(card.id).executeAsOneOrNull()
        assertNotNull(updatedCard)
        assertTrue(updatedCard.repetitions > 0, "Repetitions should be incremented")
        assertNotNull(updatedCard.next_review_at, "Next review date should be set")
        assertTrue(updatedCard.interval_days >= 1, "Interval should be at least 1 day")
    }

    @Test
    fun quizAnswers_persistedCorrectly() = runTest {
        val quizData = startQuizUseCase(
            type = SessionType.QUIZ,
            questionCount = 2,
        )
        val sessionId = quizData.session.id
        val cards = quizData.cards.firstOrNull() ?: emptyList()
        assertEquals(2, cards.size)

        // Submit answers for both questions
        submitAnswerUseCase(
            sessionId = sessionId,
            cardId = cards[0].id,
            userAnswer = cards[0].correctAnswer,
            isCorrect = true,
            confidenceRating = null,
            timeSpentSeconds = 5,
            currentQuestionIndex = 0,
            totalQuestions = 2,
        )
        submitAnswerUseCase(
            sessionId = sessionId,
            cardId = cards[1].id,
            userAnswer = "Wrong",
            isCorrect = false,
            confidenceRating = null,
            timeSpentSeconds = 15,
            currentQuestionIndex = 1,
            totalQuestions = 2,
        )

        // Verify answers are stored in DB
        val answers = database.quizAnswerEntityQueries.selectBySessionId(sessionId).executeAsList()
        assertEquals(2, answers.size)
        assertEquals(1L, answers[0].is_correct)
        assertEquals(0L, answers[1].is_correct)
    }

    @Test
    fun allCorrectAnswers_resultsShow100Percent() = runTest {
        val quizData = startQuizUseCase(
            type = SessionType.QUIZ,
            questionCount = 3,
        )
        val sessionId = quizData.session.id
        val cards = quizData.cards.firstOrNull() ?: emptyList()
        assertEquals(3, cards.size)

        cards.forEachIndexed { index, card ->
            submitAnswerUseCase(
                sessionId = sessionId,
                cardId = card.id,
                userAnswer = card.correctAnswer,
                isCorrect = true,
                confidenceRating = null,
                timeSpentSeconds = 5,
                currentQuestionIndex = index,
                totalQuestions = 3,
            )
        }

        getResultsUseCase(sessionId).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(100, result.scorePercent)
            assertEquals(3, result.totalCorrect)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun multipleChoiceCards_haveProperOptions() = runTest {
        val quizData = startQuizUseCase(
            type = SessionType.QUIZ,
            questionCount = 1,
        )
        val cards = quizData.cards.firstOrNull() ?: emptyList()
        assertTrue(cards.isNotEmpty())

        val card = cards.first()
        assertEquals(4, card.options.size)
        assertTrue(card.options.any { it.isCorrect }, "Should have at least one correct option")
        assertTrue(card.options.count { it.isCorrect } == 1, "Should have exactly one correct option")
    }
}
