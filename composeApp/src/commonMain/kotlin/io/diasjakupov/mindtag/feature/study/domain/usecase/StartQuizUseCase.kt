package io.diasjakupov.mindtag.feature.study.domain.usecase

import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.model.StudySession
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository

data class QuizStartData(
    val session: StudySession,
)

class StartQuizUseCase(private val studyRepository: StudyRepository) {
    suspend operator fun invoke(
        type: SessionType,
        subjectId: String? = null,
        questionCount: Int = 10,
        timeLimitSeconds: Int? = null,
    ): QuizStartData {
        val session = studyRepository.createSession(type, subjectId, questionCount, timeLimitSeconds)
        return QuizStartData(session = session)
    }
}
