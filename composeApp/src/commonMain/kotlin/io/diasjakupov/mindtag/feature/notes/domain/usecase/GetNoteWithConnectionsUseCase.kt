package io.diasjakupov.mindtag.feature.notes.domain.usecase

import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository

data class NoteWithConnections(
    val note: Note,
    val relatedNotes: List<RelatedNote>,
)

class GetNoteWithConnectionsUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(noteId: Long): NoteWithConnections? {
        val note = repository.getNoteById(noteId) ?: return null
        val related = repository.getRelatedNotes(noteId)
        return NoteWithConnections(note, related)
    }
}
