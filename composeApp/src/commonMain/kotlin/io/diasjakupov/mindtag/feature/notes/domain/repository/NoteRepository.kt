package io.diasjakupov.mindtag.feature.notes.domain.repository

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(subjectId: String? = null): Flow<List<Note>>
    fun getNoteById(id: String): Flow<Note?>
    fun getRelatedNotes(noteId: String, limit: Int = 5): Flow<List<RelatedNote>>
    fun getSubjects(): Flow<List<Subject>>
    suspend fun createNote(title: String, content: String, subjectId: String): Note
    suspend fun getAllNotesSnapshot(): List<Note>
    suspend fun updateNote(id: String, title: String, content: String)
    suspend fun deleteNote(id: String)
    suspend fun createSubject(name: String, colorHex: String, iconName: String): Subject
}
