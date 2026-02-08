package io.diasjakupov.mindtag.feature.notes.presentation.create

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.notes.domain.usecase.CreateNoteUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetSubjectsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class NoteCreateViewModel(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
) : MviViewModel<NoteCreateState, NoteCreateIntent, NoteCreateEffect>(NoteCreateState()) {

    override val tag = "NoteCreateVM"

    init {
        loadSubjects()
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
                updateState { copy(content = intent.content) }
            }
            is NoteCreateIntent.SelectSubject -> {
                updateState { copy(selectedSubjectId = intent.subjectId) }
            }
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
                createNoteUseCase(
                    title = currentState.title,
                    content = currentState.content,
                    subjectId = subjectId,
                )
                Logger.d(tag, "save: success")
                sendEffect(NoteCreateEffect.NavigateBackWithSuccess)
            } catch (e: Exception) {
                Logger.e(tag, "save: error", e)
                updateState { copy(isSaving = false) }
                sendEffect(NoteCreateEffect.ShowError(e.message ?: "Failed to save note"))
            }
        }
    }
}
