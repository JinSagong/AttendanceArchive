package com.jin.attendance_archive.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.compose.addFocusCleaner

@Composable
fun ErrorScreen() {
    AnimatedVisibility(
        visible = ScreenManager.errorScreen.value.first,
        modifier = Modifier.fillMaxSize()
            .addFocusCleaner(LocalFocusManager.current),
        enter = slideInHorizontally(initialOffsetX = { if (ScreenManager.errorScreen.value.second) it else -it }),
        exit = slideOutHorizontally(targetOffsetX = { if (ScreenManager.errorScreen.value.second) -it else it })
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(80f.dp).padding(8f.dp)
                )
                Text(
                    modifier = Modifier.wrapContentSize()
                        .padding(horizontal = 16f.dp, vertical = 8f.dp),
                    text = Strings.networkError,
                    textAlign = TextAlign.Center,
                    fontSize = 14f.sp
                )
            }
        }
    }
}