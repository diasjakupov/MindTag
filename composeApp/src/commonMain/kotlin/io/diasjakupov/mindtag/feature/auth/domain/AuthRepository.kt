package io.diasjakupov.mindtag.feature.auth.domain

import io.diasjakupov.mindtag.core.network.ApiResult

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<Unit>
    suspend fun register(email: String, password: String): ApiResult<Unit>
}
