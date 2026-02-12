package io.diasjakupov.mindtag.feature.notes.domain.model

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val summary: String,
    val subjectId: String,
    val subjectName: String = "",
    val weekNumber: Int?,
    val readTimeMinutes: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
