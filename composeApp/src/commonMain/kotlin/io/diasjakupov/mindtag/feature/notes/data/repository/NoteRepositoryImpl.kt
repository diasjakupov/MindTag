package io.diasjakupov.mindtag.feature.notes.data.repository

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.dto.NoteResponseDto
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.notes.data.api.NoteApi
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class NoteRepositoryImpl(
    private val noteApi: NoteApi,
    private val db: MindTagDatabase,
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
            is ApiResult.Success -> result.data.map { dto ->
                RelatedNote(noteId = dto.noteId, title = dto.title)
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

    // --- Write-through cache helpers ---

    private fun cacheNotes(dtos: List<NoteResponseDto>) {
        val now = Clock.System.now().toEpochMilliseconds()
        dtos.forEach { dto ->
            // Ensure subject exists in local DB (FK constraint)
            val subjectId = dto.subject
            val existingSubject = db.subjectEntityQueries.selectById(subjectId).executeAsOneOrNull()
            if (existingSubject == null) {
                val colorIndex = subjectId.hashCode().and(0x7FFFFFFF) % subjectColors.size
                db.subjectEntityQueries.insert(
                    id = subjectId,
                    name = subjectId,
                    color_hex = subjectColors[colorIndex],
                    icon_name = "book",
                    progress = 0.0,
                    total_notes = 0,
                    reviewed_notes = 0,
                    created_at = now,
                    updated_at = now,
                )
            }

            // Upsert note (INSERT OR REPLACE)
            val summary = dto.body.take(150).let { if (dto.body.length > 150) "$it..." else it }
            val readTime = (dto.body.split(" ").size / 200).coerceAtLeast(1).toLong()
            db.noteEntityQueries.insert(
                id = dto.id.toString(),
                title = dto.title,
                content = dto.body,
                summary = summary,
                subject_id = dto.subject,
                week_number = null,
                read_time_minutes = readTime,
                created_at = parseTimestamp(dto.createdAt),
                updated_at = parseTimestamp(dto.updatedAt),
            )
        }
    }

    private fun getNotesFromCache(subjectFilter: String?): List<Note> {
        val entities = if (subjectFilter != null) {
            db.noteEntityQueries.selectBySubjectId(subjectFilter).executeAsList()
        } else {
            db.noteEntityQueries.selectAll().executeAsList()
        }
        return entities.map { entity ->
            Note(
                id = entity.id.toLongOrNull() ?: 0L,
                title = entity.title,
                content = entity.content,
                summary = entity.summary,
                subjectId = entity.subject_id,
                subjectName = entity.subject_id,
                weekNumber = entity.week_number?.toInt(),
                readTimeMinutes = entity.read_time_minutes.toInt(),
                createdAt = entity.created_at,
                updatedAt = entity.updated_at,
            )
        }
    }

    private fun getNoteFromCache(id: Long): Note? {
        val entity = db.noteEntityQueries.selectById(id.toString()).executeAsOneOrNull()
            ?: return null
        return Note(
            id = entity.id.toLongOrNull() ?: 0L,
            title = entity.title,
            content = entity.content,
            summary = entity.summary,
            subjectId = entity.subject_id,
            subjectName = entity.subject_id,
            weekNumber = entity.week_number?.toInt(),
            readTimeMinutes = entity.read_time_minutes.toInt(),
            createdAt = entity.created_at,
            updatedAt = entity.updated_at,
        )
    }

    private fun getSubjectsFromCache(): List<Subject> {
        return db.subjectEntityQueries.selectAll().executeAsList().map { entity ->
            Subject(
                id = entity.id,
                name = entity.name,
                colorHex = entity.color_hex,
                iconName = entity.icon_name,
            )
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
        Clock.System.now().toEpochMilliseconds()
    }
}
