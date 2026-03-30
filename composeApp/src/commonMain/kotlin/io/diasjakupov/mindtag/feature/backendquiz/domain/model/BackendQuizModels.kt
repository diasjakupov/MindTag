package io.diasjakupov.mindtag.feature.backendquiz.domain.model

enum class QuizStatus { PENDING, READY, ERROR }

data class QuizSummary(
    val id: Long,
    val noteId: Long,
    val noteTitleSnapshot: String,
    val status: QuizStatus,
    val questionCount: Int,
    val createdAt: String,
    val generatedAt: String?,
)

data class QuizQuestion(
    val id: Long,
    val questionText: String,
    val options: List<String>,
    val orderIndex: Int,
)

data class QuizDetail(
    val id: Long,
    val noteId: Long,
    val noteTitleSnapshot: String,
    val status: QuizStatus,
    val questions: List<QuizQuestion>,
    val createdAt: String,
    val generatedAt: String?,
)

data class AttemptStart(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val startedAt: String,
    val questions: List<QuizQuestion>,
)

data class AttemptResult(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val startedAt: String,
    val completedAt: String?,
    val questionResults: List<QuestionResult>,
)

data class QuestionResult(
    val questionId: Long,
    val questionText: String,
    val options: List<String>,
    val userAnswer: String,
    val correctAnswer: String,
    val correct: Boolean,
    val explanation: String?,
)
