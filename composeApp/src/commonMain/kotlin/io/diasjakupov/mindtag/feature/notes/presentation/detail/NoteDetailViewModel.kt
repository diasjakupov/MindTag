package io.diasjakupov.mindtag.feature.notes.presentation.detail

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetNoteWithConnectionsUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetSubjectsUseCase
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.usecase.StartQuizUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    private val noteId: Long,
    private val getNoteWithConnectionsUseCase: GetNoteWithConnectionsUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
    private val noteRepository: NoteRepository,
    private val startQuizUseCase: StartQuizUseCase,
) : MviViewModel<NoteDetailState, NoteDetailIntent, NoteDetailEffect>(NoteDetailState()) {

    override val tag = "NoteDetailVM"

    init {
        loadNote()
    }

    private fun loadNote() {
        Logger.d(tag, "loadNote: start — noteId=$noteId")
        viewModelScope.launch {
            try {
                val noteWithConnections = getNoteWithConnectionsUseCase(noteId)
                if (noteWithConnections != null) {
                    val subjects = getSubjectsUseCase()
                    val subject = subjects.find { it.id == noteWithConnections.note.subjectId }
                    Logger.d(tag, "loadNote: success — title='${noteWithConnections.note.title}', related=${noteWithConnections.relatedNotes.size}")
                    updateState {
                        copy(
                            note = noteWithConnections.note,
                            subjectName = noteWithConnections.note.subjectName.ifEmpty { subject?.name ?: "" },
                            subjectColorHex = subject?.colorHex ?: "",
                            relatedNotes = noteWithConnections.relatedNotes,
                            isLoading = false,
                        )
                    }
                } else {
                    Logger.d(tag, "loadNote: note not found — noteId=$noteId")
                    updateState { copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Logger.e(tag, "loadNote: error", e)
                updateState { copy(isLoading = false) }
            }
        }
    }

    override fun onIntent(intent: NoteDetailIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is NoteDetailIntent.TapQuizMe -> startQuiz()
            is NoteDetailIntent.TapRelatedNote -> sendEffect(NoteDetailEffect.NavigateToNote(intent.noteId))
            is NoteDetailIntent.NavigateBack -> sendEffect(NoteDetailEffect.NavigateBack)
            is NoteDetailIntent.TapEdit -> sendEffect(NoteDetailEffect.NavigateToEdit(noteId))
            is NoteDetailIntent.TapDelete -> updateState { copy(showDeleteConfirmation = true) }
            is NoteDetailIntent.ConfirmDelete -> deleteNote()
            is NoteDetailIntent.DismissDeleteDialog -> updateState { copy(showDeleteConfirmation = false) }
        }
    }

    private fun deleteNote() {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(noteId)
                Logger.d(tag, "deleteNote: success")
                sendEffect(NoteDetailEffect.NavigateBack)
            } catch (e: Exception) {
                Logger.e(tag, "deleteNote: error", e)
                sendEffect(NoteDetailEffect.ShowError("Failed to delete note"))
            }
        }
    }

    private fun startQuiz() {
        val subjectId = state.value.note?.subjectId ?: return
        if (state.value.isCreatingQuiz) return
        Logger.d(tag, "startQuiz: subjectId=$subjectId")
        updateState { copy(isCreatingQuiz = true) }

        viewModelScope.launch {
            try {
                val quizData = startQuizUseCase(
                    type = SessionType.QUICK_QUIZ,
                    subjectId = subjectId,
                    questionCount = 10,
                )
                val cards = quizData.cards.firstOrNull()
                if (cards.isNullOrEmpty()) {
                    Logger.d(tag, "startQuiz: no flashcards for subject")
                    updateState { copy(isCreatingQuiz = false) }
                    sendEffect(NoteDetailEffect.ShowError("No quiz questions available for this subject yet"))
                    return@launch
                }
                Logger.d(tag, "startQuiz: success — sessionId=${quizData.session.id}")
                updateState { copy(isCreatingQuiz = false) }
                sendEffect(NoteDetailEffect.NavigateToQuiz(quizData.session.id))
            } catch (e: Exception) {
                Logger.e(tag, "startQuiz: error", e)
                updateState { copy(isCreatingQuiz = false) }
                sendEffect(NoteDetailEffect.ShowError("Failed to start quiz"))
            }
        }
    }
}
