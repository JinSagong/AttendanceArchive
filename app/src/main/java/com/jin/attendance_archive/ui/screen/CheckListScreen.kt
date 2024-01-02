package com.jin.attendance_archive.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
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
import com.jin.attendance_archive.statemodel.CheckListStateModel
import com.jin.attendance_archive.statemodel.CheckStateModel
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.LocalMyColorScheme
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckListScreen(checkListStateModel: CheckListStateModel, checkStateModel: CheckStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = checkListStateModel.categoryPagerState()
    val scrollState = checkListStateModel.myCategory().map { rememberLazyListState() }
    if (ScreenManager.checkListScreen.value == Pair(true, true)) coroutineScope.launch {
        pagerState.scrollToPage(0)
        scrollState.forEach { it.scrollToItem(0) }
    }

    AnimatedVisibility(
        visible = ScreenManager.checkListScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.checkListScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.checkListScreen.value.second) -it else it })
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
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        .padding(8f.dp),
                    text = checkListStateModel.attendanceTypeSelected.value?.name.orEmpty(),
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (pagerState.pageCount > 1) ScrollableTabRow(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                selectedTabIndex = pagerState.currentPage.let { if (it >= pagerState.pageCount) (pagerState.pageCount - 1) else it },
                edgePadding = 0f.dp,
                divider = {}
            ) {
                checkListStateModel.myCategory().forEachIndexed { idx, category ->
                    Tab(
                        text = {
                            Text(
                                modifier = Modifier.wrapContentSize(),
                                text = category,
                                fontSize = 14f.sp
                            )
                        },
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(idx)
                                scrollState.getOrNull(idx)?.animateScrollToItem(0)
                            }
                        },
                        selected = pagerState.currentPage == idx
                    )
                }
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .padding(vertical = 12f.dp),
                    state = scrollState.getOrNull(page) ?: rememberLazyListState(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(
                        checkListStateModel.checkList().filter {
                            it.first.firstOrNull()?.category1 == checkListStateModel.myCategory()
                                .getOrNull(page)
                                    && !it.first.firstOrNull()?.category1.isNullOrEmpty()
                        },
                        key = { it.first.firstOrNull()?.category2.orEmpty() }
                    ) { map ->
                        Component.Card(
                            modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).wrapContentHeight()
                                .padding(vertical = 4f.dp, horizontal = 16f.dp)
                                .animateItemPlacement(),
                            onClick = {
                                coroutineScope.launch(Dispatchers.Default) {
                                    checkStateModel.enterCheckScreen(
                                        checkListStateModel.attendanceTypeSelected.value, map.first
                                    )
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.wrapContentSize()
                                        .padding(16f.dp),
                                    text = map.first.firstOrNull()?.category2.orEmpty(),
                                    fontSize = 14f.sp
                                )
                                Spacer(modifier = Modifier.weight(1f).wrapContentHeight())
                                Text(
                                    modifier = Modifier.wrapContentSize()
                                        .padding(16f.dp),
                                    text = if (map.second) Strings.checkStatusDone else Strings.checkStatusNotYet,
                                    color = if (map.second) LocalMyColorScheme.current.blue else LocalMyColorScheme.current.red,
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