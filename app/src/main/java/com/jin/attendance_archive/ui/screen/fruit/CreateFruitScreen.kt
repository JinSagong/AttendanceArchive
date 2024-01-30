package com.jin.attendance_archive.ui.screen.fruit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.jin.attendance_archive.statemodel.CheckStateModel
import com.jin.attendance_archive.ui.component.CheckBoxRow
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner
import kotlinx.coroutines.launch

@Composable
fun CreateFruitScreen(checkStateModel: CheckStateModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    if (ScreenManager.createFruitScreen.value == Pair(true, true)) coroutineScope.launch {
        scrollState.scrollTo(0)
    }

    AnimatedVisibility(
        visible = ScreenManager.createFruitScreen.value.first,
        modifier = Modifier
            .fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.createFruitScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.createFruitScreen.value.second) -it else it })
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
                    text = checkStateModel.fruitTitle.value,
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier
                    .widthIn(0f.dp, Dimens.maxWidth)
                    .wrapContentHeight()
                    .verticalScroll(scrollState)
                    .padding(vertical = 12f.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16f.dp, vertical = 4f.dp),
                    value = checkStateModel.fruitBeliever.value,
                    textStyle = TextStyle(
                        fontSize = 14f.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text(Strings.hasFruitBeliever, maxLines = 1, fontSize = 14f.sp) },
                    onValueChange = { value -> checkStateModel.fruitBeliever.value = value },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions()
                )
                if (checkStateModel.fruitType.value == 0) OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16f.dp, vertical = 4f.dp),
                    value = checkStateModel.fruitPeople.value,
                    textStyle = TextStyle(
                        fontSize = 14f.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text(Strings.hasFruitPreacher, maxLines = 1, fontSize = 14f.sp) },
                    onValueChange = { value -> checkStateModel.fruitPeople.value = value },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions()
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16f.dp, vertical = 4f.dp),
                    value = checkStateModel.fruitTeacher.value,
                    textStyle = TextStyle(
                        fontSize = 14f.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text(Strings.hasFruitTeacher, maxLines = 1, fontSize = 14f.sp) },
                    onValueChange = { value -> checkStateModel.fruitTeacher.value = value },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions()
                )
                if (checkStateModel.fruitType.value == 0) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                        value = checkStateModel.fruitAge.value.let { if (it <= 0) "" else it.toString() },
                        textStyle = TextStyle(
                            fontSize = 14f.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        label = { Text(Strings.hasFruitAge, maxLines = 1, fontSize = 14f.sp) },
                        onValueChange = { value ->
                            val intValue = value.toIntOrNull() ?: -1
                            checkStateModel.fruitAge.value = if (intValue < 0) -1 else intValue
                        },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions()
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                        value = checkStateModel.fruitPhone.value,
                        textStyle = TextStyle(
                            fontSize = 14f.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        label = { Text(Strings.hasFruitPhone, maxLines = 1, fontSize = 14f.sp) },
                        onValueChange = { value -> checkStateModel.fruitPhone.value = value },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions()
                    )

                    CheckBoxRow(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                        checked = checkStateModel.fruitRemeet.value,
                        text = Strings.hasFruitRemeet,
                        onClick = {
                            checkStateModel.fruitRemeet.value = !checkStateModel.fruitRemeet.value
                        }
                    )
                } else if (checkStateModel.fruitType.value == 1) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                        value = checkStateModel.fruitFrequency.value.let { if (it <= 0) "" else it.toString() },
                        textStyle = TextStyle(fontSize = 14f.sp, fontWeight = FontWeight.Bold),
                        label = {
                            Text(Strings.hasFruitFrequency, maxLines = 1, fontSize = 14f.sp)
                        },
                        onValueChange = { value ->
                            val intValue = value.toIntOrNull() ?: -1
                            checkStateModel.fruitFrequency.value =
                                if (intValue < 0) -1 else intValue
                        },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions()
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16f.dp, vertical = 4f.dp),
                        value = checkStateModel.fruitPlace.value,
                        textStyle = TextStyle(
                            fontSize = 14f.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        label = {
                            Text(
                                Strings.hasFruitPlace, maxLines = 1,
                                fontSize = 14f.sp
                            )
                        },
                        onValueChange = { value -> checkStateModel.fruitPlace.value = value },
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

            Button(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16f.dp, 0f.dp, 16f.dp, 16f.dp),
                onClick = {
                    if (!SingleClickManager.isAvailable()) return@Button
                    checkStateModel.addFruit()
                }) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(0f.dp, 4f.dp),
                    text = checkStateModel.fruitConfirm.value,
                    textAlign = TextAlign.Center,
                    fontSize = 14f.sp
                )
            }

            if (checkStateModel.fruitId.value.isNotEmpty()) Button(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16f.dp, 0f.dp, 16f.dp, 16f.dp),
                onClick = {
                    if (!SingleClickManager.isAvailable()) return@Button
                    checkStateModel.removeFruit()
                }) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(0f.dp, 4f.dp),
                    text = Strings.hasFruitDelete,
                    textAlign = TextAlign.Center,
                    fontSize = 14f.sp
                )
            }
        }
    }
}