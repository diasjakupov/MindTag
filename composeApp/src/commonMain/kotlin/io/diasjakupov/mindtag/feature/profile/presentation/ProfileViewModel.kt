package io.diasjakupov.mindtag.feature.profile.presentation

import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileContract.Effect
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileContract.Intent
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileContract.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

class ProfileViewModel(
    private val db: MindTagDatabase,
) : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "ProfileVM"

    init {
        loadStats()
    }

    private fun loadStats() {
        combine(
            db.noteEntityQueries.selectAll().asFlow().mapToList(Dispatchers.IO),
            db.studySessionEntityQueries.selectAll().asFlow().mapToList(Dispatchers.IO),
            db.userProgressEntityQueries.selectAll().asFlow().mapToList(Dispatchers.IO),
        ) { notes, sessions, progressList ->
            val maxStreak = progressList.maxOfOrNull { it.current_streak } ?: 0L
            val totalXp = progressList.sumOf { it.total_xp }

            updateState {
                copy(
                    totalNotes = notes.size,
                    totalStudySessions = sessions.size,
                    currentStreak = maxStreak.toInt(),
                    totalXp = totalXp.toInt(),
                    isLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        Logger.d(tag, "onIntent: $intent")
    }
}
