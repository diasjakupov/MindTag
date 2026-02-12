package io.diasjakupov.mindtag.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.feature.auth.domain.LoginUseCase
import io.diasjakupov.mindtag.feature.auth.domain.RegisterUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
) : MviViewModel<AuthState, AuthIntent, AuthEffect>(AuthState()) {

    override val tag = "AuthVM"

    override fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.UpdateEmail -> updateState { copy(email = intent.email, error = null) }
            is AuthIntent.UpdatePassword -> updateState { copy(password = intent.password, error = null) }
            is AuthIntent.ToggleMode -> updateState { copy(isLoginMode = !isLoginMode, error = null) }
            is AuthIntent.DismissError -> updateState { copy(error = null) }
            is AuthIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val currentState = state.value
        if (currentState.isLoading) return

        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = if (currentState.isLoginMode) {
                loginUseCase(currentState.email, currentState.password)
            } else {
                registerUseCase(currentState.email, currentState.password)
            }

            when (result) {
                is ApiResult.Success -> {
                    Logger.d(tag, "Auth success")
                    sendEffect(AuthEffect.NavigateToHome)
                }
                is ApiResult.Error -> {
                    Logger.d(tag, "Auth error: ${result.message}")
                    updateState { copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}
