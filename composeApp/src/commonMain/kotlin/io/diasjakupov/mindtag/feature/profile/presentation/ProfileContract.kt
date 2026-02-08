package io.diasjakupov.mindtag.feature.profile.presentation

object ProfileContract {
    data class State(
        val userName: String = "Alex Johnson",
        val email: String = "alex.johnson@university.edu",
        val totalNotes: Int = 15,
        val totalStudySessions: Int = 8,
        val currentStreak: Int = 7,
        val totalXp: Int = 4030,
        val memberSince: String = "January 2026",
    )

    sealed interface Intent {
        data object TapEditProfile : Intent
        data object TapNotifications : Intent
        data object TapAppearance : Intent
        data object TapAbout : Intent
        data object TapLogout : Intent
    }

    sealed interface Effect
}
