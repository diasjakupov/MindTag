package io.diasjakupov.mindtag.feature.backendquiz.data.repository

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.dto.AnswerRequestDto
import io.diasjakupov.mindtag.core.network.dto.AttemptSubmitRequestDto
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.backendquiz.data.api.QuizApi
import io.diasjakupov.mindtag.feature.backendquiz.data.mapper.toDomain
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptHistory
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptStart
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizDetail
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizSummary
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.delay

class BackendQuizRepositoryImpl(
    private val quizApi: QuizApi,
) : BackendQuizRepository {

    private val tag = "BackendQuizRepo"

    override suspend fun generateQuiz(noteId: Long): ApiResult<QuizSummary> {
        Logger.d(tag, "generateQuiz: noteId=$noteId")
        return when (val result = quizApi.generateQuiz(noteId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun pollUntilReady(
        quizId: Long,
        maxAttempts: Int,
        intervalMs: Long,
    ): ApiResult<QuizSummary> {
        Logger.d(tag, "pollUntilReady: quizId=$quizId, maxAttempts=$maxAttempts")
        repeat(maxAttempts) { attempt ->
            when (val result = quizApi.getQuiz(quizId)) {
                is ApiResult.Success -> {
                    val summary = QuizSummary(
                        id = result.data.id,
                        noteId = result.data.noteId,
                        noteTitleSnapshot = result.data.noteTitleSnapshot,
                        status = when (result.data.status) {
                            "READY" -> QuizStatus.READY
                            "ERROR" -> QuizStatus.ERROR
                            else -> QuizStatus.PENDING
                        },
                        questionCount = result.data.questions.size,
                        createdAt = result.data.createdAt,
                        generatedAt = result.data.generatedAt,
                    )
                    Logger.d(tag, "pollUntilReady: attempt ${attempt + 1}, status=${summary.status}")
                    when (summary.status) {
                        QuizStatus.READY -> return ApiResult.Success(summary)
                        QuizStatus.ERROR -> return ApiResult.Error("Quiz generation failed")
                        QuizStatus.PENDING -> delay(intervalMs)
                    }
                }
                is ApiResult.Error -> return result
            }
        }
        return ApiResult.Error("Quiz generation timed out after ${maxAttempts * intervalMs / 1000}s")
    }

    override suspend fun getQuiz(quizId: Long): ApiResult<QuizDetail> {
        return when (val result = quizApi.getQuiz(quizId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getAllQuizzes(): ApiResult<List<QuizSummary>> {
        return when (val result = quizApi.getAllQuizzes()) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    override suspend fun getQuizzesForNote(noteId: Long): ApiResult<List<QuizSummary>> {
        return when (val result = quizApi.getQuizzesForNote(noteId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    override suspend fun deleteQuiz(quizId: Long): ApiResult<Unit> {
        return quizApi.deleteQuiz(quizId)
    }

    override suspend fun startAttempt(quizId: Long): ApiResult<AttemptStart> {
        return when (val result = quizApi.startAttempt(quizId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun submitAttempt(
        quizId: Long,
        attemptId: Long,
        answers: List<AnswerRequestDto>,
    ): ApiResult<AttemptResult> {
        return when (val result = quizApi.submitAttempt(quizId, attemptId, AttemptSubmitRequestDto(answers))) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getAttemptResult(quizId: Long, attemptId: Long): ApiResult<AttemptResult> {
        return when (val result = quizApi.getAttemptResult(quizId, attemptId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getAttemptHistory(): ApiResult<List<AttemptHistory>> {
        return when (val result = quizApi.getAttemptHistory()) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }
}
