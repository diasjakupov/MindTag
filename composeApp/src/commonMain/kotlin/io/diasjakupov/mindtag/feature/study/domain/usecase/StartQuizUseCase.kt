package io.diasjakupov.mindtag.feature.study.domain.usecase

import io.diasjakupov.mindtag.feature.study.domain.model.FlashCard
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.model.StudySession
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class QuizStartData(
    val session: StudySession,
    val cards: Flow<List<FlashCard>>,
)

class StartQuizUseCase(private val studyRepository: StudyRepository) {
    suspend operator fun invoke(
        type: SessionType,
        subjectId: String? = null,
        questionCount: Int = 10,
        timeLimitSeconds: Int? = null,
    ): QuizStartData {
        val session = studyRepository.createSession(type, subjectId, questionCount, timeLimitSeconds)
        val cards = studyRepository.getCardsForSession(subjectId, questionCount)
        return QuizStartData(session = session, cards = cards)
    }
}
