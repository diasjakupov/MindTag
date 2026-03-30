package io.diasjakupov.mindtag.core.network.dto

import kotlinx.serialization.Serializable

// ─── Quiz ────────────────────────────────────────────────────────

@Serializable
data class QuizSummaryDto(
    val id: Long,
    val noteId: Long,
    val noteTitleSnapshot: String,
    val status: String,           // "PENDING" | "READY" | "ERROR"
    val questionCount: Int,
    val createdAt: String,
    val generatedAt: String? = null,
)

@Serializable
data class QuizResponseDto(
    val id: Long,
    val noteId: Long,
    val noteTitleSnapshot: String,
    val status: String,
    val questions: List<QuizQuestionDto>,
    val createdAt: String,
    val generatedAt: String? = null,
)

@Serializable
data class QuizQuestionDto(
    val id: Long,
    val questionText: String,
    val options: List<String>,
    val orderIndex: Int,
)

// ─── Attempt ─────────────────────────────────────────────────────

@Serializable
data class AttemptStartDto(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val startedAt: String,
    val questions: List<QuizQuestionDto>,
)

@Serializable
data class AnswerRequestDto(
    val questionId: Long,
    val answer: String, // "A" | "B" | "C" | "D"
)

@Serializable
data class AttemptSubmitRequestDto(
    val answers: List<AnswerRequestDto>,
)

@Serializable
data class AttemptResultDto(
    val attemptId: Long,
    val quizId: Long,
    val noteTitleSnapshot: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val startedAt: String,
    val completedAt: String? = null,
    val questionResults: List<QuestionResultDto>,
)

@Serializable
data class QuestionResultDto(
    val questionId: Long,
    val questionText: String,
    val options: List<String>,
    val userAnswer: String,
    val correctAnswer: String,
    val correct: Boolean,
    val explanation: String? = null,
)

