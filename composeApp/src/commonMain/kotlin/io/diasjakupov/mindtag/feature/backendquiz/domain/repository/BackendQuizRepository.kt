package io.diasjakupov.mindtag.feature.backendquiz.domain.repository

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.dto.AnswerRequestDto
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptStart
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizDetail
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizSummary

interface BackendQuizRepository {
    /** Triggers quiz generation (async). Returns summary with status PENDING. */
    suspend fun generateQuiz(noteId: Long): ApiResult<QuizSummary>

    /** Polls until quiz is READY or ERROR, returns the final summary. */
    suspend fun pollUntilReady(quizId: Long, maxAttempts: Int = 30, intervalMs: Long = 2000): ApiResult<QuizSummary>

    suspend fun getQuiz(quizId: Long): ApiResult<QuizDetail>
    suspend fun getAllQuizzes(): ApiResult<List<QuizSummary>>
    suspend fun getQuizzesForNote(noteId: Long): ApiResult<List<QuizSummary>>
    suspend fun deleteQuiz(quizId: Long): ApiResult<Unit>

    suspend fun startAttempt(quizId: Long): ApiResult<AttemptStart>
    suspend fun submitAttempt(quizId: Long, attemptId: Long, answers: List<AnswerRequestDto>): ApiResult<AttemptResult>
    suspend fun getAttemptResult(quizId: Long, attemptId: Long): ApiResult<AttemptResult>
}
