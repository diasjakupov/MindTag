package io.diasjakupov.mindtag.feature.backendquiz.data.api

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.dto.AttemptResultDto
import io.diasjakupov.mindtag.core.network.dto.AttemptStartDto
import io.diasjakupov.mindtag.core.network.dto.AttemptSubmitRequestDto
import io.diasjakupov.mindtag.core.network.dto.QuizResponseDto
import io.diasjakupov.mindtag.core.network.dto.QuizSummaryDto
import io.diasjakupov.mindtag.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class QuizApi(
    private val client: HttpClient,
    private val authManager: AuthManager,
) {
    // ─── Quiz CRUD ───────────────────────────────────────────────

    suspend fun generateQuiz(noteId: Long): ApiResult<QuizSummaryDto> =
        safeApiCall(authManager) { client.post("/notes/$noteId/quizzes") }

    suspend fun getAllQuizzes(): ApiResult<List<QuizSummaryDto>> =
        safeApiCall(authManager) { client.get("/quizzes") }

    suspend fun getQuiz(quizId: Long): ApiResult<QuizResponseDto> =
        safeApiCall(authManager) { client.get("/quizzes/$quizId") }

    suspend fun getQuizzesForNote(noteId: Long): ApiResult<List<QuizSummaryDto>> =
        safeApiCall(authManager) { client.get("/notes/$noteId/quizzes") }

    suspend fun deleteQuiz(quizId: Long): ApiResult<Unit> =
        safeApiCall(authManager) { client.delete("/quizzes/$quizId") }

    // ─── Attempts ────────────────────────────────────────────────

    suspend fun startAttempt(quizId: Long): ApiResult<AttemptStartDto> =
        safeApiCall(authManager) { client.post("/quizzes/$quizId/attempts") }

    suspend fun submitAttempt(
        quizId: Long,
        attemptId: Long,
        body: AttemptSubmitRequestDto,
    ): ApiResult<AttemptResultDto> =
        safeApiCall(authManager) {
            client.put("/quizzes/$quizId/attempts/$attemptId") {
                setBody(body)
            }
        }

    suspend fun getAttemptResult(
        quizId: Long,
        attemptId: Long,
    ): ApiResult<AttemptResultDto> =
        safeApiCall(authManager) {
            client.get("/quizzes/$quizId/attempts/$attemptId")
        }
}
