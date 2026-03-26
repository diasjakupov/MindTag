package io.diasjakupov.mindtag.feature.backendquiz.presentation.attempt

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.dto.AnswerRequestDto
import io.diasjakupov.mindtag.core.network.onError
import io.diasjakupov.mindtag.core.network.onSuccess
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizQuestion
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.launch

class BackendQuizAttemptViewModel(
    private val quizId: Long,
    private val attemptId: Long,
    private val repo: BackendQuizRepository,
) : MviViewModel<BackendQuizAttemptState, BackendQuizAttemptIntent, BackendQuizAttemptEffect>(
    BackendQuizAttemptState(quizId = quizId, attemptId = attemptId)
) {
    override val tag = "BackendQuizAttemptVM"

    private var questions: List<QuizQuestion> = emptyList()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            repo.getQuiz(quizId)
                .onSuccess { detail ->
                    val sorted = detail.questions.sortedBy { it.orderIndex }
                    questions = sorted
                    val first = sorted.firstOrNull()
                    updateState {
                        copy(
                            isLoading = false,
                            noteTitleSnapshot = detail.noteTitleSnapshot,
                            totalQuestions = sorted.size,
                            questionIds = sorted.map { it.id },
                            questionText = first?.questionText ?: "",
                            options = first?.toOptionUiList(selectedAnswer = null) ?: emptyList(),
                            currentIndex = 0,
                            selectedAnswer = null,
                        )
                    }
                }
                .onError { msg, _ ->
                    updateState { copy(isLoading = false, errorMessage = msg) }
                }
        }
    }

    override fun onIntent(intent: BackendQuizAttemptIntent) {
        when (intent) {
            is BackendQuizAttemptIntent.SelectOption -> selectOption(intent.label)
            is BackendQuizAttemptIntent.TapNext -> navigateQuestion(+1)
            is BackendQuizAttemptIntent.TapPrevious -> navigateQuestion(-1)
            is BackendQuizAttemptIntent.TapSubmit -> submitAnswers()
            is BackendQuizAttemptIntent.TapBack -> sendEffect(BackendQuizAttemptEffect.NavigateBack)
        }
    }

    private fun selectOption(label: String) {
        val s = state.value
        val currentQuestionId = s.questionIds.getOrNull(s.currentIndex) ?: return
        updateState {
            copy(
                selectedAnswer = label,
                answers = answers + (currentQuestionId to label),
                options = options.map { it.copy(isSelected = it.label == label) },
            )
        }
    }

    private fun navigateQuestion(delta: Int) {
        val s = state.value
        if (questions.isEmpty()) return
        val newIndex = (s.currentIndex + delta).coerceIn(0, s.totalQuestions - 1)
        if (newIndex == s.currentIndex) return
        val question = questions.getOrNull(newIndex) ?: return
        val existingAnswer = s.answers[question.id]
        updateState {
            copy(
                currentIndex = newIndex,
                questionText = question.questionText,
                options = question.toOptionUiList(selectedAnswer = existingAnswer),
                selectedAnswer = existingAnswer,
            )
        }
    }

    private fun submitAnswers() {
        viewModelScope.launch {
            updateState { copy(isSubmitting = true) }
            val s = state.value
            val answerList = s.questionIds.map { qId ->
                AnswerRequestDto(questionId = qId, answer = s.answers[qId] ?: "A")
            }
            repo.submitAttempt(quizId, attemptId, answerList)
                .onSuccess {
                    sendEffect(BackendQuizAttemptEffect.NavigateToResults(quizId, attemptId))
                }
                .onError { msg, _ ->
                    updateState { copy(isSubmitting = false, errorMessage = msg) }
                }
        }
    }
}

private fun QuizQuestion.toOptionUiList(selectedAnswer: String?): List<QuizOptionUi> {
    val labels = listOf("A", "B", "C", "D")
    return options.mapIndexed { i, text ->
        val label = labels.getOrElse(i) { i.toString() }
        QuizOptionUi(label = label, text = text, isSelected = label == selectedAnswer)
    }
}
