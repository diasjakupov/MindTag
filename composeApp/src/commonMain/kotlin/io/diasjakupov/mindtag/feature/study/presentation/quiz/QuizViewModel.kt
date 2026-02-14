package io.diasjakupov.mindtag.feature.study.presentation.quiz

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.study.domain.model.FlashCard
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository
import io.diasjakupov.mindtag.feature.study.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class QuizViewModel(
    private val sessionId: String,
    private val studyRepository: StudyRepository,
    private val submitAnswerUseCase: SubmitAnswerUseCase,
) : MviViewModel<QuizState, QuizIntent, QuizEffect>(QuizState()) {

    override val tag = "QuizVM"

    private var timerJob: Job? = null
    private var cards: List<FlashCard> = emptyList()
    private var timerTickCount = 0

    init {
        loadSession()
    }

    private fun loadSession() {
        Logger.d(tag, "loadSession: start — sessionId=$sessionId")
        viewModelScope.launch {
            try {
                val session = studyRepository.getSession(sessionId).firstOrNull() ?: run {
                    Logger.e(tag, "loadSession: session not found")
                    return@launch
                }
                val loadedCards = studyRepository.getCardsForSession(
                    session.subjectId,
                    session.totalQuestions,
                ).firstOrNull() ?: emptyList()

                cards = loadedCards
                val firstCard = cards.firstOrNull()

                Logger.d(tag, "loadSession: success — type=${session.sessionType}, cards=${cards.size}")

                updateState {
                    copy(
                        sessionType = session.sessionType.name,
                        totalQuestions = cards.size,
                        currentQuestionIndex = 0,
                        progressPercent = if (cards.isNotEmpty()) (1f / cards.size) * 100f else 0f,
                        currentQuestion = firstCard?.question ?: "",
                        currentOptions = firstCard?.toOptionUiList() ?: emptyList(),
                        selectedOptionId = null,
                        isAnswerSubmitted = false,
                        timeRemainingSeconds = if (session.sessionType == SessionType.EXAM_MODE) session.timeLimitSeconds else null,
                        timeRemainingFormatted = if (session.sessionType == SessionType.EXAM_MODE) formatTime(session.timeLimitSeconds ?: 0) else "",
                        isLoading = false,
                        isLastQuestion = cards.size <= 1,
                    )
                }

                if (session.sessionType == SessionType.EXAM_MODE) {
                    startTimer()
                }
            } catch (e: Exception) {
                Logger.e(tag, "loadSession: error", e)
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerTickCount = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                onIntent(QuizIntent.TimerTick)
            }
        }
    }

    override fun onIntent(intent: QuizIntent) {
        when (intent) {
            is QuizIntent.SelectOption -> {
                Logger.d(tag, "onIntent: SelectOption id=${intent.optionId}")
                updateState { copy(selectedOptionId = intent.optionId) }
            }
            is QuizIntent.TapNext -> handleNext()
            is QuizIntent.TapExit -> {
                Logger.d(tag, "onIntent: TapExit")
                timerJob?.cancel()
                sendEffect(QuizEffect.ShowExitConfirmation)
            }
            is QuizIntent.TimerTick -> handleTimerTick()
        }
    }

    private fun handleNext() {
        val currentState = state.value
        val selectedId = currentState.selectedOptionId ?: return
        val currentIndex = currentState.currentQuestionIndex
        val currentCard = cards.getOrNull(currentIndex) ?: return

        val selectedOption = currentCard.options.find { it.id == selectedId }
        val isCorrect = selectedOption?.isCorrect == true
        Logger.d(tag, "handleNext: q=${currentIndex + 1}/${currentState.totalQuestions}, correct=$isCorrect")

        viewModelScope.launch {
            try {
                submitAnswerUseCase(
                    sessionId = sessionId,
                    cardId = currentCard.id,
                    userAnswer = selectedOption?.text ?: "",
                    isCorrect = isCorrect,
                    confidenceRating = null,
                    timeSpentSeconds = 0,
                    currentQuestionIndex = currentIndex,
                    totalQuestions = currentState.totalQuestions,
                )
            } catch (e: Exception) {
                Logger.e(tag, "handleNext: submitAnswer error", e)
            }

            if (currentState.isLastQuestion) {
                Logger.d(tag, "handleNext: last question — navigating to results")
                timerJob?.cancel()
                sendEffect(QuizEffect.NavigateToResults(sessionId))
                return@launch
            }

            val nextIndex = currentIndex + 1
            val nextCard = cards.getOrNull(nextIndex)

            if (nextCard != null) {
                updateState {
                    copy(
                        currentQuestionIndex = nextIndex,
                        progressPercent = ((nextIndex + 1).toFloat() / totalQuestions) * 100f,
                        currentQuestion = nextCard.question,
                        currentOptions = nextCard.toOptionUiList(),
                        selectedOptionId = null,
                        isAnswerSubmitted = false,
                        isLastQuestion = nextIndex >= totalQuestions - 1,
                    )
                }
            } else {
                timerJob?.cancel()
                sendEffect(QuizEffect.NavigateToResults(sessionId))
            }
        }
    }

    private fun handleTimerTick() {
        val current = state.value.timeRemainingSeconds ?: return
        val newTime = current - 1
        timerTickCount++

        if (timerTickCount % 10 == 0) {
            Logger.d(tag, "timer: ${formatTime(newTime)} remaining")
        }

        if (newTime <= 0) {
            Logger.d(tag, "timer: expired — navigating to results")
            timerJob?.cancel()
            updateState {
                copy(
                    timeRemainingSeconds = 0,
                    timeRemainingFormatted = "00:00",
                )
            }
            sendEffect(QuizEffect.NavigateToResults(sessionId))
        } else {
            updateState {
                copy(
                    timeRemainingSeconds = newTime,
                    timeRemainingFormatted = formatTime(newTime),
                )
            }
        }
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

private fun FlashCard.toOptionUiList(): List<QuizOptionUi> =
    options.map { option ->
        QuizOptionUi(
            id = option.id,
            text = option.text,
        )
    }
