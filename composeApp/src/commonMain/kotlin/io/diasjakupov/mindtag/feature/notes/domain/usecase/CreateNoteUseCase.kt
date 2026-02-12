package io.diasjakupov.mindtag.feature.notes.domain.usecase

import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository

class CreateNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(title: String, content: String, subjectName: String): Note {
        require(title.isNotBlank()) { "Note title must not be blank" }
        return repository.createNote(title.trim(), content, subjectName)
    }
}
