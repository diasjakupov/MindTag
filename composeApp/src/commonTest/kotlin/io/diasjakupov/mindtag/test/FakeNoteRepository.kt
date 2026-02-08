package io.diasjakupov.mindtag.test

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock

class FakeNoteRepository : NoteRepository {

    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())
    private val subjectsFlow = MutableStateFlow<List<Subject>>(emptyList())
    private val relatedNotesMap = mutableMapOf<String, List<RelatedNote>>()

    private var nextId = 1

    fun setNotes(notes: List<Note>) {
        notesFlow.value = notes
    }

    fun setSubjects(subjects: List<Subject>) {
        subjectsFlow.value = subjects
    }

    fun setRelatedNotes(noteId: String, related: List<RelatedNote>) {
        relatedNotesMap[noteId] = related
    }

    override fun getNotes(subjectId: String?): Flow<List<Note>> {
        return notesFlow.map { notes ->
            if (subjectId != null) notes.filter { it.subjectId == subjectId } else notes
        }
    }

    override fun getNoteById(id: String): Flow<Note?> {
        return notesFlow.map { notes -> notes.find { it.id == id } }
    }

    override fun getRelatedNotes(noteId: String, limit: Int): Flow<List<RelatedNote>> {
        return MutableStateFlow(relatedNotesMap[noteId]?.take(limit) ?: emptyList())
    }

    override fun getSubjects(): Flow<List<Subject>> = subjectsFlow

    override suspend fun createNote(title: String, content: String, subjectId: String): Note {
        val note = Note(
            id = "note-${nextId++}",
            title = title,
            content = content,
            summary = "",
            subjectId = subjectId,
            weekNumber = null,
            readTimeMinutes = 1,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
        notesFlow.update { it + note }
        return note
    }

    override suspend fun updateNote(id: String, title: String, content: String) {
        notesFlow.update { notes ->
            notes.map { note ->
                if (note.id == id) note.copy(title = title, content = content, updatedAt = Clock.System.now().toEpochMilliseconds())
                else note
            }
        }
    }

    override suspend fun deleteNote(id: String) {
        notesFlow.update { notes -> notes.filter { it.id != id } }
    }
}
