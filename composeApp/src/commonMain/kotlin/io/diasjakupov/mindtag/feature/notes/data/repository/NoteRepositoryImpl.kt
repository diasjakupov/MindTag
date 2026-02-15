package io.diasjakupov.mindtag.feature.notes.data.repository

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.dto.NoteResponseDto
import io.diasjakupov.mindtag.core.network.dto.SearchResponseDto
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.local.NoteEntity
import io.diasjakupov.mindtag.feature.notes.data.api.NoteApi
import io.diasjakupov.mindtag.feature.notes.data.api.SearchApi
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class NoteRepositoryImpl(
    private val noteApi: NoteApi,
    private val db: MindTagDatabase,
    private val searchApi: SearchApi,
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
                cacheNotes(result.data)
                if (subjectFilter != null) {
                    notes.filter { it.subjectId == subjectFilter }
                } else {
                    notes
                }
            }
            is ApiResult.Error -> {
                Logger.e(tag, "getNotes: ${result.message}, falling back to cache")
                getNotesFromCache(subjectFilter)
            }
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return when (val result = noteApi.getNoteById(id)) {
            is ApiResult.Success -> {
                cacheNotes(listOf(result.data))
                result.data.toDomain()
            }
            is ApiResult.Error -> {
                Logger.e(tag, "getNoteById: ${result.message}, falling back to cache")
                getNoteFromCache(id)
            }
        }
    }

    override suspend fun getRelatedNotes(noteId: Long): List<RelatedNote> {
        return when (val result = noteApi.getRelatedNotes(noteId)) {
            is ApiResult.Success -> result.data.mapNotNull { dto ->
                val id = dto.noteId.toLongOrNull() ?: return@mapNotNull null
                RelatedNote(noteId = id, title = dto.title)
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
                cacheNotes(result.data)
                result.data
                    .map { it.subject }
                    .distinct()
                    .map { name ->
                        Subject(
                            id = name,
                            name = name,
                            colorHex = colorForSubject(name),
                            iconName = "book",
                        )
                    }
            }
            is ApiResult.Error -> {
                Logger.e(tag, "getSubjects: ${result.message}, falling back to cache")
                getSubjectsFromCache()
            }
        }
    }

    override suspend fun createNote(title: String, content: String, subjectName: String): Note {
        return when (val result = noteApi.createNote(title, subjectName, content)) {
            is ApiResult.Success -> {
                cacheNotes(listOf(result.data))
                result.data.toDomain()
            }
            is ApiResult.Error -> throw Exception(result.message)
        }
    }

    override suspend fun updateNote(id: Long, title: String, content: String, subjectName: String) {
        when (val result = noteApi.updateNote(id, title, subjectName, content)) {
            is ApiResult.Success -> {
                cacheNotes(listOf(result.data))
            }
            is ApiResult.Error -> throw Exception(result.message)
        }
    }

    override suspend fun deleteNote(id: Long) {
        when (val result = noteApi.deleteNote(id)) {
            is ApiResult.Success -> {
                db.noteEntityQueries.delete(id.toString())
            }
            is ApiResult.Error -> throw Exception(result.message)
        }
    }

    override suspend fun searchNotes(query: String, page: Int, size: Int): PaginatedNotes {
        return when (val result = searchApi.search(query, page, size)) {
            is ApiResult.Success -> result.data.toPaginatedNotes(page, size)
            is ApiResult.Error -> {
                Logger.e(tag, "searchNotes: ${result.message}, falling back to cache")
                val cached = getNotesFromCache(null).filter {
                    it.title.contains(query, ignoreCase = true)
                }
                PaginatedNotes(notes = cached, total = cached.size.toLong(), page = 0, hasMore = false)
            }
        }
    }

    override suspend fun listNotesBySubject(subject: String, page: Int, size: Int): PaginatedNotes {
        return when (val result = searchApi.listBySubject(subject, page, size)) {
            is ApiResult.Success -> result.data.toPaginatedNotes(page, size)
            is ApiResult.Error -> {
                Logger.e(tag, "listNotesBySubject: ${result.message}, falling back to cache")
                val cached = getNotesFromCache(subject)
                PaginatedNotes(notes = cached, total = cached.size.toLong(), page = 0, hasMore = false)
            }
        }
    }

    private fun SearchResponseDto.toPaginatedNotes(page: Int, size: Int): PaginatedNotes {
        val notes = results.map { dto ->
            Note(
                id = dto.noteId.toLongOrNull() ?: 0L,
                title = dto.title,
                content = dto.snippet,
                summary = dto.snippet,
                subjectId = "",
                subjectName = "",
                weekNumber = null,
                readTimeMinutes = 1,
                createdAt = 0L,
                updatedAt = 0L,
            )
        }
        val fetched = (page + 1) * size
        return PaginatedNotes(
            notes = notes,
            total = total,
            page = page,
            hasMore = fetched < total,
        )
    }

    private fun cacheNotes(dtos: List<NoteResponseDto>) {
        val now = Clock.System.now().toEpochMilliseconds()
        db.transaction {
            dtos.forEach { dto ->
                val subjectId = dto.subject
                val existingSubject = db.subjectEntityQueries.selectById(subjectId).executeAsOneOrNull()
                if (existingSubject == null) {
                    db.subjectEntityQueries.insert(
                        id = subjectId,
                        name = subjectId,
                        color_hex = colorForSubject(subjectId),
                        icon_name = "book",
                        progress = 0.0,
                        total_notes = 0,
                        reviewed_notes = 0,
                        created_at = now,
                        updated_at = now,
                    )
                }

                db.noteEntityQueries.insert(
                    id = dto.id.toString(),
                    title = dto.title,
                    content = dto.body,
                    summary = summarize(dto.body),
                    subject_id = dto.subject,
                    week_number = null,
                    read_time_minutes = estimateReadTimeMinutes(dto.body).toLong(),
                    created_at = parseTimestamp(dto.createdAt),
                    updated_at = dto.updatedAt?.let { parseTimestamp(it) } ?: parseTimestamp(dto.createdAt),
                )
            }
        }
    }

    private fun getNotesFromCache(subjectFilter: String?): List<Note> {
        val entities = if (subjectFilter != null) {
            db.noteEntityQueries.selectBySubjectId(subjectFilter).executeAsList()
        } else {
            db.noteEntityQueries.selectAll().executeAsList()
        }
        return entities.map { it.toDomain() }
    }

    private fun getNoteFromCache(id: Long): Note? =
        db.noteEntityQueries.selectById(id.toString()).executeAsOneOrNull()?.toDomain()

    private fun getSubjectsFromCache(): List<Subject> =
        db.subjectEntityQueries.selectAll().executeAsList().map { entity ->
            Subject(
                id = entity.id,
                name = entity.name,
                colorHex = entity.color_hex,
                iconName = entity.icon_name,
            )
        }

    private fun NoteResponseDto.toDomain() = Note(
        id = id,
        title = title,
        content = body,
        summary = summarize(body),
        subjectId = subject,
        subjectName = subject,
        weekNumber = null,
        readTimeMinutes = estimateReadTimeMinutes(body),
        createdAt = parseTimestamp(createdAt),
        updatedAt = updatedAt?.let { parseTimestamp(it) } ?: parseTimestamp(createdAt),
    )

    private fun NoteEntity.toDomain() = Note(
        id = id.toLongOrNull() ?: 0L,
        title = title,
        content = content,
        summary = summary,
        subjectId = subject_id,
        subjectName = subject_id,
        weekNumber = week_number?.toInt(),
        readTimeMinutes = read_time_minutes.toInt(),
        createdAt = created_at,
        updatedAt = updated_at,
    )

    private fun summarize(body: String): String =
        if (body.length > 150) "${body.take(150)}..." else body

    private fun estimateReadTimeMinutes(body: String): Int =
        (body.split(" ").size / 200).coerceAtLeast(1)

    private fun colorForSubject(name: String): String =
        subjectColors[name.hashCode().and(0x7FFFFFFF) % subjectColors.size]

    private fun parseTimestamp(iso: String): Long = try {
        Instant.parse(iso).toEpochMilliseconds()
    } catch (_: Exception) {
        try {
            // Backend sends LocalDateTime without timezone offset, assume UTC
            Instant.parse("${iso}Z").toEpochMilliseconds()
        } catch (_: Exception) {
            Clock.System.now().toEpochMilliseconds()
        }
    }
}
