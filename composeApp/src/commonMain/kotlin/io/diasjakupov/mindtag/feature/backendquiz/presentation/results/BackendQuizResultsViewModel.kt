package io.diasjakupov.mindtag.feature.backendquiz.presentation.results

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.onError
import io.diasjakupov.mindtag.core.network.onSuccess
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.launch

class BackendQuizResultsViewModel(
    private val quizId: Long,
    private val attemptId: Long,
    private val repo: BackendQuizRepository,
) : MviViewModel<BackendQuizResultsState, BackendQuizResultsIntent, BackendQuizResultsEffect>(
    BackendQuizResultsState()
) {
    override val tag = "BackendQuizResultsVM"

    init { loadResults() }

    private fun loadResults() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            repo.getAttemptResult(quizId, attemptId)
                .onSuccess { result ->
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
                }
                .onError { msg, _ ->
                    updateState { copy(isLoading = false, errorMessage = msg) }
                }
        }
    }

    override fun onIntent(intent: BackendQuizResultsIntent) {
        when (intent) {
            is BackendQuizResultsIntent.ToggleQuestion -> toggleQuestion(intent.questionId)
            is BackendQuizResultsIntent.TapClose -> sendEffect(BackendQuizResultsEffect.NavigateBack)
            is BackendQuizResultsIntent.TapRetry -> sendEffect(BackendQuizResultsEffect.NavigateBack)
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
