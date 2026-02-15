package io.diasjakupov.mindtag.feature.notes.domain.repository

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote

interface NoteRepository {
    suspend fun getNotes(subjectFilter: String? = null): List<Note>
    suspend fun getNoteById(id: Long): Note?
    suspend fun getRelatedNotes(noteId: Long): List<RelatedNote>
    suspend fun getSubjects(): List<Subject>
    suspend fun createNote(title: String, content: String, subjectName: String): Note
    suspend fun updateNote(id: Long, title: String, content: String, subjectName: String)
    suspend fun deleteNote(id: Long)
    suspend fun searchNotes(query: String, page: Int = 0, size: Int = 20): PaginatedNotes
    suspend fun listNotesBySubject(subject: String, page: Int = 0, size: Int = 20): PaginatedNotes
}
