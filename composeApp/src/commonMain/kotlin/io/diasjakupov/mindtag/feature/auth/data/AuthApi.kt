package io.diasjakupov.mindtag.feature.auth.data

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.dto.AuthResponseDto
import io.diasjakupov.mindtag.core.network.dto.LoginRequest
import io.diasjakupov.mindtag.core.network.dto.RegisterRequest
import io.diasjakupov.mindtag.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApi(
    private val client: HttpClient,
    private val authManager: AuthManager,
) {
    suspend fun login(email: String, password: String): ApiResult<AuthResponseDto> =
        safeApiCall(authManager) {
            client.post("/auth/login") {
                setBody(LoginRequest(email, password))
            }
        }

    suspend fun register(email: String, password: String): ApiResult<AuthResponseDto> =
        safeApiCall(authManager) {
            client.post("/auth/register") {
                setBody(RegisterRequest(email, password))
            }
        }
}
