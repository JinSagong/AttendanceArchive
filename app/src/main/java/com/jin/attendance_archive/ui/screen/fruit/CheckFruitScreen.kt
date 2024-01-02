package com.jin.attendance_archive.ui.screen.fruit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.util.DutyUtil
import com.jin.attendance_archive.statemodel.CheckStateModel
import com.jin.attendance_archive.statemodel.PeopleStateModel
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.ui.component.MsgDialog
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.LocalMyColorScheme
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckFruitScreen(checkStateModel: CheckStateModel, peopleStateModel: PeopleStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(0) { 2 }
    val scrollState = listOf(rememberLazyListState(), rememberLazyListState())
    if (ScreenManager.checkFruitScreen.value == Pair(true, true)) coroutineScope.launch {
        pagerState.scrollToPage(0)
        scrollState[0].scrollToItem(0)
        scrollState[1].scrollToItem(0)
    }

    AnimatedVisibility(
        visible = ScreenManager.checkFruitScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.checkFruitScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.checkFruitScreen.value.second) -it else it })
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    modifier = Modifier.weight(1f).wrapContentHeight()
                        .padding(8f.dp),
                    text = checkStateModel.listOrganization.firstOrNull()?.category2.orEmpty(),
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.wrapContentSize()
                        .clickable {
                            if (!SingleClickManager.isAvailable()) return@clickable
                            checkStateModel.check { ScreenManager.onBackPressed() }
                        }
                        .padding(8f.dp)
                )
            }

            ScrollableTabRow(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                selectedTabIndex = pagerState.currentPage.let { if (it >= pagerState.pageCount) (pagerState.pageCount - 1) else it },
                edgePadding = 0f.dp,
                divider = {}
            ) {
                Tab(
                    text = {
                        Text(
                            modifier = Modifier.wrapContentSize(),
                            text = Strings.hasFruitAttendance,
                            fontSize = 14f.sp
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                            scrollState[0].animateScrollToItem(0)
                        }
                    },
                    selected = pagerState.currentPage == 0
                )
                Tab(
                    text = {
                        Text(
                            modifier = Modifier.wrapContentSize(),
                            text = Strings.hasFruitFruit,
                            fontSize = 14f.sp
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                            scrollState[1].animateScrollToItem(0)
                        }
                    },
                    selected = pagerState.currentPage == 1
                )
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .padding(vertical = 12f.dp),
                    state = scrollState[page],
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (page == 0) {
                        item {
                            Row(
                                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth)
                                    .wrapContentHeight()
                                    .padding(horizontal = 12f.dp)
                            ) {
                                Component.Card(
                                    modifier = Modifier.weight(1f).wrapContentHeight()
                                        .padding(4f.dp),
                                    onClick = {
                                        if (!SingleClickManager.isAvailable()) return@Card
                                        peopleStateModel.initSearchQuery()
                                        checkStateModel.openSearchPeople(1)
                                    }
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxSize()
                                            .padding(16f.dp),
                                        text = Strings.hasFruitAddAttendance,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14f.sp
                                    )
                                }
                                Component.Card(
                                    modifier = Modifier.weight(1f).wrapContentHeight()
                                        .padding(4f.dp),
                                    onClick = {
                                        if (!SingleClickManager.isAvailable()) return@Card
                                        peopleStateModel.initSearchQuery()
                                        checkStateModel.openSearchPeople(2)
                                    }
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxSize()
                                            .padding(16f.dp),
                                        text = Strings.hasFruitAddDedication,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14f.sp
                                    )
                                }
                            }
                        }
                        items(
                            checkStateModel.mapFruitPeople.values
                                .sortedBy { item -> item.first.name }
                                .sortedBy { item -> item.second.checked },
                            key = { it.first.id }
                        ) { item ->
                            val people = item.first
                            val checked = item.second.checked
                            Component.Card(
                                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth)
                                    .wrapContentHeight()
                                    .padding(vertical = 4f.dp, horizontal = 16f.dp)
                                    .animateItemPlacement(),
                                onClick = {
                                    val duty = DutyUtil.mapDuty[people.duty]?.name.orEmpty()
                                    val dutyText = if (duty.isNotEmpty()) "[$duty]" else ""
                                    MsgDialog.Builder(hasCancelButton = true)
                                        .setMessage("$dutyText${people.name}: ${if (checked == 1) Strings.hasFruitAttendanceDeleteMsg else if (checked == 2) Strings.hasFruitDedicationDeleteMsg else ""}")
                                        .setFinishMessage(if (checked == 1) Strings.hasFruitAttendanceDeleteCompleted else if (checked == 2) Strings.hasFruitDedicationDeleteCompleted else "")
                                        .onConfirm {
                                            checkStateModel.removeFruitPeople(people.id)
                                            it.invoke()
                                        }
                                        .show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(16f.dp, 16f.dp, 8f.dp, 16f.dp),
                                        text = if (checked == 1) Strings.attendanceCheckType1 else if (checked == 2) Strings.attendanceCheckType2 else "",
                                        color = if (checked == 1) LocalMyColorScheme.current.green else LocalMyColorScheme.current.yellow,
                                        fontSize = 14f.sp
                                    )
                                    Text(
                                        modifier = Modifier.weight(1f).wrapContentHeight()
                                            .padding(8f.dp, 16f.dp, 8f.dp, 16f.dp),
                                        text = people.name,
                                        fontSize = 14f.sp
                                    )
                                    Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(8f.dp, 16f.dp, 16f.dp, 16f.dp),
                                        text = item.second.reason,
                                        fontSize = 14f.sp
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth)
                                    .wrapContentHeight()
                                    .padding(horizontal = 12f.dp)
                            ) {
                                Component.Card(
                                    modifier = Modifier.weight(1f).wrapContentHeight()
                                        .padding(4f.dp),
                                    onClick = {
                                        if (!SingleClickManager.isAvailable()) return@Card
                                        checkStateModel.openCreateFruit(0)
                                    }
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxSize()
                                            .padding(16f.dp),
                                        text = Strings.hasFruitAddFruit1,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14f.sp
                                    )
                                }
                                Component.Card(
                                    modifier = Modifier.weight(1f).wrapContentHeight()
                                        .padding(4f.dp),
                                    onClick = {
                                        if (!SingleClickManager.isAvailable()) return@Card
                                        checkStateModel.openCreateFruit(1)
                                    }
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxSize()
                                            .padding(16f.dp),
                                        text = Strings.hasFruitAddFruit2,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14f.sp
                                    )
                                }
                            }
                        }
                        items(
                            checkStateModel.mapFruit.values
                                .sortedBy { item -> item.believer }
                                .sortedBy { item -> item.type },
                            key = { it.id }
                        ) { fruit ->
                            Component.Card(
                                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth)
                                    .wrapContentHeight()
                                    .padding(vertical = 4f.dp, horizontal = 16f.dp)
                                    .animateItemPlacement(),
                                onClick = {
                                    checkStateModel.openEditFruit(fruit)
                                }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                        .padding(vertical = 12f.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.wrapContentSize()
                                                .padding(16f.dp, 4f.dp, 12f.dp, 4f.dp),
                                            text = if (fruit.type == 0) Strings.hasFruitBelieve else if (fruit.type == 1) Strings.hasFruitMeet else "",
                                            color = if (fruit. type == 0) LocalMyColorScheme.current.green else LocalMyColorScheme.current.yellow,
                                            fontSize = 14f.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            modifier = Modifier.wrapContentSize()
                                                .padding(4f.dp),
                                            text = fruit.believer,
                                            fontSize = 14f.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            modifier = Modifier.wrapContentSize()
                                                .padding(0f.dp, 4f.dp, 16f.dp, 4f.dp),
                                            text = if (fruit.remeet) "(${Strings.hasFruitRemeet})" else "",
                                            fontSize = 14f.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (fruit.people.isNotEmpty()) Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                                        text = "${Strings.hasFruitPreacher}: ${fruit.people}",
                                        fontSize = 14f.sp
                                    )
                                    if (fruit.teacher.isNotEmpty()) Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                                        text = "${Strings.hasFruitTeacher}: ${fruit.teacher}",
                                        fontSize = 14f.sp
                                    )
                                    if (fruit.age >= 0) Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                                        text = "${Strings.hasFruitAge}: ${fruit.age}",
                                        fontSize = 14f.sp
                                    )
                                    if (fruit.phone.isNotEmpty()) Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                                        text = "${Strings.hasFruitPhone}: ${fruit.phone}",
                                        fontSize = 14f.sp
                                    )
                                    if (fruit.frequency >= 0) Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                                        text = "${Strings.hasFruitFrequency}: ${fruit.frequency}",
                                        fontSize = 14f.sp
                                    )
                                    if (fruit.place.isNotEmpty()) Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                                        text = "${Strings.hasFruitPlace}: ${fruit.place}",
                                        fontSize = 14f.sp
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