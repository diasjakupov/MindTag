package io.diasjakupov.mindtag.feature.onboarding.presentation

import io.diasjakupov.mindtag.core.data.AppPreferences
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.onboarding.presentation.OnboardingContract.Effect
import io.diasjakupov.mindtag.feature.onboarding.presentation.OnboardingContract.Intent
import io.diasjakupov.mindtag.feature.onboarding.presentation.OnboardingContract.State

class OnboardingViewModel(
    private val appPreferences: AppPreferences,
) : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "OnboardingVM"

    override fun onIntent(intent: Intent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            Intent.NextPage -> {
                val current = state.value.currentPage
                if (current < state.value.totalPages - 1) {
                    updateState { copy(currentPage = current + 1) }
                }
            }
            Intent.PreviousPage -> {
                val current = state.value.currentPage
                if (current > 0) {
                    updateState { copy(currentPage = current - 1) }
                }
            }
            Intent.Skip, Intent.GetStarted -> {
                appPreferences.setOnboardingCompleted()
                sendEffect(Effect.NavigateToHome)
            }
        }
    }

    fun updatePageFromPager(page: Int) {
        updateState { copy(currentPage = page) }
    }
}
