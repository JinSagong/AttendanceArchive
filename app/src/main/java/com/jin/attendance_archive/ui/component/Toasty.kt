package com.jin.attendance_archive.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.res.Colors
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.ComposeState
import com.jin.attendance_archive.util.compose.clickableWithoutRipple
import kotlinx.coroutines.*

object Toasty {
    private val toastyScreen = ComposeState<String?>(null)

    private var coroutineScope: CoroutineScope? = null
    private var onFinishJob: Job? = null

    private val onFinish = {
        onFinishJob = coroutineScope?.launch {
            if (!toastyScreen.value.isNullOrEmpty()) delay(1000L)
            close()
        }
    }

    fun show(msg: String) {
        onFinishJob?.cancel()
        onFinishJob = null
        if (msg.isEmpty()) close()
        else {
            toastyScreen.value = msg
            onFinish.invoke()
        }
    }

    fun cancel() {
        onFinishJob?.cancel()
        onFinishJob = null
        close()
    }

    private fun close() {
        toastyScreen.value = null
    }

    @Composable
    fun ToastyScreen(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope

        AnimatedVisibility(
            visible = !toastyScreen.value.isNullOrEmpty(),
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically { it / 4 } + fadeIn(),
            exit = slideOutVertically { -it / 4 } + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .clickableWithoutRipple {
                            if (!SingleClickManager.isAvailable()) return@clickableWithoutRipple
                            cancel()
                        },
                    color = Colors.transparent
                ) {}
                Component.CardMsg(
                    modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).wrapContentHeight()
                        .padding(16f.dp)
                        .align(Alignment.Center)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(16f.dp),
                        text = toastyScreen.value.orEmpty(),
                        textAlign = TextAlign.Center,
                        fontSize = 14f.sp
                    )
                }
            }
        }
    }
}