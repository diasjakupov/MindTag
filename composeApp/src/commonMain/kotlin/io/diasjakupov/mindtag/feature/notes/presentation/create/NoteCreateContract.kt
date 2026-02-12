package io.diasjakupov.mindtag.feature.notes.presentation.create

import io.diasjakupov.mindtag.core.domain.model.Subject

data class NoteCreateState(
    val title: String = "",
    val content: String = "",
    val subjectName: String = "",
    val subjects: List<Subject> = emptyList(),
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val contentError: String? = null,
    val editNoteId: Long? = null,
    val isEditMode: Boolean = false,
)

sealed interface NoteCreateIntent {
    data class UpdateTitle(val title: String) : NoteCreateIntent
    data class UpdateContent(val content: String) : NoteCreateIntent
    data class UpdateSubjectName(val name: String) : NoteCreateIntent
    data class SelectSubject(val subjectName: String) : NoteCreateIntent
    data object Save : NoteCreateIntent
}

sealed interface NoteCreateEffect {
    data object NavigateBackWithSuccess : NoteCreateEffect
    data class ShowError(val message: String) : NoteCreateEffect
}
