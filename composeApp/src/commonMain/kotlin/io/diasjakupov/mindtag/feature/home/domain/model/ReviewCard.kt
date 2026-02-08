package io.diasjakupov.mindtag.feature.home.domain.model

data class ReviewCard(
    val noteId: String,
    val noteTitle: String,
    val subjectName: String,
    val subjectColorHex: String,
    val subjectIconName: String,
    val progressPercent: Float,
    val dueCardCount: Int,
    val weekNumber: Int?,
)
