package io.diasjakupov.mindtag.feature.auth.data

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.feature.auth.domain.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val authManager: AuthManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<Unit> {
        return when (val result = authApi.login(email, password)) {
            is ApiResult.Success -> {
                authManager.login(result.data.accessToken, result.data.userId)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun register(email: String, password: String): ApiResult<Unit> {
        return when (val result = authApi.register(email, password)) {
            is ApiResult.Success -> {
                authManager.login(result.data.accessToken, result.data.userId)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> result
        }
    }
}
