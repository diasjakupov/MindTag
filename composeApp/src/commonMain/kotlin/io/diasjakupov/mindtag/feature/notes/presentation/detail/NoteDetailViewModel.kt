package io.diasjakupov.mindtag.feature.notes.presentation.detail

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetNoteWithConnectionsUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetSubjectsUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NoteDetailViewModel(
    private val noteId: String,
    private val getNoteWithConnectionsUseCase: GetNoteWithConnectionsUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
) : MviViewModel<NoteDetailState, NoteDetailIntent, NoteDetailEffect>(NoteDetailState()) {

    override val tag = "NoteDetailVM"

    init {
        loadNote()
    }

    private fun loadNote() {
        Logger.d(tag, "loadNote: start — noteId=$noteId")
        combine(
            getNoteWithConnectionsUseCase(noteId),
            getSubjectsUseCase(),
        ) { noteWithConnections, subjects ->
            if (noteWithConnections != null) {
                val subject = subjects.find { it.id == noteWithConnections.note.subjectId }
                Logger.d(tag, "loadNote: success — title='${noteWithConnections.note.title}', related=${noteWithConnections.relatedNotes.size}")
                updateState {
                    copy(
                        note = noteWithConnections.note,
                        subjectName = subject?.name ?: "",
                        subjectColorHex = subject?.colorHex ?: "",
                        relatedNotes = noteWithConnections.relatedNotes,
                        isLoading = false,
                    )
                }
            } else {
                Logger.d(tag, "loadNote: note not found — noteId=$noteId")
                updateState { copy(isLoading = false) }
            }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: NoteDetailIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is NoteDetailIntent.TapQuizMe -> {
                // Quiz wiring comes in Phase 3 - for now no-op
            }
            is NoteDetailIntent.TapRelatedNote -> {
                sendEffect(NoteDetailEffect.NavigateToNote(intent.noteId))
            }
            is NoteDetailIntent.NavigateBack -> {
                sendEffect(NoteDetailEffect.NavigateBack)
            }
        }
    }
}
