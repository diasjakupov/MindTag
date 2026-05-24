package io.diasjakupov.mindtag.feature.backendquiz.data.repository

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.dto.AnswerRequestDto
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptStart
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuestionResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizDetail
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizQuestion
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizSummary
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.random.Random

/**
 * Stub [BackendQuizRepository] used in [io.diasjakupov.mindtag.core.config.AppEnvironment.TEST] mode.
 *
 * Pre-seeded with three realistic READY quizzes (Quiz A note 3 Polymorphism, Quiz B note 5 Big-O,
 * Quiz C note 10 Hash tables). The note ids match those in StubNoteRepository so "Quiz Me" works on
 * the hero notes. All mutations (generate, delete, start attempt, submit answers) operate on
 * in-memory state only, with artificial network-style delays so loading states render on camera.
 *
 * The attempt result deliberately returns a MIX of correct and wrong answers (~75-80%) so the
 * results screen shows both green and red detailed-analysis cards plus the AI-insight block.
 *
 * No real network calls are made.
 */
class StubBackendQuizRepository : BackendQuizRepository {

    private val now: String
        get() = Clock.System.now().toString()

    // ─── Quiz A — note 3, Polymorphism (5 questions) ──────────────────────────

    private val polymorphismQuestions = listOf(
        QuizQuestion(
            id = 301L,
            questionText = "What does dynamic dispatch decide at run time?",
            options = listOf(
                "The method to call based on the object's actual type",
                "The amount of memory to allocate",
                "The order of class loading",
                "The compiler optimisation level",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 302L,
            questionText = "Polymorphism most directly lets you replace what?",
            options = listOf(
                "Long if/switch chains on a type tag",
                "Recursion with iteration",
                "Arrays with linked lists",
                "Private fields with public ones",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 303L,
            questionText = "Which is required for runtime polymorphism in most OO languages?",
            options = listOf(
                "A shared interface or base type",
                "A static method",
                "A final class",
                "A global variable",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 304L,
            questionText = "A List<Shape> calling area() on each element demonstrates —",
            options = listOf("Polymorphism", "Encapsulation", "Memoisation", "Hashing"),
            orderIndex = 3,
        ),
        QuizQuestion(
            id = 305L,
            questionText = "Which design pattern leans most directly on polymorphism?",
            options = listOf("Strategy", "Singleton", "Flyweight", "Memento"),
            orderIndex = 4,
        ),
    )

    // ─── Quiz B — note 5, Big-O (4 questions) ─────────────────────────────────

    private val bigOQuestions = listOf(
        QuizQuestion(
            id = 501L,
            questionText = "O(n log n) is the typical complexity of —",
            options = listOf(
                "Efficient comparison sorts",
                "Hash lookups",
                "Array indexing",
                "Constant work",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 502L,
            questionText = "Big-O ignores —",
            options = listOf(
                "Constants and lower-order terms",
                "The worst case",
                "The input size",
                "The algorithm entirely",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 503L,
            questionText = "Average-case lookup in a well-sized hash table is —",
            options = listOf("O(1)", "O(log n)", "O(n)", "O(n^2)"),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 504L,
            questionText = "Quadratic time is written as —",
            options = listOf("O(n^2)", "O(n)", "O(log n)", "O(1)"),
            orderIndex = 3,
        ),
    )

    // ─── Quiz C — note 10, Hash tables (4 questions) ──────────────────────────

    private val hashQuestions = listOf(
        QuizQuestion(
            id = 1001L,
            questionText = "Two keys hashing to the same bucket is called a —",
            options = listOf("Collision", "Rotation", "Rehash", "Probe"),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 1002L,
            questionText = "Chaining resolves collisions by —",
            options = listOf(
                "Storing a list per bucket",
                "Resizing immediately",
                "Rejecting the key",
                "Sorting the bucket",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 1003L,
            questionText = "Hash-table performance degrades to O(n) when —",
            options = listOf(
                "Most keys collide",
                "The table is empty",
                "Keys are integers",
                "The load factor is low",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 1004L,
            questionText = "The load factor measures —",
            options = listOf(
                "How full the table is",
                "The hash length",
                "The key size",
                "The probe count",
            ),
            orderIndex = 3,
        ),
    )

    // ─── In-memory quiz store ─────────────────────────────────────────────────

    private val quizzes = mutableListOf(
        QuizSummary(
            id = 1L,
            noteId = 3L,
            noteTitleSnapshot = "Polymorphism and dynamic dispatch",
            status = QuizStatus.READY,
            questionCount = polymorphismQuestions.size,
            createdAt = "2026-04-20T10:00:00Z",
            generatedAt = "2026-04-20T10:00:30Z",
        ),
        QuizSummary(
            id = 2L,
            noteId = 5L,
            noteTitleSnapshot = "Big-O notation and asymptotic analysis",
            status = QuizStatus.READY,
            questionCount = bigOQuestions.size,
            createdAt = "2026-04-21T14:00:00Z",
            generatedAt = "2026-04-21T14:00:45Z",
        ),
        QuizSummary(
            id = 3L,
            noteId = 10L,
            noteTitleSnapshot = "Hash tables and collision resolution",
            status = QuizStatus.READY,
            questionCount = hashQuestions.size,
            createdAt = "2026-04-22T09:00:00Z",
            generatedAt = "2026-04-22T09:00:40Z",
        ),
    )

    private val attempts = mutableListOf<AttemptResult>()
    private var nextAttemptId = 100L
    private var nextQuizId = 4L

    private val letters = listOf("A", "B", "C", "D")

    // ─── Quiz CRUD ────────────────────────────────────────────────────────────

    override suspend fun generateQuiz(noteId: Long): ApiResult<QuizSummary> {
        delay(Random.nextLong(1400, 1600)) // ~1500ms — kicks off "Generating quiz..." status
        // Check for duplicate
        val existing = quizzes.find { it.noteId == noteId && it.status != QuizStatus.ERROR }
        if (existing != null) {
            return ApiResult.Error("Quiz already exists for this note", 409)
        }
        val newQuiz = QuizSummary(
            id = nextQuizId++,
            noteId = noteId,
            noteTitleSnapshot = "Stub Note $noteId",
            status = QuizStatus.READY,
            questionCount = polymorphismQuestions.size,
            createdAt = now,
            generatedAt = now,
        )
        quizzes.add(newQuiz)
        return ApiResult.Success(newQuiz)
    }

    override suspend fun pollUntilReady(
        quizId: Long,
        maxAttempts: Int,
        intervalMs: Long,
    ): ApiResult<QuizSummary> {
        delay(Random.nextLong(2000, 2400)) // ~2200ms — "Waiting for AI..." marquee F5 loading moment
        val quiz = quizzes.find { it.id == quizId }
            ?: return ApiResult.Error("Quiz $quizId not found", 404)
        return ApiResult.Success(quiz)
    }

    override suspend fun getQuiz(quizId: Long): ApiResult<QuizDetail> {
        val summary = quizzes.find { it.id == quizId }
            ?: return ApiResult.Error("Quiz $quizId not found", 404)
        val questions = questionsForQuiz(quizId)
        return ApiResult.Success(
            QuizDetail(
                id = summary.id,
                noteId = summary.noteId,
                noteTitleSnapshot = summary.noteTitleSnapshot,
                status = summary.status,
                questions = questions,
                createdAt = summary.createdAt,
                generatedAt = summary.generatedAt,
            )
        )
    }

    override suspend fun getAllQuizzes(): ApiResult<List<QuizSummary>> =
        ApiResult.Success(quizzes.toList())

    override suspend fun getQuizzesForNote(noteId: Long): ApiResult<List<QuizSummary>> =
        ApiResult.Success(quizzes.filter { it.noteId == noteId })

    override suspend fun deleteQuiz(quizId: Long): ApiResult<Unit> {
        val removed = quizzes.removeAll { it.id == quizId }
        return if (removed) ApiResult.Success(Unit)
        else ApiResult.Error("Quiz $quizId not found", 404)
    }

    // ─── Attempts ─────────────────────────────────────────────────────────────

    override suspend fun startAttempt(quizId: Long): ApiResult<AttemptStart> {
        delay(Random.nextLong(450, 650)) // ~500ms — "Starting quiz..."
        val summary = quizzes.find { it.id == quizId }
            ?: return ApiResult.Error("Quiz $quizId not found", 404)
        if (summary.status != QuizStatus.READY) {
            return ApiResult.Error("Quiz is not in READY status", 409)
        }
        return ApiResult.Success(
            AttemptStart(
                attemptId = nextAttemptId++,
                quizId = quizId,
                noteTitleSnapshot = summary.noteTitleSnapshot,
                startedAt = now,
                questions = questionsForQuiz(quizId),
            )
        )
    }

    override suspend fun submitAttempt(
        quizId: Long,
        attemptId: Long,
        answers: List<AnswerRequestDto>,
    ): ApiResult<AttemptResult> {
        delay(Random.nextLong(1000, 1200)) // ~1100ms — server scoring
        val questions = questionsForQuiz(quizId)
        val correctAnswers = correctAnswersForQuiz(quizId)
        // Question ids that are forced to read as WRONG so the result is a believable mix
        // (~75-80%). These also feed the F6 spaced-repetition "wrong material resurfaces" story.
        val forcedWrong = forcedWrongForQuiz(quizId)

        val questionResults = questions.map { q ->
            val correct = correctAnswers[q.id] ?: "A"
            // Hardcode the demo answers: every question reads as correct except the forced-wrong
            // ones, which take a deterministic incorrect option. This guarantees the green/red mix
            // on camera regardless of what the user tapped.
            val userAnswer = if (q.id in forcedWrong) {
                letters.firstOrNull { it != correct } ?: correct
            } else {
                correct
            }
            QuestionResult(
                questionId = q.id,
                questionText = q.questionText,
                options = q.options,
                userAnswer = userAnswer,
                correctAnswer = correct,
                correct = userAnswer == correct,
                explanation = explanationForQuestion(q.id),
            )
        }

        val correctCount = questionResults.count { it.correct }
        val score = if (questions.isNotEmpty()) (correctCount * 100) / questions.size else 0

        val result = AttemptResult(
            attemptId = attemptId,
            quizId = quizId,
            noteTitleSnapshot = quizzes.find { it.id == quizId }?.noteTitleSnapshot ?: "",
            score = score,
            totalQuestions = questions.size,
            correctAnswers = correctCount,
            startedAt = now,
            completedAt = now,
            questionResults = questionResults,
        )
        attempts.add(result)
        return ApiResult.Success(result)
    }

    override suspend fun getAttemptResult(quizId: Long, attemptId: Long): ApiResult<AttemptResult> {
        delay(Random.nextLong(700, 900)) // ~800ms — result fetch
        val result = attempts.find { it.attemptId == attemptId && it.quizId == quizId }
            ?: return ApiResult.Error("Attempt $attemptId not found", 404)
        return ApiResult.Success(result)
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun questionsForQuiz(quizId: Long): List<QuizQuestion> = when (quizId) {
        1L -> polymorphismQuestions
        2L -> bigOQuestions
        3L -> hashQuestions
        else -> polymorphismQuestions // default for dynamically-generated stub quizzes
    }

    /** Maps questionId -> correct answer letter (A/B/C/D based on correct option index). */
    private fun correctAnswersForQuiz(quizId: Long): Map<Long, String> {
        val correctIndices: Map<Long, Int> = when (quizId) {
            1L -> mapOf(301L to 0, 302L to 0, 303L to 0, 304L to 0, 305L to 0)
            2L -> mapOf(501L to 0, 502L to 0, 503L to 0, 504L to 0)
            3L -> mapOf(1001L to 0, 1002L to 0, 1003L to 0, 1004L to 0)
            else -> mapOf(301L to 0, 302L to 0, 303L to 0, 304L to 0, 305L to 0)
        }
        return correctIndices.mapValues { (_, idx) ->
            letters.getOrElse(idx) { "A" }
        }
    }

    /**
     * Question ids forced to read as wrong on the result screen, to produce a ~75-80% mixed score.
     * Quiz A (5 q): 1 wrong → 80%. Quiz B (4 q): 1 wrong → 75%. Quiz C (4 q): 1 wrong → 75%.
     */
    private fun forcedWrongForQuiz(quizId: Long): Set<Long> = when (quizId) {
        1L -> setOf(303L) // missed "shared interface/base type"
        2L -> setOf(504L) // missed "O(n^2)"
        3L -> setOf(1003L) // missed "most keys collide"
        else -> setOf(303L)
    }

    private fun explanationForQuestion(questionId: Long): String = when (questionId) {
        301L -> "The runtime resolves the call from the object's real type, not its declared type."
        302L -> "Polymorphism removes long if/switch chains on a type tag by dispatching on type."
        303L -> "Runtime polymorphism needs a shared interface or base type the caller programs against."
        304L -> "Calling area() on each Shape runs that shape's own implementation — polymorphism."
        305L -> "Strategy swaps interchangeable algorithms behind one interface, leaning entirely on polymorphism."
        501L -> "Efficient comparison sorts like merge sort and heapsort run in O(n log n)."
        502L -> "Big-O drops constants and lower-order terms, keeping only the dominant growth rate."
        503L -> "A well-sized hash table gives average O(1) lookup thanks to direct bucket addressing."
        504L -> "Quadratic time is O(n^2) — runtime grows with the square of the input size."
        1001L -> "Two keys landing in the same bucket is a collision, resolved by chaining or probing."
        1002L -> "Chaining stores a list (or chain) of entries per bucket so colliding keys coexist."
        1003L -> "When most keys collide, lookups walk long chains and degrade from O(1) to O(n)."
        1004L -> "The load factor is the ratio of stored entries to buckets — how full the table is."
        else -> "See your notes for a detailed explanation."
    }
}
