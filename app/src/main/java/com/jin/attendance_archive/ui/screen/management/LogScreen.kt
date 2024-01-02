package com.jin.attendance_archive.ui.screen.management

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.util.LogUtil
import com.jin.attendance_archive.statemodel.LogStateModel
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@Composable
fun LogScreen(logStateModel: LogStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    if (ScreenManager.logScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollToItem(0)
    }
    SideEffect {
        if (ScreenManager.logScreen.value == Pair(true, true)) logStateModel.fetchLogList()
    }

    AnimatedVisibility(
        visible = ScreenManager.logScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.logScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.logScreen.value.second) -it else it })
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
                    text = Strings.managementLogTitle,
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState
            ) {
                items(logStateModel.listLog.value) { log -> LogUtil.LogText(log) }
            }
        }
    }
}