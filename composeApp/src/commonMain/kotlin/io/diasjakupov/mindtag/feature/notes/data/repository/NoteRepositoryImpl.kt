package io.diasjakupov.mindtag.feature.notes.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.core.domain.usecase.FlashcardGenerator
import io.diasjakupov.mindtag.core.domain.usecase.SemanticAnalyzer
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.local.NoteEntity
import io.diasjakupov.mindtag.data.local.SubjectEntity
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class NoteRepositoryImpl(
    private val db: MindTagDatabase,
) : NoteRepository {

    private val tag = "NoteRepo"

    override fun getNotes(subjectId: String?): Flow<List<Note>> {
        val query = if (subjectId != null) {
            db.noteEntityQueries.selectBySubjectId(subjectId)
        } else {
            db.noteEntityQueries.selectAll()
        }
        return query.asFlow().mapToList(Dispatchers.IO).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNoteById(id: String): Flow<Note?> =
        db.noteEntityQueries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun getRelatedNotes(noteId: String, limit: Int): Flow<List<RelatedNote>> =
        db.semanticLinkEntityQueries.selectRelatedNotes(noteId = noteId, limit = limit.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    RelatedNote(
                        noteId = row.related_note_id,
                        title = row.note_title,
                        subjectName = row.subject_name,
                        subjectIconName = row.subject_icon_name,
                        subjectColorHex = row.subject_color_hex,
                        similarityScore = row.similarity_score.toFloat(),
                    )
                }
            }

    override fun getSubjects(): Flow<List<Subject>> =
        db.subjectEntityQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }

    override suspend fun createNote(title: String, content: String, subjectId: String): Note {
        Logger.d(tag, "createNote: title='$title', subjectId=$subjectId")
        val id = kotlin.uuid.Uuid.random().toString()
        val now = Clock.System.now().toEpochMilliseconds()
        val readTimeMinutes = (content.split(" ").size / 200).coerceAtLeast(1)
        val summary = content.take(150).let {
            if (content.length > 150) "$it..." else it
        }

        db.noteEntityQueries.insert(
            id = id,
            title = title,
            content = content,
            summary = summary,
            subject_id = subjectId,
            week_number = null,
            read_time_minutes = readTimeMinutes.toLong(),
            created_at = now,
            updated_at = now,
        )

        // Auto-generate semantic links
        val allNotes = db.noteEntityQueries.selectAll().executeAsList().map { it.toDomain() }
        val otherNotes = allNotes.filter { it.id != id }
        val noteInfo = otherNotes.map { n ->
            SemanticAnalyzer.NoteInfo(n.id, "${n.title} ${n.content}", n.subjectId, n.weekNumber)
        }
        val links = SemanticAnalyzer.computeLinks(
            newNoteId = id,
            newNoteText = "$title $content",
            newNoteSubjectId = subjectId,
            newNoteWeek = null,
            allOtherNotes = noteInfo,
        )
        links.forEach { link ->
            val linkId = kotlin.uuid.Uuid.random().toString()
            db.semanticLinkEntityQueries.insert(
                id = linkId,
                source_note_id = id,
                target_note_id = link.targetNoteId,
                similarity_score = link.similarityScore.toDouble(),
                link_type = link.linkType,
                strength = link.strength.toDouble(),
                created_at = now,
            )
        }
        Logger.d(tag, "createNote: generated ${links.size} semantic links")

        // Auto-generate flashcards
        val generatedCards = FlashcardGenerator.generate(
            noteTitle = title,
            noteContent = content,
            subjectId = subjectId,
            noteId = id,
        )
        generatedCards.forEach { card ->
            val cardId = kotlin.uuid.Uuid.random().toString()
            db.flashCardEntityQueries.insert(
                id = cardId,
                question = card.question,
                type = card.type,
                difficulty = card.difficulty,
                subject_id = card.subjectId,
                correct_answer = card.correctAnswer,
                options_json = null,
                source_note_ids_json = card.sourceNoteIdsJson,
                ai_explanation = card.explanation,
                ease_factor = 2.5,
                interval_days = 0,
                repetitions = 0,
                next_review_at = null,
                created_at = now,
            )
        }
        Logger.d(tag, "createNote: generated ${generatedCards.size} flashcards")

        Logger.d(tag, "createNote: success — id=$id")
        return Note(
            id = id,
            title = title,
            content = content,
            summary = summary,
            subjectId = subjectId,
            weekNumber = null,
            readTimeMinutes = readTimeMinutes,
            createdAt = now,
            updatedAt = now,
        )
    }

    override suspend fun updateNote(id: String, title: String, content: String) {
        Logger.d(tag, "updateNote: id=$id, title='$title'")
        val now = Clock.System.now().toEpochMilliseconds()
        val readTimeMinutes = (content.split(" ").size / 200).coerceAtLeast(1)
        val summary = content.take(150).let {
            if (content.length > 150) "$it..." else it
        }

        db.noteEntityQueries.update(
            title = title,
            content = content,
            summary = summary,
            week_number = null,
            read_time_minutes = readTimeMinutes.toLong(),
            updated_at = now,
            id = id,
        )
    }

    override suspend fun getAllNotesSnapshot(): List<Note> =
        db.noteEntityQueries.selectAll().executeAsList().map { it.toDomain() }

    override suspend fun deleteNote(id: String) {
        Logger.d(tag, "deleteNote: id=$id")
        db.noteEntityQueries.delete(id)
    }

    private fun NoteEntity.toDomain() = Note(
        id = id,
        title = title,
        content = content,
        summary = summary,
        subjectId = subject_id,
        weekNumber = week_number?.toInt(),
        readTimeMinutes = read_time_minutes.toInt(),
        createdAt = created_at,
        updatedAt = updated_at,
    )

    private fun SubjectEntity.toDomain() = Subject(
        id = id,
        name = name,
        colorHex = color_hex,
        iconName = icon_name,
    )
}
