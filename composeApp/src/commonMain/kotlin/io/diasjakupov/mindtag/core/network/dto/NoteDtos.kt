package io.diasjakupov.mindtag.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteCreateRequestDto(
    val title: String,
    val subject: String,
    val body: String,
)

@Serializable
data class NoteUpdateRequestDto(
    val title: String,
    val subject: String,
    val body: String,
)

@Serializable
data class NoteResponseDto(
    val id: Long,
    val title: String,
    val subject: String,
    val body: String,
    val contentHash: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class RelatedNoteResponseDto(
    val noteId: String,
    val title: String,
)
