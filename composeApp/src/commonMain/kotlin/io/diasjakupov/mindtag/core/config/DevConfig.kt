package io.diasjakupov.mindtag.core.config

object DevConfig {
    /**
     * Set to true to auto-populate the database with mock data on first launch.
     * MUST be false for production builds.
     */
    const val ENABLE_SEED_DATA: Boolean = true
}
