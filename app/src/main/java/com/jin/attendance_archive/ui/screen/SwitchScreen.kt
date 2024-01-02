package com.jin.attendance_archive.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.util.AttendanceTypeUtil
import com.jin.attendance_archive.statemodel.SwitchStateModel
import com.jin.attendance_archive.ui.component.CheckBoxRow
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.res.Colors
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.clickableWithoutRipple
import kotlinx.coroutines.launch

@Composable
fun SwitchScreen(switchStateModel: SwitchStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    if (ScreenManager.switchScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollTo(0)
    }

    AnimatedVisibility(
        visible = ScreenManager.switchScreen.value.first,
        modifier = Modifier.fillMaxSize(),
        enter = slideInVertically { it / 4 } + fadeIn(),
        exit = slideOutVertically { -it / 4 } + fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize()
                    .clickableWithoutRipple {
                        if (!SingleClickManager.isAvailable()) return@clickableWithoutRipple
                        switchStateModel.confirmSwitch()
                    },
                color = Colors.transparent
            ) {}
            Component.CardMsg(
                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).wrapContentHeight()
                    .padding(vertical = 32f.dp, horizontal = 16f.dp)
                    .align(Alignment.Center)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(
                                16f.dp,
                                if (!switchStateModel.switchConfirmedState.value) 32f.dp else 16f.dp,
                                16f.dp,
                                16f.dp
                            ),
                        text = if (!switchStateModel.switchConfirmedState.value) Strings.switchTitle
                        else Strings.switchCompleted,
                        textAlign = TextAlign.Center,
                        fontSize = if (!switchStateModel.switchConfirmedState.value) 16f.sp else 14f.sp,
                        fontWeight = if (!switchStateModel.switchConfirmedState.value) FontWeight.Bold else FontWeight.Normal
                    )
                    AnimatedVisibility(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        visible = !switchStateModel.switchConfirmedState.value
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                            Column(
                                modifier = Modifier.wrapContentSize()
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                AttendanceTypeUtil.listAttendanceType.value.forEach { data ->
                                    CheckBoxRow(
                                        checked = data.on,
                                        text = data.name,
                                        modifier = Modifier.wrapContentSize(),
                                        onClick = { switchStateModel.setSwitch(data.id, !data.on) }
                                    )
                                }
                            }
                            Text(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                    .clickable {
                                        if (!SingleClickManager.isAvailable()) return@clickable
                                        switchStateModel.confirmSwitch()
                                    }
                                    .padding(16f.dp),
                                text = Strings.switchConfirm,
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