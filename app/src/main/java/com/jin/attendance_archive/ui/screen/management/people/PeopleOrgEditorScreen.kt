package com.jin.attendance_archive.ui.screen.management.people

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.statemodel.PeopleStateModel
import com.jin.attendance_archive.ui.component.ListPicker
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@Composable
fun PeopleOrgEditorScreen(peopleStateModel: PeopleStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    if (ScreenManager.peopleOrgEditorScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollTo(0)
    }

    AnimatedVisibility(
        visible = ScreenManager.peopleOrgEditorScreen.value.first,
        modifier = Modifier
            .fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.peopleOrgEditorScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.peopleOrgEditorScreen.value.second) -it else it })
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = null,
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable {
                            if (!SingleClickManager.isAvailable()) return@clickable
                            ScreenManager.onBackPressed()
                        }
                        .padding(8f.dp)
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8f.dp),
                    text = "${if (peopleStateModel.peopleOrgResult.value == null) Strings.managePeopleOrgTitleCreate else Strings.managePeopleOrgTitleEdit}: ${peopleStateModel.peopleName.value}",
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier
                    .widthIn(0f.dp, Dimens.maxWidth)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16f.dp, 16f.dp, 16f.dp, 0f.dp),
                    text = Strings.managePeopleOrgSubtitle1,
                    fontSize = 14f.sp,
                    fontWeight = FontWeight.Bold
                )
                ListPicker(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 16f.dp),
                    value = peopleStateModel.peopleOrgRegion.value,
                    label = { if (it == 1) "구미" else "서울" },
                    onValueChange = {
                        peopleStateModel.peopleOrgRegion.value = it
                        peopleStateModel.updateOrgState()
                    },
                    list = listOf(1, 2)
                )

                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    visible = peopleStateModel.peopleOrgCategory1List.value.isNotEmpty()
                ) {
                    Column {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(16f.dp, 16f.dp, 16f.dp, 0f.dp),
                            text = Strings.managePeopleOrgSubtitle2,
                            fontSize = 14f.sp,
                            fontWeight = FontWeight.Bold
                        )
                        ListPicker(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(horizontal = 16f.dp),
                            value = peopleStateModel.peopleOrgCategory1.value,
                            label = { it.ifEmpty { "선택" } },
                            onValueChange = {
                                peopleStateModel.peopleOrgCategory1.value = it
                                peopleStateModel.updateOrgState()
                            },
                            list = listOf("") + peopleStateModel.peopleOrgCategory1List.value
                        )
                    }
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    visible = peopleStateModel.peopleOrgCategory2List.value.isNotEmpty()
                ) {
                    Column {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(16f.dp, 16f.dp, 16f.dp, 0f.dp),
                            text = Strings.managePeopleOrgSubtitle3,
                            fontSize = 14f.sp,
                            fontWeight = FontWeight.Bold
                        )
                        ListPicker(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(horizontal = 16f.dp),
                            value = peopleStateModel.peopleOrgCategory2.value,
                            label = { it.ifEmpty { "선택" } },
                            onValueChange = {
                                peopleStateModel.peopleOrgCategory2.value = it
                                peopleStateModel.updateOrgState()
                            },
                            list = listOf("") + peopleStateModel.peopleOrgCategory2List.value
                        )
                    }
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    visible = peopleStateModel.peopleOrgCategory3List.value.isNotEmpty()
                ) {
                    Column {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(16f.dp, 16f.dp, 16f.dp, 0f.dp),
                            text = Strings.managePeopleOrgSubtitle4,
                            fontSize = 14f.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ListPicker(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(horizontal = 16f.dp),
                                value = peopleStateModel.peopleOrgCategory3.value,
                                label = { it.ifEmpty { if (peopleStateModel.peopleOrgCustomCategory3.value != null) Strings.managePeopleOrgCustom else "선택" } },
                                onValueChange = {
                                    peopleStateModel.peopleOrgCategory3.value = it
                                    peopleStateModel.updateOrgState()
                                },
                                list = listOf("") + peopleStateModel.peopleOrgCategory3List.value
                            )
                            AnimatedVisibility(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                visible = peopleStateModel.peopleOrgCustomCategory3.value != null
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(16f.dp),
                                    value = peopleStateModel.peopleOrgCustomCategory3.value.orEmpty(),
                                    textStyle = TextStyle(
                                        fontSize = 14f.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    label = {
                                        Text(
                                            Strings.managePeopleOrgCustom, maxLines = 1,
                                            fontSize = 14f.sp
                                        )
                                    },
                                    onValueChange = { value ->
                                        peopleStateModel.peopleOrgCustomCategory3.value = value
                                        peopleStateModel.updateOrgState(forUpdatingCustomCategory = true)
                                    },
                                    singleLine = true,
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions()
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    visible = peopleStateModel.peopleOrgResult.value != null
                ) {
                    Button(modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16f.dp),
                        onClick = {
                            if (!SingleClickManager.isAvailable()) return@Button
                            peopleStateModel.updateOrg()
                        }) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(0f.dp, 4f.dp),
                            text = Strings.managePeopleOrgConfirm,
                            textAlign = TextAlign.Center,
                            fontSize = 14f.sp
                        )
                    }
                }
            }
        }
    }
}