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
import kotlinx.datetime.Clock

/**
 * Stub [BackendQuizRepository] used in [io.diasjakupov.mindtag.core.config.AppEnvironment.TEST] mode.
 *
 * Pre-seeded with two realistic READY quizzes (one per stub note subject). All mutations
 * (generate, delete, start attempt, submit answers) operate on in-memory state only.
 *
 * No network calls are made.
 */
class StubBackendQuizRepository : BackendQuizRepository {

    private val now: String
        get() = Clock.System.now().toString()

    // ─── Hardcoded stub questions ─────────────────────────────────────────────

    private val mlQuestions = listOf(
        QuizQuestion(
            id = 101L,
            questionText = "Which machine learning paradigm trains on labelled input-output pairs?",
            options = listOf("Unsupervised Learning", "Supervised Learning", "Reinforcement Learning", "Self-Supervised Learning"),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 102L,
            questionText = "What does the learning rate η control in gradient descent?",
            options = listOf("The number of hidden layers", "The size of each parameter update step", "The batch size used in training", "The depth of the decision tree"),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 103L,
            questionText = "Which of the following is an adaptive optimisation algorithm?",
            options = listOf("Batch GD", "Stochastic GD", "Adam", "Mini-batch GD"),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 104L,
            questionText = "The Transformer architecture was introduced in which paper?",
            options = listOf("ImageNet Classification with Deep CNNs", "Attention Is All You Need", "Playing Atari with Deep RL", "BERT: Pre-training of Deep Bidirectional Transformers"),
            orderIndex = 3,
        ),
        QuizQuestion(
            id = 105L,
            questionText = "In the self-attention formula Attention(Q,K,V) = softmax(QKᵀ/√dₖ)V, what is dₖ?",
            options = listOf("The vocabulary size", "The number of attention heads", "The dimensionality of key vectors", "The feed-forward hidden size"),
            orderIndex = 4,
        ),
    )

    private val memoryQuestions = listOf(
        QuizQuestion(
            id = 201L,
            questionText = "According to the Atkinson-Shiffrin model, how many primary memory stores are there?",
            options = listOf("Two", "Three", "Four", "Five"),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 202L,
            questionText = "Which brain structure is critical for forming new declarative memories?",
            options = listOf("Amygdala", "Cerebellum", "Hippocampus", "Prefrontal Cortex"),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 203L,
            questionText = "In the SM-2 algorithm, what is the minimum allowed value for the easiness factor (EF)?",
            options = listOf("1.0", "1.3", "2.0", "2.5"),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 204L,
            questionText = "What term describes the inability to form NEW memories after a brain injury?",
            options = listOf("Retrograde amnesia", "Anterograde amnesia", "Korsakoff syndrome", "Semantic dementia"),
            orderIndex = 3,
        ),
    )

    // ─── In-memory quiz store ─────────────────────────────────────────────────

    private val quizzes = mutableListOf(
        QuizSummary(
            id = 1L,
            noteId = 1L,
            noteTitleSnapshot = "Introduction to Machine Learning",
            status = QuizStatus.READY,
            questionCount = mlQuestions.size,
            createdAt = "2026-03-25T10:00:00Z",
            generatedAt = "2026-03-25T10:00:30Z",
        ),
        QuizSummary(
            id = 2L,
            noteId = 4L,
            noteTitleSnapshot = "Human Memory Systems",
            status = QuizStatus.READY,
            questionCount = memoryQuestions.size,
            createdAt = "2026-03-26T14:00:00Z",
            generatedAt = "2026-03-26T14:00:45Z",
        ),
    )

    private val attempts = mutableListOf<AttemptResult>()
    private var nextAttemptId = 100L
    private var nextQuizId = 3L

    // ─── Quiz CRUD ────────────────────────────────────────────────────────────

    override suspend fun generateQuiz(noteId: Long): ApiResult<QuizSummary> {
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
            questionCount = mlQuestions.size,
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
        val questions = questionsForQuiz(quizId)
        val correctAnswers = correctAnswersForQuiz(quizId)

        val questionResults = questions.map { q ->
            val userAnswer = answers.find { it.questionId == q.id }?.answer ?: "A"
            val correct = correctAnswers[q.id] ?: "A"
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
        val result = attempts.find { it.attemptId == attemptId && it.quizId == quizId }
            ?: return ApiResult.Error("Attempt $attemptId not found", 404)
        return ApiResult.Success(result)
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun questionsForQuiz(quizId: Long): List<QuizQuestion> = when (quizId) {
        1L -> mlQuestions
        2L -> memoryQuestions
        else -> mlQuestions // default for dynamically-generated stub quizzes
    }

    /** Maps questionId -> correct answer letter (A/B/C/D based on correct option index). */
    private fun correctAnswersForQuiz(quizId: Long): Map<Long, String> {
        val correctIndices: Map<Long, Int> = when (quizId) {
            1L -> mapOf(101L to 1, 102L to 1, 103L to 2, 104L to 1, 105L to 2)
            2L -> mapOf(201L to 1, 202L to 2, 203L to 1, 204L to 1)
            else -> mapOf(101L to 1, 102L to 1, 103L to 2, 104L to 1, 105L to 2)
        }
        return correctIndices.mapValues { (_, idx) ->
            listOf("A", "B", "C", "D").getOrElse(idx) { "A" }
        }
    }

    private fun explanationForQuestion(questionId: Long): String = when (questionId) {
        101L -> "Supervised learning uses labelled training pairs to learn a mapping from inputs to outputs."
        102L -> "The learning rate η scales the gradient, determining how large each parameter update step is."
        103L -> "Adam (Adaptive Moment Estimation) adjusts the learning rate per-parameter using first and second moment estimates."
        104L -> "The Transformer was introduced in 'Attention Is All You Need' by Vaswani et al. (2017)."
        105L -> "dₖ is the dimensionality of the key (and query) vectors. Dividing by √dₖ prevents dot products from growing large."
        201L -> "Atkinson-Shiffrin identifies three stores: sensory memory, short-term memory (STM), and long-term memory (LTM)."
        202L -> "The hippocampus is essential for encoding new declarative memories. Damage causes anterograde amnesia."
        203L -> "The SM-2 algorithm clamps the easiness factor to a minimum of 1.3 to prevent intervals from collapsing."
        204L -> "Anterograde amnesia is the inability to form new memories after the injury, as classically shown in patient H.M."
        else -> "See your notes for a detailed explanation."
    }
}
