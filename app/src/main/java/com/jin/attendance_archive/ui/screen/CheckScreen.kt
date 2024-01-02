package com.jin.attendance_archive.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.statemodel.CheckStateModel
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.LocalMyColorScheme
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@Composable
fun CheckScreen(checkStateModel: CheckStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    if (ScreenManager.checkScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollToItem(0)
    }

    AnimatedVisibility(
        visible = ScreenManager.checkScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.checkScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.checkScreen.value.second) -it else it })
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

            LazyColumn(
                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).fillMaxHeight()
                    .padding(vertical = 12f.dp),
                state = scrollState
            ) {
                checkStateModel.listPeople.value.forEach { data ->
                    if (data.first.isNotEmpty()) item {
                        Text(
                            modifier = Modifier.wrapContentSize()
                                .padding(16f.dp),
                            text = data.first,
                            fontSize = 14f.sp
                        )
                    }
                    items(data.second) { people ->
                        Component.Card(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(vertical = 4f.dp, horizontal = 16f.dp),
                            onClick = {
                                people.second.value = if (people.second.value == 0) 1 else 0
                            }
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                        .align(Alignment.CenterStart)
                                        .padding(vertical = 10f.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.wrapContentSize().defaultMinSize(minWidth = 56f.dp)
                                            .padding(16f.dp, 16f.dp, 0f.dp, 16f.dp),
                                        text = if (people.second.value == 1) Strings.attendanceCheckType1 else Strings.attendanceCheckType0,
                                        color = if (people.second.value == 1) LocalMyColorScheme.current.green else LocalMyColorScheme.current.red,
                                        fontSize = 14f.sp
                                    )
                                    Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(8f.dp, 16f.dp, 16f.dp, 16f.dp),
                                        text = people.first.name,
                                        fontSize = 14f.sp
                                    )
                                }
                                this@Card.AnimatedVisibility(
                                    visible = people.second.value != 1,
                                    modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight()
                                        .padding(8f.dp)
                                        .align(Alignment.CenterEnd)
                                ) {
                                    OutlinedTextField(
                                        modifier = Modifier.fillMaxSize(),
                                        value = people.third.value,
                                        shape = RoundedCornerShape(12f.dp),
                                        singleLine = true,
                                        placeholder = { Text(Strings.attendanceReason) },
                                        onValueChange = { people.third.value = it }
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