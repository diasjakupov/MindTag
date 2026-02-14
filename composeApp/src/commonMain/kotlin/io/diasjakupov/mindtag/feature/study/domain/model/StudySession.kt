package io.diasjakupov.mindtag.feature.study.domain.model

data class StudySession(
    val id: String,
    val subjectId: String?,
    val sessionType: SessionType,
    val startedAt: Long,
    val finishedAt: Long?,
    val totalQuestions: Int,
    val timeLimitSeconds: Int?,
    val status: SessionStatus,
)

enum class SessionType { QUIZ }

enum class SessionStatus { IN_PROGRESS, COMPLETED, ABANDONED }
