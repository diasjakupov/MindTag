package io.diasjakupov.mindtag.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val token: String, val userId: Long) : AuthState
}

class AuthManager(private val tokenStorage: TokenStorage) {
    private val _state = MutableStateFlow<AuthState>(restoreState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    val isAuthenticated: Boolean
        get() = _state.value is AuthState.Authenticated

    val token: String?
        get() = (_state.value as? AuthState.Authenticated)?.token

    val userId: Long?
        get() = (_state.value as? AuthState.Authenticated)?.userId

    fun login(token: String, userId: Long) {
        tokenStorage.saveToken(token, userId)
        _state.value = AuthState.Authenticated(token, userId)
    }

    fun logout() {
        tokenStorage.clear()
        _state.value = AuthState.Unauthenticated
    }

    private fun restoreState(): AuthState {
        val token = tokenStorage.getToken()
        val userId = tokenStorage.getUserId()
        return if (token != null && userId != null) {
            AuthState.Authenticated(token, userId)
        } else {
            AuthState.Unauthenticated
        }
    }
}
