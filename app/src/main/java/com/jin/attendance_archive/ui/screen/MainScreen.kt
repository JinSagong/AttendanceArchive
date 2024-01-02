package com.jin.attendance_archive.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.util.AttendanceTypeUtil
import com.jin.attendance_archive.model.util.AttendanceUtil
import com.jin.attendance_archive.model.util.OrganizationUtil
import com.jin.attendance_archive.model.util.UserUtil
import com.jin.attendance_archive.statemodel.CheckListStateModel
import com.jin.attendance_archive.statemodel.SignStateModel
import com.jin.attendance_archive.statemodel.SwitchStateModel
import com.jin.attendance_archive.ui.component.BallPulseSyncProgressIndicator
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.ui.component.MsgDialog
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.LocalMyColorScheme
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.Accessibility
import com.jin.attendance_archive.util.DateTimeUtil
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    signStateModel: SignStateModel,
    switchStateModel: SwitchStateModel,
    checkListStateModel: CheckListStateModel
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    if (ScreenManager.mainScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollToItem(0)
    }

    AnimatedVisibility(
        visible = ScreenManager.mainScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.mainScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.mainScreen.value.second) -it else it })
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    .padding(8f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f).wrapContentHeight()
                        .padding(8f.dp),
                    text = UserUtil.dataUser.value?.name.orEmpty(),
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (Accessibility.management) Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    modifier = Modifier.wrapContentSize()
                        .clickable {
                            if (!SingleClickManager.isAvailable()) return@clickable
                            ScreenManager.openManagementScreen()
                        }
                        .padding(8f.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.wrapContentSize()
                        .clickable {
                            if (!SingleClickManager.isAvailable()) return@clickable
                            MsgDialog.withTwoBtn()
                                .setMessage(Strings.signOutMessage)
                                .setFinishMessage(Strings.signOutCompleted)
                                .onConfirm {
                                    it.invoke()
                                    signStateModel.signOut()
                                }
                                .show()
                        }
                        .padding(8f.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    .padding(horizontal = 8f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.wrapContentSize()
                        .padding(8f.dp),
                    shape = RoundedCornerShape(12f.dp),
                    contentPadding = PaddingValues(8f.dp),
                    onClick = {
                        if (!SingleClickManager.isAvailable()) return@Button
                        switchStateModel.openSwitch()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = null
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        .padding(8f.dp)
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = DateTimeUtil.getWeek(),
                        fontSize = 14f.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = DateTimeUtil.getPeriod(),
                        fontSize = 12f.sp
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).fillMaxHeight()
                        .padding(vertical = 8f.dp),
                    state = scrollState
                ) {
                    items(
                        AttendanceTypeUtil.listAttendanceType.value.filter { it.on },
                        key = { it.id }
                    ) { data ->
                        Component.Card(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(vertical = 8f.dp, horizontal = 16f.dp)
                                .animateItemPlacement(),
                            onClick = { checkListStateModel.selectAttendanceType(data) }
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    AnimatedVisibility(
                                        visible = (AttendanceUtil.countAttendance[data.id]?.value
                                            ?: 0f) > 0f,
                                        modifier = Modifier
                                            .weight(
                                                (AttendanceUtil.countAttendance[data.id]?.value
                                                    ?: 0f).let { if (it <= 0f) 0.01f else it })
                                            .fillMaxHeight(),
                                    ) {
                                        Surface(
                                            modifier = Modifier.fillMaxSize(),
                                            color = LocalMyColorScheme.current.progress
                                        ) {}
                                    }
                                    AnimatedVisibility(
                                        visible = OrganizationUtil.getOrganizationCount(data.hasFruit)
                                            .let { count ->
                                                (AttendanceUtil.countAttendance[data.id]?.value
                                                    ?: 0f) < count || count == 0
                                            },
                                        modifier = Modifier
                                            .weight(
                                                (AttendanceUtil.countAttendance[data.id]?.value
                                                    ?: 0f).let {
                                                    val totalCount =
                                                        OrganizationUtil.getOrganizationCount(data.hasFruit)
                                                    if (it >= totalCount) 0.01f else totalCount - it
                                                })
                                            .wrapContentHeight()
                                    ) {
                                        Spacer(modifier = Modifier.fillMaxSize())
                                    }
                                }
                                Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                            .padding(16f.dp),
                                        text = data.name,
                                        fontSize = 18f.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        modifier = Modifier.wrapContentSize()
                                            .align(Alignment.CenterHorizontally)
                                            .padding(start = 16f.dp, top = 8f.dp, end = 16f.dp),
                                        text = Strings.attendanceProgress,
                                        fontSize = 12f.sp
                                    )
                                    Row(
                                        modifier = Modifier.wrapContentSize()
                                            .align(Alignment.CenterHorizontally)
                                            .padding(start = 16f.dp, end = 16f.dp, bottom = 16f.dp)
                                    ) {
                                        Text(
                                            modifier = Modifier.wrapContentSize()
                                                .align(Alignment.Bottom),
                                            text = (AttendanceUtil.countAttendance[data.id]?.value
                                                ?: 0f).toInt().toString(),
                                            fontSize = 28f.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            modifier = Modifier.wrapContentSize()
                                                .align(Alignment.Bottom)
                                                .padding(horizontal = 4f.dp),
                                            text = "/",
                                            fontSize = 18f.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            modifier = Modifier.wrapContentSize()
                                                .align(Alignment.Bottom),
                                            text = OrganizationUtil.getOrganizationCount(data.hasFruit)
                                                .toString(),
                                            fontSize = 18f.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                this@Column.AnimatedVisibility(
                    visible = !AttendanceUtil.loaded.value,
                    modifier = Modifier.fillMaxSize(),
                    enter = EnterTransition.None,
                    exit = ExitTransition.None
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { BallPulseSyncProgressIndicator() }
                }
            }
        }
    }
}