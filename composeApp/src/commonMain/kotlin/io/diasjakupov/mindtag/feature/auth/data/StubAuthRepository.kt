package io.diasjakupov.mindtag.feature.auth.data

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.feature.auth.domain.AuthRepository

/**
 * Stub [AuthRepository] used in [io.diasjakupov.mindtag.core.config.AppEnvironment.TEST] mode.
 *
 * Both login and register succeed instantly with a hardcoded fake token and userId so that
 * the full authenticated screen flow can be exercised without a running backend.
 *
 * No network calls are made.
 */
class StubAuthRepository(
    private val authManager: AuthManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<Unit> =
        authenticate()

    override suspend fun register(email: String, password: String): ApiResult<Unit> =
        authenticate()

    private fun authenticate(): ApiResult<Unit> {
        authManager.login(
            token = "stub-jwt-token-for-test-mode",
            userId = 1L,
        )
        return ApiResult.Success(Unit)
    }
}
