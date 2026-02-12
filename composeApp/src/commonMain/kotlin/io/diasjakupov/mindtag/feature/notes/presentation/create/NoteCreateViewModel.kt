package io.diasjakupov.mindtag.feature.notes.presentation.create

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import io.diasjakupov.mindtag.feature.notes.domain.usecase.CreateNoteUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetSubjectsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class NoteCreateViewModel(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
    private val noteRepository: NoteRepository,
    private val noteId: String? = null,
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
        noteRepository.getNoteById(noteId!!)
            .onEach { note ->
                if (note != null) {
                    Logger.d(tag, "loadExistingNote: loaded — title='${note.title}'")
                    updateState {
                        copy(
                            title = note.title,
                            content = note.content,
                            selectedSubjectId = note.subjectId,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadSubjects() {
        Logger.d(tag, "loadSubjects: start")
        getSubjectsUseCase()
            .onEach { subjects ->
                Logger.d(tag, "loadSubjects: loaded ${subjects.size} subjects")
                updateState {
                    copy(
                        subjects = subjects,
                        selectedSubjectId = selectedSubjectId ?: subjects.firstOrNull()?.id,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: NoteCreateIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is NoteCreateIntent.UpdateTitle -> {
                updateState { copy(title = intent.title, titleError = null) }
            }
            is NoteCreateIntent.UpdateContent -> {
                updateState { copy(content = intent.content, contentError = null) }
            }
            is NoteCreateIntent.SelectSubject -> {
                updateState { copy(selectedSubjectId = intent.subjectId) }
            }
            is NoteCreateIntent.Save -> save()
            is NoteCreateIntent.TapAddSubject -> {
                updateState {
                    copy(
                        showCreateSubjectDialog = true,
                        newSubjectName = "",
                        newSubjectColor = "#135BEC",
                        newSubjectNameError = null,
                    )
                }
            }
            is NoteCreateIntent.UpdateNewSubjectName -> {
                updateState { copy(newSubjectName = intent.name, newSubjectNameError = null) }
            }
            is NoteCreateIntent.UpdateNewSubjectColor -> {
                updateState { copy(newSubjectColor = intent.colorHex) }
            }
            is NoteCreateIntent.DismissCreateSubjectDialog -> {
                updateState { copy(showCreateSubjectDialog = false) }
            }
            is NoteCreateIntent.ConfirmCreateSubject -> createSubject()
        }
    }

    private fun createSubject() {
        val currentState = state.value
        val name = currentState.newSubjectName.trim()
        if (name.isBlank()) {
            Logger.d(tag, "createSubject: validation failed — name is blank")
            updateState { copy(newSubjectNameError = "Subject name cannot be empty") }
            return
        }
        viewModelScope.launch {
            try {
                val subject = noteRepository.createSubject(name, currentState.newSubjectColor, "book")
                Logger.d(tag, "createSubject: success — id=${subject.id}")
                updateState {
                    copy(
                        showCreateSubjectDialog = false,
                        selectedSubjectId = subject.id,
                        subjects = subjects + subject,
                    )
                }
            } catch (e: Exception) {
                Logger.e(tag, "createSubject: error", e)
                updateState { copy(showCreateSubjectDialog = false) }
                sendEffect(NoteCreateEffect.ShowError(e.message ?: "Failed to create subject"))
            }
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
        val subjectId = currentState.selectedSubjectId
        if (subjectId == null) {
            Logger.d(tag, "save: validation failed — no subject selected")
            sendEffect(NoteCreateEffect.ShowError("Please select a subject"))
            return
        }

        Logger.d(tag, "save: start — title='${currentState.title}', subjectId=$subjectId")
        updateState { copy(isSaving = true) }
        viewModelScope.launch {
            try {
                if (currentState.isEditMode && currentState.editNoteId != null) {
                    noteRepository.updateNote(
                        id = currentState.editNoteId,
                        title = currentState.title,
                        content = currentState.content,
                    )
                    Logger.d(tag, "save: update success")
                } else {
                    createNoteUseCase(
                        title = currentState.title,
                        content = currentState.content,
                        subjectId = subjectId,
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
