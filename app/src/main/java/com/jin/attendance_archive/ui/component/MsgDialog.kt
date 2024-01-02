package com.jin.attendance_archive.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.res.Colors
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.ComposeState
import com.jin.attendance_archive.util.compose.clickableWithoutRipple
import kotlinx.coroutines.*

class MsgDialog private constructor() {
    class Builder(private val hasCancelButton: Boolean) {
        private var message = ""
        private var onDoneListener: ((onFinish: (() -> Unit)) -> Unit)? = null
        private var onCancelListener: (() -> Unit)? = null
        private var finishMessage = ""
        fun setMessage(message: String) = apply { this.message = message }
        fun setFinishMessage(message: String) = apply { finishMessage = message }
        fun onConfirm(l: ((onFinish: (() -> Unit)) -> Unit)?) = apply { onDoneListener = l }
        fun onCancel(l: (() -> Unit)?) = apply { onCancelListener = l }
        fun show() {
            isOpened = true
            msgDialogFinish.value = false
            msgDialogScreen.value = DataMsgDialog(
                hasCancelButton, message, finishMessage, onDoneListener, onCancelListener
            )
        }
    }

    private data class DataMsgDialog(
        val hasCancelButton: Boolean,
        val message: String,
        val finishMessage: String,
        val onDoneListener: ((onFinish: (() -> Unit)) -> Unit)?,
        val onCancelListener: (() -> Unit)?
    )

    companion object {
        private val msgDialogScreen = ComposeState<DataMsgDialog?>(null)
        private val msgDialogFinish = ComposeState(false)

        private var coroutineScope: CoroutineScope? = null
        private var onFinishJob: Job? = null

        private val onFinish = {
            onFinishJob = coroutineScope?.launch {
                msgDialogFinish.value = true
                if (!msgDialogScreen.value?.finishMessage.isNullOrEmpty()) delay(1000L)
                close()
            }
        }

        var isOpened = false

        fun withOneBtn() = Builder(hasCancelButton = false)
        fun withTwoBtn() = Builder(hasCancelButton = true)

        fun cancel() {
            onFinishJob?.cancel()
            onFinishJob = null
            msgDialogScreen.value?.onCancelListener?.invoke()
            close()
        }

        fun close() {
            msgDialogScreen.value = null
            isOpened = false
        }

        @Composable
        fun MsgDialogScreen(coroutineScope: CoroutineScope) {
            this.coroutineScope = coroutineScope

            msgDialogScreen.remember()
            msgDialogFinish.remember()

            MsgDialogWithOneBtnScreen()
            MsgDialogWithTwoBtnScreen()
        }

        @Composable
        private fun MsgDialogWithOneBtnScreen() {
            AnimatedVisibility(
                visible = msgDialogScreen.value?.hasCancelButton == false,
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
                        Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                            Text(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                    .padding(
                                        horizontal = 16f.dp,
                                        vertical = if (!msgDialogFinish.value) 32f.dp else 16f.dp
                                    ),
                                text = if (!msgDialogFinish.value) msgDialogScreen.value?.message.orEmpty()
                                else msgDialogScreen.value?.finishMessage.orEmpty(),
                                textAlign = TextAlign.Center,
                                fontSize = 14f.sp
                            )
                            AnimatedVisibility(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                visible = !msgDialogFinish.value
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                        .clickable {
                                            if (!SingleClickManager.isAvailable()) return@clickable
                                            msgDialogScreen.value?.onDoneListener
                                                ?.invoke(onFinish) ?: close()
                                        }
                                        .padding(16f.dp),
                                    text = Strings.dialogConfirm,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14f.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        @Composable
        private fun MsgDialogWithTwoBtnScreen() {
            AnimatedVisibility(
                visible = msgDialogScreen.value?.hasCancelButton == true,
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
                        Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                            Text(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                    .padding(
                                        horizontal = 16f.dp,
                                        vertical = if (!msgDialogFinish.value) 32f.dp else 16f.dp
                                    ),
                                text = if (!msgDialogFinish.value) msgDialogScreen.value?.message.orEmpty()
                                else msgDialogScreen.value?.finishMessage.orEmpty(),
                                textAlign = TextAlign.Center,
                                fontSize = 14f.sp
                            )
                            AnimatedVisibility(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                visible = !msgDialogFinish.value
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                                    Text(
                                        modifier = Modifier.weight(1f).wrapContentHeight()
                                            .clickable {
                                                if (!SingleClickManager.isAvailable()) return@clickable
                                                cancel()
                                            }
                                            .padding(16.dp),
                                        text = Strings.dialogCancel,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14f.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        modifier = Modifier.weight(1f).wrapContentHeight()
                                            .clickable {
                                                if (!SingleClickManager.isAvailable()) return@clickable
                                                msgDialogScreen.value?.onDoneListener
                                                    ?.invoke(onFinish) ?: close()
                                            }
                                            .padding(16f.dp),
                                        text = Strings.dialogConfirm,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14f.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

