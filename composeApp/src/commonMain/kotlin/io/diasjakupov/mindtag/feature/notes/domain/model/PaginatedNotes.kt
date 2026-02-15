package io.diasjakupov.mindtag.feature.notes.domain.model

data class PaginatedNotes(
    val notes: List<Note>,
    val total: Long,
    val page: Int,
    val hasMore: Boolean,
)