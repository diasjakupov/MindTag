package io.diasjakupov.mindtag.feature.notes.presentation.detail

import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote

data class NoteDetailState(
    val note: Note? = null,
    val subjectName: String = "",
    val subjectColorHex: String = "",
    val relatedNotes: List<RelatedNote> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface NoteDetailIntent {
    data object TapQuizMe : NoteDetailIntent
    data class TapRelatedNote(val noteId: String) : NoteDetailIntent
    data object NavigateBack : NoteDetailIntent
}

sealed interface NoteDetailEffect {
    data class NavigateToQuiz(val sessionId: String) : NoteDetailEffect
    data class NavigateToNote(val noteId: String) : NoteDetailEffect
    data object NavigateBack : NoteDetailEffect
}
