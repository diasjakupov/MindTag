package io.diasjakupov.mindtag.test

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock

class FakeNoteRepository : NoteRepository {

    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())
    private val subjectsFlow = MutableStateFlow<List<Subject>>(emptyList())
    private val relatedNotesMap = mutableMapOf<Long, List<RelatedNote>>()

    private var nextId = 1L

    fun setNotes(notes: List<Note>) {
        notesFlow.value = notes
    }

    fun setSubjects(subjects: List<Subject>) {
        subjectsFlow.value = subjects
    }

    fun setRelatedNotes(noteId: Long, related: List<RelatedNote>) {
        relatedNotesMap[noteId] = related
    }

    override suspend fun getNotes(subjectFilter: String?): List<Note> {
        return notesFlow.value.let { notes ->
            if (subjectFilter != null) notes.filter { it.subjectId == subjectFilter } else notes
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return notesFlow.value.find { it.id == id }
    }

    override suspend fun getRelatedNotes(noteId: Long): List<RelatedNote> {
        return relatedNotesMap[noteId] ?: emptyList()
    }

    override suspend fun getSubjects(): List<Subject> {
        return subjectsFlow.value
    }

    override suspend fun createNote(title: String, content: String, subjectName: String): Note {
        val note = Note(
            id = nextId++,
            title = title,
            content = content,
            summary = "",
            subjectId = subjectName,
            weekNumber = null,
            readTimeMinutes = 1,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
        notesFlow.update { it + note }
        return note
    }

    override suspend fun updateNote(id: Long, title: String, content: String, subjectName: String) {
        notesFlow.update { notes ->
            notes.map { note ->
                if (note.id == id) note.copy(title = title, content = content, updatedAt = Clock.System.now().toEpochMilliseconds())
                else note
            }
        }
    }

    override suspend fun deleteNote(id: Long) {
        notesFlow.update { notes -> notes.filter { it.id != id } }
    }

    override suspend fun searchNotes(query: String, page: Int, size: Int): PaginatedNotes {
        val filtered = notesFlow.value.filter { it.title.contains(query, ignoreCase = true) }
        return PaginatedNotes(
            notes = filtered,
            page = page,
            hasMore = false,
        )
    }

    override suspend fun listNotesBySubject(subject: String, page: Int, size: Int): PaginatedNotes {
        val filtered = notesFlow.value.filter { it.subjectId == subject }
        return PaginatedNotes(
            notes = filtered,
            page = page,
            hasMore = false,
        )
    }

    override suspend fun semanticSearch(query: String): List<Note> {
        return notesFlow.value.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }
    }
}
