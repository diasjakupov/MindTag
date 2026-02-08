package io.diasjakupov.mindtag.feature.profile.presentation

import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileContract.Effect
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileContract.Intent
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileContract.State

class ProfileViewModel : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "ProfileVM"

    override fun onIntent(intent: Intent) {
        Logger.d(tag, "onIntent: $intent")
        // No-ops for now — placeholders for future implementation
    }
}
