package io.diasjakupov.mindtag.feature.study.domain.model

data class QuizAnswer(
    val id: String,
    val sessionId: String,
    val cardId: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val confidenceRating: ConfidenceRating?,
    val timeSpentSeconds: Int,
    val answeredAt: Long,
)

enum class ConfidenceRating { EASY, HARD }
