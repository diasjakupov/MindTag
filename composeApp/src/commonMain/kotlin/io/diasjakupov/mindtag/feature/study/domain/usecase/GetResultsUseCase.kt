package io.diasjakupov.mindtag.feature.study.domain.usecase

import io.diasjakupov.mindtag.feature.study.domain.model.SessionResult
import io.diasjakupov.mindtag.feature.study.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow

class GetResultsUseCase(private val quizRepository: QuizRepository) {
    operator fun invoke(sessionId: String): Flow<SessionResult?> =
        quizRepository.getSessionResults(sessionId)
}
