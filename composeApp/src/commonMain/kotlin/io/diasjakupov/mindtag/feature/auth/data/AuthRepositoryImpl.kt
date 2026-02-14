package io.diasjakupov.mindtag.feature.auth.data

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.dto.AuthResponseDto
import io.diasjakupov.mindtag.feature.auth.domain.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val authManager: AuthManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<Unit> =
        authenticate(authApi.login(email, password))

    override suspend fun register(email: String, password: String): ApiResult<Unit> =
        authenticate(authApi.register(email, password))

    private fun authenticate(result: ApiResult<AuthResponseDto>): ApiResult<Unit> =
        when (result) {
            is ApiResult.Success -> {
                authManager.login(result.data.accessToken, result.data.userId)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> result
        }
}
