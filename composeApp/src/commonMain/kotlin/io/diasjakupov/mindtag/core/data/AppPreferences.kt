package io.diasjakupov.mindtag.core.data

import io.diasjakupov.mindtag.data.local.MindTagDatabase

class AppPreferences(private val db: MindTagDatabase) {
    fun isOnboardingCompleted(): Boolean {
        return db.appSettingsEntityQueries.selectByKey("onboarding_completed")
            .executeAsOneOrNull() == "true"
    }

    fun setOnboardingCompleted() {
        db.appSettingsEntityQueries.upsert("onboarding_completed", "true")
    }
}
