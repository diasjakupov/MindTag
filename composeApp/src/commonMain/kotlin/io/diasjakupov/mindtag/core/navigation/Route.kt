package io.diasjakupov.mindtag.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable data object Library : Route
    @Serializable data object Study : Route
    @Serializable data class NoteCreate(val noteId: Long? = null) : Route
    @Serializable data class NoteDetail(val noteId: Long) : Route
    @Serializable data class Quiz(val sessionId: String) : Route
    @Serializable data class QuizResults(val sessionId: String) : Route
    @Serializable data object Auth : Route
}
