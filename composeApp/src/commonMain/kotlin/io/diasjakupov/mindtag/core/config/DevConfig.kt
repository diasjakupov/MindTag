package io.diasjakupov.mindtag.core.config

object DevConfig {
    /**
     * Set to true to auto-populate the database with mock data on first launch.
     * MUST be false for production builds.
     */
    const val ENABLE_SEED_DATA: Boolean = true

    /**
     * Default environment mode used on every cold start.
     *
     * Change to [AppEnvironment.TEST] to run the app without a backend by default.
     * The mode can also be toggled at runtime via the in-app environment banner
     * (long-press the red banner at the bottom of the screen).
     *
     * See [EnvironmentStore] and [AppEnvironment] for details.
     */
    val DEFAULT_ENVIRONMENT: AppEnvironment = AppEnvironment.TEST
}
