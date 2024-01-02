package com.jin.attendance_archive.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.statemodel.SignStateModel
import com.jin.attendance_archive.ui.component.Component
import com.jin.attendance_archive.res.Dimens
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.SingleClickManager
import com.jin.attendance_archive.util.compose.addFocusCleaner

@Composable
fun SignInScreen(signStateModel: SignStateModel) {
    AnimatedVisibility(
        visible = ScreenManager.signInScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.signInScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.signInScreen.value.second) -it else it })
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().wrapContentHeight().align(Alignment.Center)) {
                Text(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        .padding(16f.dp)
                        .align(Alignment.CenterHorizontally),
                    text = Strings.appName,
                    textAlign = TextAlign.Center,
                    fontSize = 24f.sp,
                    fontWeight = FontWeight.Bold
                )
                Component.Card(
                    modifier = Modifier.widthIn(0f.dp, Dimens.maxWidth).wrapContentHeight()
                        .padding(16f.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Column(
                        modifier = Modifier.wrapContentSize()
                            .padding(16f.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            value = signStateModel.idState.value,
                            textStyle = TextStyle(fontSize = 14f.sp, fontWeight = FontWeight.Bold),
                            label = {
                                Text(Strings.signInIdLabel, maxLines = 1, fontSize = 14f.sp)
                            },
                            placeholder = {
                                Text(Strings.signInIdPlaceHolder, maxLines = 1, fontSize = 14f.sp)
                            },
                            onValueChange = { value -> signStateModel.idState.value = value },
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                signStateModel.signIn()
                            })
                        )
                        Button(modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .padding(0f.dp, 16f.dp, 0f.dp, 0f.dp),
                            onClick = {
                                if (!SingleClickManager.isAvailable()) return@Button
                                signStateModel.signIn()
                            }) {
                            Row(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                    .padding(0f.dp, 4f.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Login, contentDescription = null,
                                    modifier = Modifier.wrapContentSize()
                                        .padding(0f.dp, 0f.dp, 8f.dp, 0f.dp)
                                )
                                Text(
                                    modifier = Modifier.wrapContentSize(),
                                    text = Strings.signInConfirm,
                                    textAlign = TextAlign.Center,
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