package com.jin.attendance_archive.ui.screen.management

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
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
import com.jin.attendance_archive.statemodel.UserStateModel
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManageUserScreen(userStateModel: UserStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    if (ScreenManager.manageUserScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollToItem(0)
    }
    SideEffect {
        if (ScreenManager.manageUserScreen.value == Pair(true, true)) userStateModel.fetchUserList()
    }

    AnimatedVisibility(
        visible = ScreenManager.manageUserScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.manageUserScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.manageUserScreen.value.second) -it else it })
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
                    text = Strings.managementUserTitle,
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            LazyColumn(
                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).fillMaxHeight()
                    .padding(vertical = 8f.dp),
                state = scrollState
            ) {
                items(
                    userStateModel.listUser.value,
                    key = { it.id }
                ) { user ->
                    Component.Card(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(vertical = 4f.dp, horizontal = 16f.dp)
                            .animateItemPlacement(),
                        onClick = {}
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.wrapContentSize()
                                    .padding(16f.dp)
                            ) {
                                Text(
                                    modifier = Modifier.wrapContentSize(),
                                    text = user.name,
                                    fontSize = 14f.sp
                                )
                                Text(
                                    modifier = Modifier.wrapContentSize(),
                                    text = "ID: ${user.userId}",
                                    fontSize = 12f.sp
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f).wrapContentHeight())
                            Text(
                                modifier = Modifier.wrapContentSize()
                                    .padding(16f.dp),
                                text = "${user.org.size}개 체크 가능",
                                fontSize = 14f.sp
                            )
                        }
                    }
                }
            }
        }
    }
}