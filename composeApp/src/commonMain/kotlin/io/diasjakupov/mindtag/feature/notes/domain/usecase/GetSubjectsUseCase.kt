package io.diasjakupov.mindtag.feature.notes.domain.usecase

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository

class GetSubjectsUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(): List<Subject> = repository.getSubjects()
}
