package io.diasjakupov.mindtag.feature.notes.presentation.create

import io.diasjakupov.mindtag.core.domain.model.Subject

data class NoteCreateState(
    val title: String = "",
    val content: String = "",
    val selectedSubjectId: String? = null,
    val subjects: List<Subject> = emptyList(),
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val contentError: String? = null,
    val showCreateSubjectDialog: Boolean = false,
    val newSubjectName: String = "",
    val newSubjectColor: String = "#135BEC",
    val newSubjectNameError: String? = null,
    val editNoteId: String? = null,
    val isEditMode: Boolean = false,
)

sealed interface NoteCreateIntent {
    data class UpdateTitle(val title: String) : NoteCreateIntent
    data class UpdateContent(val content: String) : NoteCreateIntent
    data class SelectSubject(val subjectId: String) : NoteCreateIntent
    data object Save : NoteCreateIntent
    data object TapAddSubject : NoteCreateIntent
    data class UpdateNewSubjectName(val name: String) : NoteCreateIntent
    data class UpdateNewSubjectColor(val colorHex: String) : NoteCreateIntent
    data object ConfirmCreateSubject : NoteCreateIntent
    data object DismissCreateSubjectDialog : NoteCreateIntent
}

sealed interface NoteCreateEffect {
    data object NavigateBackWithSuccess : NoteCreateEffect
    data class ShowError(val message: String) : NoteCreateEffect
}
