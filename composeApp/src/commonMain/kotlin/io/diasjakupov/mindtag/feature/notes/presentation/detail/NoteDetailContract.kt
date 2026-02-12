package io.diasjakupov.mindtag.feature.notes.presentation.detail

import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote

data class NoteDetailState(
    val note: Note? = null,
    val subjectName: String = "",
    val subjectColorHex: String = "",
    val relatedNotes: List<RelatedNote> = emptyList(),
    val isLoading: Boolean = true,
    val isCreatingQuiz: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
)

sealed interface NoteDetailIntent {
    data object TapQuizMe : NoteDetailIntent
    data class TapRelatedNote(val noteId: String) : NoteDetailIntent
    data object NavigateBack : NoteDetailIntent
    data object TapEdit : NoteDetailIntent
    data object TapDelete : NoteDetailIntent
    data object ConfirmDelete : NoteDetailIntent
    data object DismissDeleteDialog : NoteDetailIntent
}

sealed interface NoteDetailEffect {
    data class NavigateToQuiz(val sessionId: String) : NoteDetailEffect
    data class NavigateToNote(val noteId: String) : NoteDetailEffect
    data object NavigateBack : NoteDetailEffect
    data class NavigateToEdit(val noteId: String) : NoteDetailEffect
    data class ShowError(val message: String) : NoteDetailEffect
}
