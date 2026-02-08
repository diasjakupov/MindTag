package io.diasjakupov.mindtag.feature.home.presentation

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.home.domain.usecase.GetDashboardUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    private val getDashboardUseCase: GetDashboardUseCase,
) : MviViewModel<HomeContract.State, HomeContract.Intent, HomeContract.Effect>(HomeContract.State()) {

    override val tag = "HomeVM"

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        Logger.d(tag, "loadDashboard: start")
        getDashboardUseCase()
            .onEach { data ->
                Logger.d(tag, "loadDashboard: success — notes=${data.totalNotesCount}, streak=${data.currentStreak}, reviewCards=${data.reviewCards.size}")
                updateState {
                    copy(
                        userName = data.userName,
                        totalNotesCount = data.totalNotesCount,
                        currentStreak = data.currentStreak,
                        reviewCards = data.reviewCards,
                        upNextTasks = data.upNextTasks,
                        isLoading = false,
                    )
                }
            }
            .catch { e ->
                Logger.e(tag, "loadDashboard: error", e)
                updateState { copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: HomeContract.Intent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is HomeContract.Intent.TapReviewCard -> {
                sendEffect(HomeContract.Effect.NavigateToNote(intent.noteId))
            }
            is HomeContract.Intent.TapTask -> {
                // Tasks can navigate to different places based on type
                // For now, we just navigate to note detail for review tasks
            }
            is HomeContract.Intent.Refresh -> {
                updateState { copy(isLoading = true) }
                loadDashboard()
            }
        }
    }
}
