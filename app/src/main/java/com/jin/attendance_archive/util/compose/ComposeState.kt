package com.jin.attendance_archive.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class ComposeState<T>(initValue: T) {
    private var _mutableState = mutableStateOf(initValue)
    var value = initValue
        get() = _mutableState.value
        set(value) {
            _mutableState.value = value
            field = value
        }

    @Composable
    fun remember() {
        _mutableState = remember { _mutableState }
    }
}