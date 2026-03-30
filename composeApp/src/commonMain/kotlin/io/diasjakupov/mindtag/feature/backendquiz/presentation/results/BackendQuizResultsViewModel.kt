package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.onError
import io.diasjakupov.mindtag.core.network.onSuccess
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuestionResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import io.diasjakupov.mindtag.feature.study.domain.usecase.SaveToStudyUseCase
import kotlinx.coroutines.launch

class BackendQuizResultsViewModel(
    private val quizId: Long,
    private val attemptId: Long,
    private val repo: BackendQuizRepository,
    private val noteRepository: NoteRepository,
    private val saveToStudyUseCase: SaveToStudyUseCase,
) : MviViewModel<BackendQuizResultsState, BackendQuizResultsIntent, BackendQuizResultsEffect>(
    BackendQuizResultsState()
) {
    override val tag = "BackendQuizResultsVM"

    private var domainResults: List<QuestionResult> = emptyList()

    init { loadResults() }

    private fun loadResults() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            repo.getAttemptResult(quizId, attemptId)
                .onSuccess { result ->
                    domainResults = result.questionResults
                    updateState {
                        copy(
                            isLoading = false,
                            noteTitleSnapshot = result.noteTitleSnapshot,
                            score = result.score,
                            totalQuestions = result.totalQuestions,
                            correctAnswers = result.correctAnswers,
                            feedbackMessage = when {
                                result.score >= 80 -> "Great work! 🎉"
                                result.score >= 60 -> "Good job! Keep it up!"
                                else -> "Keep practicing!"
                            },
                            questionResults = result.questionResults.map { qr ->
                                QuestionResultUi(
                                    questionId = qr.questionId,
                                    questionText = qr.questionText,
                                    options = qr.options,
                                    userAnswer = qr.userAnswer,
                                    correctAnswer = qr.correctAnswer,
                                    isCorrect = qr.correct,
                                    explanation = qr.explanation,
                                )
                            },
                        )
                    }
                    resolveSubject()
                }
                .onError { msg, _ ->
                    updateState { copy(isLoading = false, errorMessage = msg) }
                }
        }
    }

    private suspend fun resolveSubject() {
        val quizResult = repo.getQuiz(quizId)
        if (quizResult is ApiResult.Success) {
            val detail = quizResult.data
            val noteId = detail.noteId
            updateState { copy(noteId = noteId) }
            try {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    updateState { copy(subjectId = note.subjectId) }
                } else {
                    updateState { copy(subjectId = state.value.noteTitleSnapshot) }
                }
            } catch (_: Exception) {
                updateState { copy(subjectId = state.value.noteTitleSnapshot) }
            }
        } else {
            updateState { copy(subjectId = state.value.noteTitleSnapshot) }
        }
    }

    override fun onIntent(intent: BackendQuizResultsIntent) {
        when (intent) {
            is BackendQuizResultsIntent.ToggleQuestion -> toggleQuestion(intent.questionId)
            is BackendQuizResultsIntent.TapClose -> sendEffect(BackendQuizResultsEffect.NavigateBack)
            is BackendQuizResultsIntent.TapRetry -> loadResults()
            is BackendQuizResultsIntent.TapSaveToStudy -> updateState { copy(showSaveDialog = true) }
            is BackendQuizResultsIntent.ConfirmSave -> saveToStudy(intent.saveAll)
            is BackendQuizResultsIntent.DismissSaveDialog -> updateState { copy(showSaveDialog = false) }
        }
    }

    private fun saveToStudy(saveAll: Boolean) {
        viewModelScope.launch {
            updateState { copy(showSaveDialog = false, isSaving = true) }
            val s = state.value
            val count = saveToStudyUseCase(
                questions = domainResults,
                subjectId = s.subjectId,
                sourceNoteId = s.noteId,
                saveAll = saveAll,
            )
            updateState { copy(isSaving = false, hasSaved = true) }
            sendEffect(BackendQuizResultsEffect.ShowSnackbar("$count cards saved to Study"))
        }
    }

    private fun toggleQuestion(questionId: Long) {
        updateState {
            copy(
                questionResults = questionResults.map { qr ->
                    if (qr.questionId == questionId) qr.copy(isExpanded = !qr.isExpanded) else qr
                }
            )
        }
    }
}
