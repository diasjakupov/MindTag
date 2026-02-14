package io.diasjakupov.mindtag.feature.study.domain.model

data class SessionResult(
    val session: StudySession,
    val scorePercent: Int,
    val totalCorrect: Int,
    val totalQuestions: Int,
    val timeSpentFormatted: String,
    val answers: List<QuizAnswerDetail>,
)

data class QuizAnswerDetail(
    val cardId: String,
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val aiInsight: String?,
)
