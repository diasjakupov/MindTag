package io.diasjakupov.mindtag.feature.onboarding.presentation

object OnboardingContract {
    data class State(
        val currentPage: Int = 0,
        val totalPages: Int = 4,
    )

    sealed interface Intent {
        data object NextPage : Intent
        data object PreviousPage : Intent
        data object Skip : Intent
        data object GetStarted : Intent
    }

    sealed interface Effect {
        data object NavigateToHome : Effect
    }
}
