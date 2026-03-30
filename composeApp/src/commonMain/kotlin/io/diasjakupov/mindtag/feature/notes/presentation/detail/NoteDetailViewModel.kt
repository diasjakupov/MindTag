package io.diasjakupov.mindtag.feature.notes.presentation.detail

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetNoteWithConnectionsUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetSubjectsUseCase
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    private val noteId: Long,
    private val getNoteWithConnectionsUseCase: GetNoteWithConnectionsUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
    private val noteRepository: NoteRepository,
    private val backendQuizRepository: BackendQuizRepository,
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
            is NoteDetailIntent.TapQuizHistory -> sendEffect(NoteDetailEffect.NavigateToQuizHistory(noteId))
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
        if (state.value.isCreatingQuiz) return
        Logger.d(tag, "startQuiz: noteId=$noteId")
        updateState { copy(isCreatingQuiz = true, quizGenerationStatus = "Generating quiz...") }

        viewModelScope.launch {
            // Step 1: Generate quiz
            val generateResult = backendQuizRepository.generateQuiz(noteId)
            if (generateResult is ApiResult.Error) {
                Logger.e(tag, "startQuiz: generateQuiz failed — ${generateResult.message}")
                updateState { copy(isCreatingQuiz = false, quizGenerationStatus = "") }
                sendEffect(NoteDetailEffect.ShowError(generateResult.message))
                return@launch
            }
            val quizSummary = (generateResult as ApiResult.Success).data
            val quizId = quizSummary.id
            Logger.d(tag, "startQuiz: quiz generated — quizId=$quizId")

            // Step 2: Poll until ready
            updateState { copy(quizGenerationStatus = "Waiting for AI...") }
            val pollResult = backendQuizRepository.pollUntilReady(quizId)
            if (pollResult is ApiResult.Error) {
                Logger.e(tag, "startQuiz: pollUntilReady failed — ${pollResult.message}")
                updateState { copy(isCreatingQuiz = false, quizGenerationStatus = "") }
                sendEffect(NoteDetailEffect.ShowError(pollResult.message))
                return@launch
            }
            Logger.d(tag, "startQuiz: quiz is READY — quizId=$quizId")

            // Step 3: Start attempt
            updateState { copy(quizGenerationStatus = "Starting quiz...") }
            val attemptResult = backendQuizRepository.startAttempt(quizId)
            if (attemptResult is ApiResult.Error) {
                Logger.e(tag, "startQuiz: startAttempt failed — ${attemptResult.message}")
                updateState { copy(isCreatingQuiz = false, quizGenerationStatus = "") }
                sendEffect(NoteDetailEffect.ShowError(attemptResult.message))
                return@launch
            }
            val attempt = (attemptResult as ApiResult.Success).data
            Logger.d(tag, "startQuiz: attempt started — attemptId=${attempt.attemptId}")
            updateState { copy(isCreatingQuiz = false, quizGenerationStatus = "") }
            sendEffect(NoteDetailEffect.NavigateToBackendQuiz(quizId = quizId, attemptId = attempt.attemptId))
        }
    }
}
