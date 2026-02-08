package io.diasjakupov.mindtag.feature.study.domain.repository

import io.diasjakupov.mindtag.feature.study.domain.model.ConfidenceRating
import io.diasjakupov.mindtag.feature.study.domain.model.QuizAnswer
import io.diasjakupov.mindtag.feature.study.domain.model.SessionResult
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    suspend fun submitAnswer(answer: QuizAnswer)
    fun getSessionResults(sessionId: String): Flow<SessionResult?>
    suspend fun updateCardSchedule(cardId: String, isCorrect: Boolean, confidence: ConfidenceRating?)
}
