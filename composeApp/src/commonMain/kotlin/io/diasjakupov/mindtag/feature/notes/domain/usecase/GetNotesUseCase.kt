package io.diasjakupov.mindtag.feature.notes.domain.usecase

import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository

class GetNotesUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(subjectFilter: String? = null): List<Note> =
        repository.getNotes(subjectFilter)
}
