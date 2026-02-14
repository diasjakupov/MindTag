package io.diasjakupov.mindtag.feature.study.presentation.results

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.study.domain.usecase.GetResultsUseCase
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ResultsViewModel(
    private val sessionId: String,
    private val getResultsUseCase: GetResultsUseCase,
) : MviViewModel<ResultsState, ResultsIntent, ResultsEffect>(ResultsState()) {

    override val tag = "ResultsVM"

    init {
        loadResults()
    }

    private fun loadResults() {
        Logger.d(tag, "loadResults: start — sessionId=$sessionId")
        getResultsUseCase(sessionId)
            .filterNotNull()
            .onEach { result ->
                val scorePercent = result.scorePercent

                val (feedback, subtext) = when {
                    scorePercent >= 80 -> "Great job!" to "You're mastering this topic. Just a few more tweaks and you'll be perfect."
                    scorePercent >= 60 -> "Good effort!" to "You're making solid progress. Review the areas below to improve."
                    else -> "Keep practicing!" to "Don't worry, every attempt helps you learn. Focus on the insights below."
                }

                Logger.d(tag, "loadResults: success — score=$scorePercent%, correct=${result.totalCorrect}/${result.totalQuestions}, feedback='$feedback'")

                val answers = result.answers.map { detail ->
                    AnswerDetailUi(
                        cardId = detail.cardId,
                        questionText = detail.question,
                        isCorrect = detail.isCorrect,
                        userAnswer = detail.userAnswer,
                        correctAnswer = detail.correctAnswer,
                        aiInsight = detail.aiInsight,
                    )
                }

                updateState {
                    copy(
                        scorePercent = scorePercent,
                        feedbackMessage = feedback,
                        feedbackSubtext = subtext,
                        timeSpent = result.timeSpentFormatted,
                        answers = answers,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: ResultsIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is ResultsIntent.ToggleAnswer -> {
                updateState {
                    copy(
                        expandedAnswerId = if (expandedAnswerId == intent.cardId) null else intent.cardId,
                    )
                }
            }
            is ResultsIntent.TapReviewNotes -> {
                sendEffect(ResultsEffect.NavigateToLibrary)
            }
            is ResultsIntent.TapClose -> {
                sendEffect(ResultsEffect.NavigateBack)
            }
        }
    }
}
