package io.diasjakupov.mindtag.feature.notes.presentation.create

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import io.diasjakupov.mindtag.feature.notes.domain.usecase.CreateNoteUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetSubjectsUseCase
import kotlinx.coroutines.launch

class NoteCreateViewModel(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
    private val noteRepository: NoteRepository,
    private val noteId: Long? = null,
) : MviViewModel<NoteCreateState, NoteCreateIntent, NoteCreateEffect>(
    NoteCreateState(editNoteId = noteId, isEditMode = noteId != null)
) {

    override val tag = "NoteCreateVM"

    init {
        loadSubjects()
        if (noteId != null) loadExistingNote()
    }

    private fun loadExistingNote() {
        Logger.d(tag, "loadExistingNote: noteId=$noteId")
        viewModelScope.launch {
            try {
                val note = noteRepository.getNoteById(noteId!!)
                if (note != null) {
                    Logger.d(tag, "loadExistingNote: loaded — title='${note.title}'")
                    updateState {
                        copy(
                            title = note.title,
                            content = note.content,
                            subjectName = note.subjectName,
                        )
                    }
                }
            } catch (e: Exception) {
                Logger.e(tag, "loadExistingNote: error", e)
                sendEffect(NoteCreateEffect.ShowError("Failed to load note"))
            }
        }
    }

    private fun loadSubjects() {
        Logger.d(tag, "loadSubjects: start")
        viewModelScope.launch {
            try {
                val subjects = getSubjectsUseCase()
                Logger.d(tag, "loadSubjects: loaded ${subjects.size} subjects")
                updateState { copy(subjects = subjects) }
            } catch (e: Exception) {
                Logger.e(tag, "loadSubjects: error", e)
            }
        }
    }

    override fun onIntent(intent: NoteCreateIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is NoteCreateIntent.UpdateTitle -> updateState { copy(title = intent.title, titleError = null) }
            is NoteCreateIntent.UpdateContent -> updateState { copy(content = intent.content, contentError = null) }
            is NoteCreateIntent.UpdateSubjectName -> updateState { copy(subjectName = intent.name) }
            is NoteCreateIntent.SelectSubject -> updateState { copy(subjectName = intent.subjectName) }
            is NoteCreateIntent.Save -> save()
        }
    }

    private fun save() {
        val currentState = state.value
        if (currentState.title.isBlank()) {
            Logger.d(tag, "save: validation failed — title is blank")
            updateState { copy(titleError = "Title cannot be empty") }
            return
        }
        if (currentState.content.isBlank()) {
            Logger.d(tag, "save: validation failed — content is blank")
            updateState { copy(contentError = "Content cannot be empty") }
            return
        }
        if (currentState.subjectName.isBlank()) {
            Logger.d(tag, "save: validation failed — no subject")
            sendEffect(NoteCreateEffect.ShowError("Please enter a subject name"))
            return
        }

        Logger.d(tag, "save: start — title='${currentState.title}', subject='${currentState.subjectName}'")
        updateState { copy(isSaving = true) }
        viewModelScope.launch {
            try {
                if (currentState.isEditMode && currentState.editNoteId != null) {
                    noteRepository.updateNote(
                        id = currentState.editNoteId,
                        title = currentState.title,
                        content = currentState.content,
                        subjectName = currentState.subjectName,
                    )
                    Logger.d(tag, "save: update success")
                } else {
                    createNoteUseCase(
                        title = currentState.title,
                        content = currentState.content,
                        subjectName = currentState.subjectName,
                    )
                    Logger.d(tag, "save: create success")
                }
                sendEffect(NoteCreateEffect.NavigateBackWithSuccess)
            } catch (e: Exception) {
                Logger.e(tag, "save: error", e)
                updateState { copy(isSaving = false) }
                sendEffect(NoteCreateEffect.ShowError(e.message ?: "Failed to save note"))
            }
        }
    }
}
