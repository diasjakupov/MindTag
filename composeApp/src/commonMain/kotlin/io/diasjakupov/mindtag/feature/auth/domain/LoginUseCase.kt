package io.diasjakupov.mindtag.feature.auth.domain

import io.diasjakupov.mindtag.core.network.ApiResult

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): ApiResult<Unit> {
        if (email.isBlank()) return ApiResult.Error("Email cannot be empty")
        if (password.isBlank()) return ApiResult.Error("Password cannot be empty")
        return repository.login(email.trim(), password)
    }
}
