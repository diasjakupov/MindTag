package io.diasjakupov.mindtag.feature.study.domain.usecase

import io.diasjakupov.mindtag.feature.study.domain.model.ConfidenceRating
import io.diasjakupov.mindtag.feature.study.domain.model.QuizAnswer
import io.diasjakupov.mindtag.feature.study.domain.repository.QuizRepository
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository
import kotlinx.datetime.Clock

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class SubmitAnswerUseCase(
    private val quizRepository: QuizRepository,
    private val studyRepository: StudyRepository,
) {
    suspend operator fun invoke(
        sessionId: String,
        cardId: String,
        userAnswer: String,
        isCorrect: Boolean,
        confidenceRating: ConfidenceRating?,
        timeSpentSeconds: Int,
        currentQuestionIndex: Int,
        totalQuestions: Int,
    ): Boolean {
        val answer = QuizAnswer(
            id = kotlin.uuid.Uuid.random().toString(),
            sessionId = sessionId,
            cardId = cardId,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            confidenceRating = confidenceRating,
            timeSpentSeconds = timeSpentSeconds,
            answeredAt = Clock.System.now().toEpochMilliseconds(),
        )

        quizRepository.submitAnswer(answer)
        quizRepository.updateCardSchedule(cardId, isCorrect, confidenceRating)

        val isLastQuestion = currentQuestionIndex >= totalQuestions - 1
        if (isLastQuestion) {
            studyRepository.completeSession(sessionId)
        }

        return isLastQuestion
    }
}
