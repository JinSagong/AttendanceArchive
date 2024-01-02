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
import com.jin.attendance_archive.model.util.OrganizationUtil
import com.jin.attendance_archive.statemodel.PeopleStateModel
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.LocalMyColorScheme
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManagePeopleScreen(peopleStateModel: PeopleStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    if (ScreenManager.managePeopleScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollToItem(0)
    }

    AnimatedVisibility(
        visible = ScreenManager.managePeopleScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.managePeopleScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.managePeopleScreen.value.second) -it else it })
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
                    text = Strings.managementPeopleTitle,
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    .padding(16f.dp, 16f.dp, 16f.dp, 0f.dp),
                text = Strings.managePeopleGuide,
                fontSize = 14f.sp
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    .padding(16f.dp),
                value = peopleStateModel.searchQuery.value,
                textStyle = TextStyle(fontSize = 14f.sp, fontWeight = FontWeight.Bold),
                label = {
                    Text(Strings.searchPeopleLabel, maxLines = 1, fontSize = 14f.sp)
                },
                placeholder = {
                    Text(Strings.searchPeoplePlaceHolder, maxLines = 1, fontSize = 14f.sp)
                },
                onValueChange = { value -> peopleStateModel.searchQuery.value = value },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions()
            )
            LazyColumn(
                modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).fillMaxHeight(),
                state = scrollState
            ) {
                items(
                    peopleStateModel.searchPeopleResult(onlyActivated = false),
                    key = { it.id }) { people ->
                    Component.Card(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(vertical = 4f.dp, horizontal = 16f.dp)
                            .animateItemPlacement(),
                        onClick = {
                            if (!SingleClickManager.isAvailable()) return@Card
                            peopleStateModel.moveToEditor(people)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.wrapContentSize()
                                    .padding(16f.dp)
                            ) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.wrapContentSize(),
                                        text = DutyUtil.mapDuty[people.duty]?.name.orEmpty(),
                                        fontSize = 14f.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        modifier = Modifier.wrapContentSize()
                                            .padding(horizontal = 4f.dp),
                                        text = people.name,
                                        fontSize = 14f.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!people.activated) Text(
                                        modifier = Modifier.wrapContentSize(),
                                        text = Strings.managePeopleInactivated,
                                        fontSize = 14f.sp,
                                        color = LocalMyColorScheme.current.red
                                    )
                                }
                                people.org.forEach { org ->
                                    val dataOrg = OrganizationUtil.mapOrganization[org]
                                    val region =
                                        if (dataOrg?.region == 1) "[구미] " else if (dataOrg?.region == 2) "[서울] " else ""
                                    val category = "${dataOrg?.category1}: ${dataOrg?.category2}"
                                    val category3 =
                                        if (!dataOrg?.category3.isNullOrEmpty()) " ${dataOrg?.category3}" else ""
                                    Text(
                                        modifier = Modifier.wrapContentSize(),
                                        text = region + category + category3,
                                        fontSize = 12f.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f).wrapContentHeight())
                            Icon(
                                modifier = Modifier.wrapContentSize()
                                    .padding(0f.dp, 16f.dp, 16f.dp, 16f.dp),
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null
                            )
                        }
                    }
                }
                if (peopleStateModel.searchPeopleResult(onlyActivated = false)
                        .isEmpty() && peopleStateModel.searchQuery.value.isNotEmpty()
                ) item {
                    Component.Card(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(vertical = 4f.dp, horizontal = 16f.dp)
                            .animateItemPlacement(),
                        onClick = {
                            if (!SingleClickManager.isAvailable()) return@Card
                            peopleStateModel.moveToEditor(null)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.wrapContentSize()
                                    .padding(16f.dp),
                                text = peopleStateModel.searchQuery.value,
                                fontSize = 14f.sp
                            )
                            Spacer(modifier = Modifier.weight(1f).wrapContentHeight())
                            Icon(
                                modifier = Modifier.wrapContentSize()
                                    .padding(16f.dp),
                                imageVector = Icons.Filled.PersonAddAlt1,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}