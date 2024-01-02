package com.jin.attendance_archive.model.util

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.model.data.DataLog
import com.jin.attendance_archive.util.DateTimeUtil

object LogUtil {
    fun getDataLog(prevState: String, newState: String): DataLog {
        val currentTime = System.currentTimeMillis()
        val userName = UserUtil.dataUser.value?.name.orEmpty()
        return DataLog(currentTime.toString(), userName, prevState, newState, currentTime)
    }

    @Composable
    fun LogText(data: DataLog) {
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                .padding(horizontal = 16f.dp, vertical = 8f.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                text = "[${DateTimeUtil.getLogTime(data.time)}] ${data.userName}",
                fontSize = 14f.sp,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                Text(
                    modifier = Modifier.wrapContentSize(),
                    text = "prev: ",
                    fontSize = 14f.sp,
                )
                Text(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    text = data.prevState,
                    fontSize = 14f.sp,
                )
            }
            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                Text(
                    modifier = Modifier.wrapContentSize(),
                    text = "new: ",
                    fontSize = 14f.sp,
                )
                Text(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    text = data.newState,
                    fontSize = 14f.sp,
                )
            }
        }
    }
}