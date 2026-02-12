package io.diasjakupov.mindtag.feature.study.presentation.hub

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository
import io.diasjakupov.mindtag.feature.study.domain.usecase.StartQuizUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class StudyHubViewModel(
    private val startQuizUseCase: StartQuizUseCase,
    private val studyRepository: StudyRepository,
) : MviViewModel<StudyHubState, StudyHubIntent, StudyHubEffect>(StudyHubState()) {

    override val tag = "StudyHubVM"

    override fun onIntent(intent: StudyHubIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is StudyHubIntent.TapStartQuiz -> createAndNavigate(SessionType.QUICK_QUIZ)
            is StudyHubIntent.TapBeginExam -> createAndNavigate(SessionType.EXAM_MODE)
            is StudyHubIntent.DismissError -> updateState { copy(errorMessage = null) }
        }
    }

    private fun createAndNavigate(type: SessionType) {
        if (state.value.isCreatingSession) return
        Logger.d(tag, "createAndNavigate: start — type=$type")
        updateState { copy(isCreatingSession = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val cards = studyRepository.getCardsForSession(null, 1).firstOrNull()
                if (cards.isNullOrEmpty()) {
                    Logger.d(tag, "createAndNavigate: no flashcards available")
                    updateState {
                        copy(
                            isCreatingSession = false,
                            errorMessage = "Create some notes first to generate quiz questions",
                        )
                    }
                    return@launch
                }

                val timeLimitSeconds = if (type == SessionType.EXAM_MODE) 45 * 60 else null
                val quizData = startQuizUseCase(
                    type = type,
                    questionCount = if (type == SessionType.EXAM_MODE) 50 else 10,
                    timeLimitSeconds = timeLimitSeconds,
                )
                Logger.d(tag, "createAndNavigate: success — sessionId=${quizData.session.id}")
                updateState { copy(isCreatingSession = false) }
                sendEffect(StudyHubEffect.NavigateToQuiz(quizData.session.id))
            } catch (e: Exception) {
                Logger.e(tag, "createAndNavigate: error", e)
                updateState { copy(isCreatingSession = false) }
            }
        }
    }
}
