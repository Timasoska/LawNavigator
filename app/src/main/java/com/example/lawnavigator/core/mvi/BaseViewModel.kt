package com.example.lawnavigator.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : ViewState, Intent : ViewIntent, Effect : ViewSideEffect> : ViewModel() {

    abstract fun createInitialState(): State

    private val _state: MutableStateFlow<State> by lazy { MutableStateFlow(createInitialState()) }
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<Intent>()

    private val _effect = Channel<Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            _event.collect { handleEvent(it) }
        }
    }

    fun setEvent(event: Intent) {
        viewModelScope.launch { _event.emit(event) }
    }

    protected fun setState(reduce: State.() -> State) {
        val newState = currentState.reduce()
        _state.value = newState
    }

    protected fun setEffect(builder: () -> Effect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    val currentState: State get() = state.value

    abstract fun handleEvent(event: Intent)
}