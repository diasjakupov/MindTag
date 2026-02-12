package io.diasjakupov.mindtag.feature.notes.domain.model

data class RelatedNote(
    val noteId: Long,
    val title: String,
    val subjectName: String = "",
    val subjectIconName: String = "",
    val subjectColorHex: String = "",
    val similarityScore: Float = 0f,
)
