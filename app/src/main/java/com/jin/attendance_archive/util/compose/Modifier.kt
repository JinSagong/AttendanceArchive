package com.jin.attendance_archive.util.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.addFocusCleaner(focusManager: FocusManager, doOnClear: (() -> Unit)? = null) =
    this.pointerInput(Unit) {
        detectTapGestures {
            doOnClear?.invoke()
            focusManager.clearFocus()
        }
    }


inline fun Modifier.clickableWithoutRipple(crossinline onClick: () -> Unit) = composed {
    clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
        onClick.invoke()
    }
}