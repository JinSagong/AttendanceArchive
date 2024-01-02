package com.jin.attendance_archive.ui.screen.management.people

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.util.DutyUtil
import com.jin.attendance_archive.statemodel.PeopleStateModel
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.ui.component.ListPicker
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import com.jin.attendance_archive.util.compose.clickableWithoutRipple
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PeopleEditorScreen(peopleStateModel: PeopleStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    if (ScreenManager.peopleEditorScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollToItem(0)
    }

    AnimatedVisibility(
        visible = ScreenManager.peopleEditorScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.peopleEditorScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.peopleEditorScreen.value.second) -it else it })
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
                    text = "${if (peopleStateModel.peopleId.value.isNullOrEmpty()) Strings.managePeopleTitleCreate else Strings.managePeopleTitleEdit}: ${peopleStateModel.peopleNamePrev.value}",
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.wrapContentSize()
                        .clickable {
                            if (!SingleClickManager.isAvailable()) return@clickable
                            peopleStateModel.update(coroutineScope)
                        }
                        .padding(8f.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).fillMaxHeight(),
                state = scrollState
            ) {
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(16f.dp, 16f.dp, 16f.dp, 0f.dp),
                        text = Strings.managePeopleSubtitle1,
                        fontSize = 14f.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(16f.dp),
                        value = peopleStateModel.peopleName.value,
                        textStyle = TextStyle(fontSize = 14f.sp, fontWeight = FontWeight.Bold),
                        label = {
                            Text(Strings.searchPeoplePlaceHolder, maxLines = 1, fontSize = 14f.sp)
                        },
                        onValueChange = { value -> peopleStateModel.peopleName.value = value },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions()
                    )
                }
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(16f.dp, 16f.dp, 16f.dp, 0f.dp),
                        text = Strings.managePeopleSubtitle2,
                        fontSize = 14f.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    ListPicker(
                        modifier = Modifier.wrapContentSize().padding(horizontal = 16f.dp),
                        value = peopleStateModel.peopleDuty.value,
                        label = { if (it.isEmpty()) "직분선택" else DutyUtil.mapDuty[it]?.name.orEmpty() },
                        onValueChange = { peopleStateModel.peopleDuty.value = it },
                        list = listOf("") + DutyUtil.mapDuty.values.map { it.id }.sorted()
                    )
                }
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(16f.dp, 16f.dp, 16f.dp, 0f.dp),
                        text = Strings.managePeopleSubtitle3,
                        fontSize = 14f.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(horizontal = 16f.dp),
                        text = Strings.managePeopleSubtitle3Guide,
                        fontSize = 12f.sp
                    )
                }
                items(peopleStateModel.peopleOrg.value + listOf(null)) { item ->
                    Component.Card(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(vertical = 4f.dp, horizontal = 16f.dp)
                            .animateItemPlacement(),
                        onClick = {
                            if (!SingleClickManager.isAvailable()) return@Card
                            if (item == null) peopleStateModel.moveToOrgEditor(null)
                            else peopleStateModel.checkOrg(item)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                .padding(8f.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1f).wrapContentHeight()
                                    .padding(8f.dp),
                                text = if (item == null) Strings.managePeopleOrgCreate else {
                                    val region =
                                        if (item.region == 1) "[구미] " else if (item.region == 2) "[서울] " else ""
                                    val category = "${item.category1}: ${item.category2}"
                                    val category3 =
                                        if (item.category3.isNotEmpty()) " ${item.category3}" else ""
                                    region + category + category3
                                },
                                fontSize = 14f.sp
                            )
                            if (item == null) Icon(
                                modifier = Modifier.wrapContentSize()
                                    .padding(8f.dp),
                                imageVector = Icons.Filled.Add,
                                contentDescription = null
                            ) else {
                                Icon(
                                    modifier = Modifier.wrapContentSize()
                                        .padding(8f.dp)
                                        .clickableWithoutRipple {
                                            if (!SingleClickManager.isAvailable()) return@clickableWithoutRipple
                                            peopleStateModel.deleteOrg(item)
                                        },
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null
                                )
                                Icon(
                                    modifier = Modifier.wrapContentSize()
                                        .padding(8f.dp)
                                        .clickableWithoutRipple {
                                            if (!SingleClickManager.isAvailable()) return@clickableWithoutRipple
                                            peopleStateModel.moveToOrgEditor(item)
                                        },
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(16f.dp, 16f.dp, 16f.dp, 0f.dp),
                        text = Strings.managePeopleSubtitle4,
                        fontSize = 14f.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    ListPicker(
                        modifier = Modifier.wrapContentSize().padding(horizontal = 16f.dp),
                        value = peopleStateModel.peopleActivated.value,
                        label = { if (it) Strings.managePeopleActivated else Strings.managePeopleInactivated },
                        onValueChange = { peopleStateModel.peopleActivated.value = it },
                        list = listOf(true, false)
                    )
                }
            }
        }
    }
}