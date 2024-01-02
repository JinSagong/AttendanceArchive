package com.jin.attendance_archive.ui.screen.management

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import com.jin.attendance_archive.statemodel.PeopleStateModel
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.Accessibility
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@Composable
fun ManagementScreen(peopleStateModel: PeopleStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    if (ScreenManager.managementScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollTo(0)
    }

    AnimatedVisibility(
        visible = ScreenManager.managementScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.managementScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.managementScreen.value.second) -it else it })
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
                    text = Strings.managementScreen,
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(vertical = 8f.dp),
            ) {
                if (Accessibility.filingAttendance) Component.Card(
                    modifier = Modifier.fillMaxWidth().height(180f.dp)
                        .padding(vertical = 8f.dp, horizontal = 16f.dp),
                    onClick = {
                        if (!SingleClickManager.isAvailable()) return@Card
                        ScreenManager.openFilingWeeklyScreen()
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        Text(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(16f.dp),
                            text = Strings.managementFilingWeeklyTitle,
                            fontSize = 18f.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.wrapContentSize()
                                .padding(
                                    start = 16f.dp, top = 0f.dp, end = 16f.dp, bottom = 16f.dp
                                ),
                            text = Strings.managementFilingWeeklyGuide,
                            fontSize = 14f.sp
                        )
                    }
                }

                if (Accessibility.filingAttendance) Component.Card(
                    modifier = Modifier.fillMaxWidth().height(180f.dp)
                        .padding(vertical = 8f.dp, horizontal = 16f.dp),
                    onClick = {
                        if (!SingleClickManager.isAvailable()) return@Card
                        ScreenManager.openFilingYearlyScreen()
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        Text(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(16f.dp),
                            text = Strings.managementFilingYearlyTitle,
                            fontSize = 18f.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.wrapContentSize()
                                .padding(
                                    start = 16f.dp, top = 0f.dp, end = 16f.dp, bottom = 16f.dp
                                ),
                            text = Strings.managementFilingYearlyGuide,
                            fontSize = 14f.sp
                        )
                    }
                }

                if (Accessibility.filingFruit) Component.Card(
                    modifier = Modifier.fillMaxWidth().height(180f.dp)
                        .padding(vertical = 8f.dp, horizontal = 16f.dp),
                    onClick = {
                        if (!SingleClickManager.isAvailable()) return@Card
                        ScreenManager.openFilingFruitScreen()
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        Text(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(16f.dp),
                            text = Strings.managementFilingFruitTitle,
                            fontSize = 18f.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.wrapContentSize()
                                .padding(
                                    start = 16f.dp, top = 0f.dp, end = 16f.dp, bottom = 16f.dp
                                ),
                            text = Strings.managementFilingFruitGuide,
                            fontSize = 14f.sp
                        )
                    }
                }

                if (Accessibility.managePeople) Component.Card(
                    modifier = Modifier.fillMaxWidth().height(180f.dp)
                        .padding(vertical = 8f.dp, horizontal = 16f.dp),
                    onClick = {
                        if (!SingleClickManager.isAvailable()) return@Card
                        peopleStateModel.initSearchQuery()
                        ScreenManager.openManagePeopleScreen()
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        Text(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(16f.dp),
                            text = Strings.managementPeopleTitle,
                            fontSize = 18f.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.wrapContentSize()
                                .padding(
                                    start = 16f.dp, top = 0f.dp, end = 16f.dp, bottom = 16f.dp
                                ),
                            text = Strings.managementPeopleGuide,
                            fontSize = 14f.sp
                        )
                    }
                }

                if (Accessibility.manageUser) Component.Card(
                    modifier = Modifier.fillMaxWidth().height(180f.dp)
                        .padding(vertical = 8f.dp, horizontal = 16f.dp),
                    onClick = {
                        if (!SingleClickManager.isAvailable()) return@Card
                        ScreenManager.openManageUserScreen()
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        Text(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(16f.dp),
                            text = Strings.managementUserTitle,
                            fontSize = 18f.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.wrapContentSize()
                                .padding(
                                    start = 16f.dp, top = 0f.dp, end = 16f.dp, bottom = 16f.dp
                                ),
                            text = Strings.managementUserGuide,
                            fontSize = 14f.sp
                        )
                    }
                }

                if (Accessibility.log) Component.Card(
                    modifier = Modifier.fillMaxWidth().height(180f.dp)
                        .padding(vertical = 8f.dp, horizontal = 16f.dp),
                    onClick = {
                        if (!SingleClickManager.isAvailable()) return@Card
                        ScreenManager.openLogScreen()
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        Text(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(16f.dp),
                            text = Strings.managementLogTitle,
                            fontSize = 18f.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.wrapContentSize()
                                .padding(
                                    start = 16f.dp, top = 0f.dp, end = 16f.dp, bottom = 16f.dp
                                ),
                            text = Strings.managementLogGuide,
                            fontSize = 14f.sp
                        )
                    }
                }
            }
        }
    }
}