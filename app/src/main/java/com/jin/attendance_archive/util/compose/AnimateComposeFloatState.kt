package com.jin.attendance_archive.util.compose

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.*

class AnimateComposeFloatState(private val initValue: Float) {
    private var _mutableState = mutableStateOf(initValue)
    private var _transitionState: State<Float>? = null
    var value = initValue
        get() = _transitionState?.value ?: initValue
        set(value) {
            _mutableState.value = value
            field = value
        }

    @Composable
    fun remember() {
        _mutableState = remember { _mutableState }
        _transitionState = updateTransition(_mutableState).animateFloat { it.value }
    }
}