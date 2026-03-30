package io.diasjakupov.mindtag.feature.backendquiz.data.mapper

import io.diasjakupov.mindtag.core.network.dto.AttemptResultDto
import io.diasjakupov.mindtag.core.network.dto.AttemptStartDto
import io.diasjakupov.mindtag.core.network.dto.QuestionResultDto
import io.diasjakupov.mindtag.core.network.dto.QuizQuestionDto
import io.diasjakupov.mindtag.core.network.dto.QuizResponseDto
import io.diasjakupov.mindtag.core.network.dto.QuizSummaryDto
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptStart
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuestionResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizDetail
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizQuestion
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizSummary

fun QuizSummaryDto.toDomain() = QuizSummary(
    id = id,
    noteId = noteId,
    noteTitleSnapshot = noteTitleSnapshot,
    status = status.toQuizStatus(),
    questionCount = questionCount,
    createdAt = createdAt,
    generatedAt = generatedAt,
)

fun QuizResponseDto.toDomain() = QuizDetail(
    id = id,
    noteId = noteId,
    noteTitleSnapshot = noteTitleSnapshot,
    status = status.toQuizStatus(),
    questions = questions.map { it.toDomain() },
    createdAt = createdAt,
    generatedAt = generatedAt,
)

fun QuizQuestionDto.toDomain() = QuizQuestion(
    id = id,
    questionText = questionText,
    options = options,
    orderIndex = orderIndex,
)

fun AttemptStartDto.toDomain() = AttemptStart(
    attemptId = attemptId,
    quizId = quizId,
    noteTitleSnapshot = noteTitleSnapshot,
    startedAt = startedAt,
    questions = questions.map { it.toDomain() },
)

fun AttemptResultDto.toDomain() = AttemptResult(
    attemptId = attemptId,
    quizId = quizId,
    noteTitleSnapshot = noteTitleSnapshot,
    score = score,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    startedAt = startedAt,
    completedAt = completedAt,
    questionResults = questionResults.map { it.toDomain() },
)

fun QuestionResultDto.toDomain() = QuestionResult(
    questionId = questionId,
    questionText = questionText,
    options = options,
    userAnswer = userAnswer,
    correctAnswer = correctAnswer,
    correct = correct,
    explanation = explanation,
)

private fun String.toQuizStatus(): QuizStatus = when (this) {
    "READY" -> QuizStatus.READY
    "ERROR" -> QuizStatus.ERROR
    else -> QuizStatus.PENDING
}
