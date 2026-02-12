package io.diasjakupov.mindtag.feature.auth.presentation

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AuthIntent {
    data class UpdateEmail(val email: String) : AuthIntent
    data class UpdatePassword(val password: String) : AuthIntent
    data object ToggleMode : AuthIntent
    data object Submit : AuthIntent
    data object DismissError : AuthIntent
}

sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
}
