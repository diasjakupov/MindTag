package io.diasjakupov.mindtag.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable data object Home : Route
    @Serializable data object Library : Route
    @Serializable data object Practice : Route
    @Serializable data object Planner : Route
    @Serializable data object Profile : Route
    @Serializable data object NoteCreate : Route
    @Serializable data class NoteDetail(val noteId: String) : Route
    @Serializable data class Quiz(val sessionId: String) : Route
    @Serializable data class QuizResults(val sessionId: String) : Route
    @Serializable data object Onboarding : Route
}
