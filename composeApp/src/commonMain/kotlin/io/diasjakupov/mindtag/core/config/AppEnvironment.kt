package io.diasjakupov.mindtag.core.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Two-mode environment system for MindTag.
 *
 * - [NETWORK] — all features use the real backend API (default / production behaviour).
 * - [TEST]    — all features return hardcoded stub data; zero network requests are made.
 *
 * The active mode is stored in [EnvironmentStore], which is the single source of truth.
 * Switch with [EnvironmentStore.setMode]. The DI layer reads this at startup and re-wires
 * repositories when the mode changes.
 */
enum class AppEnvironment {
    NETWORK,
    TEST,
}

/**
 * Runtime holder for the current [AppEnvironment].
 *
 * This object is read by the Koin DI module to decide which repository implementations to bind.
 * Switching modes while the app is running changes the [mode] StateFlow, and any Composable
 * observing it (e.g. [io.diasjakupov.mindtag.core.config.EnvironmentBanner]) will recompose.
 *
 * NOTE: The selection is kept in-memory only. On next cold start the app returns to [DEFAULT].
 * Persistence to the SQLite settings table is intentionally omitted — test mode should not
 * survive restarts accidentally in a production build.
 */
object EnvironmentStore {

    /** The default mode used on every cold start. Reads from [DevConfig.DEFAULT_ENVIRONMENT].
     *  Change that constant to [AppEnvironment.TEST] for day-to-day development without a
     *  running backend. */
    val DEFAULT: AppEnvironment = DevConfig.DEFAULT_ENVIRONMENT

    private val _mode = MutableStateFlow(DEFAULT)

    /** Observe the current environment mode reactively. */
    val mode: StateFlow<AppEnvironment> = _mode.asStateFlow()

    /** The current environment mode (non-reactive snapshot). */
    val current: AppEnvironment get() = _mode.value

    /** Switch to [newMode] immediately. Downstream repositories will use the new mode
     *  for the next call — no restart required. */
    fun setMode(newMode: AppEnvironment) {
        _mode.value = newMode
    }

    val isTestMode: Boolean get() = current == AppEnvironment.TEST
}
