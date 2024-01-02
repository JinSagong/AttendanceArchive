package com.jin.attendance_archive.ui.screen.management.filing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.util.AttendanceTypeUtil
import com.jin.attendance_archive.platformMode
import com.jin.attendance_archive.statemodel.FilingStateModel
import com.jin.attendance_archive.ui.component.BallPulseSyncProgressIndicator
import com.jin.attendance_archive.ui.component.CheckBoxRow
import com.jin.attendance_archive.ui.component.ListPicker
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@Composable
fun FilingYearlyScreen(filingStateModel: FilingStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    if (ScreenManager.filingYearlyScreen.value == Pair(true, true)) {
        coroutineScope.launch { scrollState.scrollTo(0) }
        filingStateModel.initPosition()
    }
    SideEffect {
        if (ScreenManager.filingYearlyScreen.value == Pair(false, false)) {
            filingStateModel.cancelGenerateFile()
        }
    }

    AnimatedVisibility(
        visible = ScreenManager.filingYearlyScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.filingYearlyScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.filingYearlyScreen.value.second) -it else it })
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
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = null,
                    modifier = Modifier.wrapContentSize()
                        .clickable {
                            if (!SingleClickManager.isAvailable()) return@clickable
                            ScreenManager.onBackPressed()
                        }
                        .padding(8f.dp)
                )
                Text(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        .padding(8f.dp),
                    text = Strings.managementFilingYearlyTitle,
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).fillMaxHeight()
                        .verticalScroll(scrollState)
                        .padding(vertical = 8f.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(16f.dp),
                        text = Strings.filingYearGuide,
                        fontSize = 14f.sp
                    )
                    Row(
                        modifier = Modifier.wrapContentSize()
                            .padding(16f.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ListPicker(
                            value = filingStateModel.yearPosition.value,
                            label = { filingStateModel.yearArray.getOrNull(it).orEmpty() },
                            onValueChange = { filingStateModel.yearPosition.value = it },
                            list = filingStateModel.yearArray.indices.toList(),
                            lastIdxForInvalidValue = true
                        )
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(16f.dp),
                        text = Strings.filingSelectionGuide,
                        fontSize = 14f.sp
                    )
                    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        CheckBoxRow(
                            modifier = Modifier.wrapContentSize(),
                            checked = filingStateModel.option.value == 0,
                            text = Strings.filingSun,
                            onClick = { filingStateModel.option.value = 0 },
                        )
                        AttendanceTypeUtil.listAttendanceType.value.filter { !it.hasFruit }
                            .forEachIndexed { idx, attendanceType ->
                                CheckBoxRow(
                                    modifier = Modifier.wrapContentSize(),
                                    checked = filingStateModel.option.value == idx + 1,
                                    text = attendanceType.name,
                                    onClick = { filingStateModel.option.value = idx + 1 },
                                )
                            }
                    }
                    Button(modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        .padding(16f.dp),
                        onClick = {
                            if (!SingleClickManager.isAvailable()) return@Button
                            filingStateModel.generateYearlyFile(coroutineScope)
                        }) {
                        Text(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(0f.dp, 4f.dp),
                            text = Strings.filingGenerateFile,
                            textAlign = TextAlign.Center,
                            fontSize = 14f.sp
                        )
                    }
                    AnimatedVisibility(
                        visible = filingStateModel.file.value != null && platformMode == ScreenManager.MODE_ANDROID,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(modifier = Modifier.fillMaxHeight().wrapContentHeight()) {
                            Button(modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(16f.dp),
                                onClick = {
                                    if (!SingleClickManager.isAvailable()) return@Button
                                    filingStateModel.openFile()
                                }) {
                                Text(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                        .padding(0f.dp, 4f.dp),
                                    text = Strings.filingChooserTitleToOpen,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14f.sp
                                )
                            }
                            Button(modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(16f.dp),
                                onClick = {
                                    if (!SingleClickManager.isAvailable()) return@Button
                                    filingStateModel.shareFileViaKakaoTalk()
                                }) {
                                Text(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                        .padding(0f.dp, 4f.dp),
                                    text = Strings.filingShareViaKakaoTalk,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14f.sp
                                )
                            }
                            Button(modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(16f.dp),
                                onClick = {
                                    if (!SingleClickManager.isAvailable()) return@Button
                                    filingStateModel.shareFileViaEmail()
                                }) {
                                Text(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                        .padding(0f.dp, 4f.dp),
                                    text = Strings.filingShareViaEmail,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14f.sp
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = filingStateModel.file.value != null && platformMode == ScreenManager.MODE_DESKTOP,
                        modifier = Modifier.fillMaxSize()
                    ) {

                        Button(modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(16f.dp),
                            onClick = {
                                if (!SingleClickManager.isAvailable()) return@Button
                                filingStateModel.downloadFile()
                            }) {
                            Text(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                    .padding(0f.dp, 4f.dp),
                                text = Strings.filingDownload,
                                textAlign = TextAlign.Center,
                                fontSize = 14f.sp
                            )
                        }
                    }
                }
                this@Column.AnimatedVisibility(
                    visible = filingStateModel.fileGeneratorJob.value != null,
                    modifier = Modifier.fillMaxSize()
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