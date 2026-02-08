package io.diasjakupov.mindtag.feature.notes.domain.usecase

import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class NoteWithConnections(
    val note: Note,
    val relatedNotes: List<RelatedNote>,
)

class GetNoteWithConnectionsUseCase(private val repository: NoteRepository) {
    operator fun invoke(noteId: String): Flow<NoteWithConnections?> =
        combine(
            repository.getNoteById(noteId),
            repository.getRelatedNotes(noteId),
        ) { note, related ->
            note?.let { NoteWithConnections(it, related) }
        }
}
