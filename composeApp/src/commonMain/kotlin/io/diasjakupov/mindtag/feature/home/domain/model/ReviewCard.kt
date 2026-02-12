package io.diasjakupov.mindtag.feature.home.domain.model

data class ReviewCard(
    val subjectId: String,
    val noteId: Long,
    val noteTitle: String,
    val subjectName: String,
    val subjectColorHex: String,
    val subjectIconName: String,
    val progressPercent: Float,
    val dueCardCount: Int,
    val weekNumber: Int?,
)
