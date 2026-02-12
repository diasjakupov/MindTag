package io.diasjakupov.mindtag.feature.notes.data.repository

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.dto.NoteResponseDto
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.notes.data.api.NoteApi
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.datetime.Instant

class NoteRepositoryImpl(
    private val noteApi: NoteApi,
) : NoteRepository {

    private val tag = "NoteRepo"

    private val subjectColors = listOf(
        "#135BEC", "#22C55E", "#F97316", "#A855F7",
        "#EF4444", "#EAB308", "#2DD4BF", "#EC4899",
    )

    override suspend fun getNotes(subjectFilter: String?): List<Note> {
        return when (val result = noteApi.getNotes()) {
            is ApiResult.Success -> {
                val notes = result.data.map { it.toDomain() }
                if (subjectFilter != null) {
                    notes.filter { it.subjectId == subjectFilter }
                } else {
                    notes
                }
            }
            is ApiResult.Error -> {
                Logger.e(tag, "getNotes: ${result.message}")
                emptyList()
            }
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return when (val result = noteApi.getNoteById(id)) {
            is ApiResult.Success -> result.data.toDomain()
            is ApiResult.Error -> {
                Logger.e(tag, "getNoteById: ${result.message}")
                null
            }
        }
    }

    override suspend fun getRelatedNotes(noteId: Long): List<RelatedNote> {
        return when (val result = noteApi.getRelatedNotes(noteId)) {
            is ApiResult.Success -> result.data.map { dto ->
                RelatedNote(
                    noteId = dto.noteId,
                    title = dto.title,
                )
            }
            is ApiResult.Error -> {
                Logger.e(tag, "getRelatedNotes: ${result.message}")
                emptyList()
            }
        }
    }

    override suspend fun getSubjects(): List<Subject> {
        return when (val result = noteApi.getNotes()) {
            is ApiResult.Success -> {
                result.data
                    .map { it.subject }
                    .distinct()
                    .mapIndexed { index, name ->
                        Subject(
                            id = name,
                            name = name,
                            colorHex = subjectColors[index % subjectColors.size],
                            iconName = "book",
                        )
                    }
            }
            is ApiResult.Error -> {
                Logger.e(tag, "getSubjects: ${result.message}")
                emptyList()
            }
        }
    }

    override suspend fun createNote(title: String, content: String, subjectName: String): Note {
        return when (val result = noteApi.createNote(title, subjectName, content)) {
            is ApiResult.Success -> result.data.toDomain()
            is ApiResult.Error -> throw Exception(result.message)
        }
    }

    override suspend fun updateNote(id: Long, title: String, content: String, subjectName: String) {
        when (val result = noteApi.updateNote(id, title, subjectName, content)) {
            is ApiResult.Success -> { /* success */ }
            is ApiResult.Error -> throw Exception(result.message)
        }
    }

    override suspend fun deleteNote(id: Long) {
        when (val result = noteApi.deleteNote(id)) {
            is ApiResult.Success -> { /* success */ }
            is ApiResult.Error -> throw Exception(result.message)
        }
    }

    private fun NoteResponseDto.toDomain() = Note(
        id = id,
        title = title,
        content = body,
        summary = body.take(150).let { if (body.length > 150) "$it..." else it },
        subjectId = subject,
        subjectName = subject,
        weekNumber = null,
        readTimeMinutes = (body.split(" ").size / 200).coerceAtLeast(1),
        createdAt = parseTimestamp(createdAt),
        updatedAt = parseTimestamp(updatedAt),
    )

    private fun parseTimestamp(iso: String): Long = try {
        Instant.parse(iso).toEpochMilliseconds()
    } catch (_: Exception) {
        0L
    }
}
