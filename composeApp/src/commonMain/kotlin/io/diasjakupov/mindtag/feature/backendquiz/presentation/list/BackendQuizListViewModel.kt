package io.diasjakupov.mindtag.feature.backendquiz.presentation.list

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.onError
import io.diasjakupov.mindtag.core.network.onSuccess
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.launch

class BackendQuizListViewModel(
    private val noteId: Long?,
    private val repo: BackendQuizRepository,
) : MviViewModel<BackendQuizListState, BackendQuizListIntent, BackendQuizListEffect>(
    BackendQuizListState(noteId = noteId),
) {
    override val tag = "BackendQuizListVM"

    init {
        loadQuizzes()
    }

    override fun onIntent(intent: BackendQuizListIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is BackendQuizListIntent.TapQuiz -> onTapQuiz(intent.quizId)
            is BackendQuizListIntent.DeleteQuiz -> onDeleteQuiz(intent.quizId)
            is BackendQuizListIntent.Refresh -> loadQuizzes()
            is BackendQuizListIntent.NavigateBack -> sendEffect(BackendQuizListEffect.NavigateBack)
        }
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            val result = if (noteId != null) {
                repo.getQuizzesForNote(noteId)
            } else {
                repo.getAllQuizzes()
            }
            result
                .onSuccess { quizzes ->
                    updateState {
                        copy(
                            isLoading = false,
                            quizzes = quizzes.map { q ->
                                QuizListItemUi(
                                    quizId = q.id,
                                    noteTitleSnapshot = q.noteTitleSnapshot,
                                    status = q.status,
                                    questionCount = q.questionCount,
                                    createdAt = q.createdAt,
                                )
                            },
                        )
                    }
                }
                .onError { msg, _ ->
                    Logger.e(tag, "loadQuizzes error: $msg")
                    updateState { copy(isLoading = false, errorMessage = msg) }
                }
        }
    }

    private fun onTapQuiz(quizId: Long) {
        val quiz = state.value.quizzes.find { it.quizId == quizId } ?: return
        if (quiz.status != QuizStatus.READY) {
            sendEffect(BackendQuizListEffect.ShowError("Quiz is not ready yet"))
            return
        }
        viewModelScope.launch {
            repo.startAttempt(quizId)
                .onSuccess { attempt ->
                    sendEffect(BackendQuizListEffect.NavigateToAttempt(quizId, attempt.attemptId))
                }
                .onError { msg, _ ->
                    sendEffect(BackendQuizListEffect.ShowError(msg))
                }
        }
    }

    private fun onDeleteQuiz(quizId: Long) {
        viewModelScope.launch {
            repo.deleteQuiz(quizId)
                .onSuccess { loadQuizzes() }
                .onError { msg, _ -> sendEffect(BackendQuizListEffect.ShowError(msg)) }
        }
    }
}
