package io.diasjakupov.mindtag.feature.notes.domain.model

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val summary: String,
    val subjectId: String,
    val weekNumber: Int?,
    val readTimeMinutes: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
