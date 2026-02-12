package io.diasjakupov.mindtag.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    val userId: Long,
    val accessToken: String,
    val tokenType: String,
)
