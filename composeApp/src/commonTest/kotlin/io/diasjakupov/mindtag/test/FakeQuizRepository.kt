package io.diasjakupov.mindtag.test

import io.diasjakupov.mindtag.feature.study.domain.model.ConfidenceRating
import io.diasjakupov.mindtag.feature.study.domain.model.QuizAnswer
import io.diasjakupov.mindtag.feature.study.domain.model.SessionResult
import io.diasjakupov.mindtag.feature.study.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeQuizRepository : QuizRepository {

    private val submittedAnswers = mutableListOf<QuizAnswer>()
    private val sessionResultsFlow = MutableStateFlow<Map<String, SessionResult>>(emptyMap())
    private val updatedSchedules = mutableListOf<Triple<String, Boolean, ConfidenceRating?>>()

    fun getSubmittedAnswers(): List<QuizAnswer> = submittedAnswers.toList()
    fun getUpdatedSchedules(): List<Triple<String, Boolean, ConfidenceRating?>> = updatedSchedules.toList()

    fun setSessionResult(sessionId: String, result: SessionResult) {
        sessionResultsFlow.value = sessionResultsFlow.value + (sessionId to result)
    }

    override suspend fun submitAnswer(answer: QuizAnswer) {
        submittedAnswers.add(answer)
    }

    override fun getSessionResults(sessionId: String): Flow<SessionResult?> {
        return MutableStateFlow(sessionResultsFlow.value[sessionId])
    }

    override suspend fun updateCardSchedule(
        cardId: String,
        isCorrect: Boolean,
        confidence: ConfidenceRating?,
    ) {
        updatedSchedules.add(Triple(cardId, isCorrect, confidence))
    }
}
