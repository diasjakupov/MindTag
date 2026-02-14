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

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val subjects = studyRepository.getSubjects().firstOrNull() ?: emptyList()
                val dueCount = studyRepository.getDueCardCount()

                updateState {
                    copy(
                        subjects = subjects.map { SubjectUi(id = it.id, name = it.name) },
                        cardsDueCount = dueCount,
                    )
                }
            } catch (e: Exception) {
                Logger.e(tag, "loadData: error", e)
            }
        }
    }

    override fun onIntent(intent: StudyHubIntent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is StudyHubIntent.SelectSubject -> updateState { copy(selectedSubjectId = intent.subjectId) }
            is StudyHubIntent.SelectQuestionCount -> updateState { copy(questionCount = intent.count) }
            is StudyHubIntent.ToggleTimer -> updateState { copy(timerEnabled = intent.enabled) }
            is StudyHubIntent.SelectTimerDuration -> updateState { copy(timerMinutes = intent.minutes) }
            is StudyHubIntent.StartQuiz -> createAndNavigate()
            is StudyHubIntent.DismissError -> updateState { copy(errorMessage = null) }
        }
    }

    private fun createAndNavigate() {
        if (state.value.isCreatingSession) return
        updateState { copy(isCreatingSession = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val s = state.value
                val cards = studyRepository.getCardsForSession(s.selectedSubjectId, 1).firstOrNull()
                if (cards.isNullOrEmpty()) {
                    updateState {
                        copy(
                            isCreatingSession = false,
                            errorMessage = "No flashcards available. Create some notes first.",
                        )
                    }
                    return@launch
                }

                val timeLimitSeconds = if (s.timerEnabled) s.timerMinutes * 60 else null
                val quizData = startQuizUseCase(
                    type = SessionType.QUIZ,
                    subjectId = s.selectedSubjectId,
                    questionCount = s.questionCount,
                    timeLimitSeconds = timeLimitSeconds,
                )
                updateState { copy(isCreatingSession = false) }
                sendEffect(StudyHubEffect.NavigateToQuiz(quizData.session.id))
            } catch (e: Exception) {
                Logger.e(tag, "createAndNavigate: error", e)
                updateState { copy(isCreatingSession = false) }
            }
        }
    }
}
