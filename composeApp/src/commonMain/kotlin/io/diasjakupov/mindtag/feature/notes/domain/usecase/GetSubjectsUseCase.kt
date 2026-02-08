package io.diasjakupov.mindtag.feature.notes.domain.usecase

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetSubjectsUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Subject>> = repository.getSubjects()
}
