package io.diasjakupov.mindtag.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.diasjakupov.mindtag.core.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<S, I, E>(initialState: S) : ViewModel() {
    protected open val tag: String = "MindTag"

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<E>()
    val effect: SharedFlow<E> = _effect.asSharedFlow()

    protected fun updateState(reducer: S.() -> S) {
        _state.update(reducer)
        Logger.d(tag, "State updated: ${_state.value}")
    }

    protected fun sendEffect(effect: E) {
        Logger.d(tag, "Effect: $effect")
        viewModelScope.launch { _effect.emit(effect) }
    }

    abstract fun onIntent(intent: I)
}
