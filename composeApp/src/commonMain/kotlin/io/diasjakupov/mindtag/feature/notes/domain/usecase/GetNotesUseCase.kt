package io.diasjakupov.mindtag.feature.notes.domain.usecase

import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(subjectId: String? = null): Flow<List<Note>> =
        repository.getNotes(subjectId)
}
