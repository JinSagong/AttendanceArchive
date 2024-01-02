package com.jin.attendance_archive.model.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.jin.attendance_archive.model.data.DataDuty

object DutyUtil {
    var mapDuty = mutableStateMapOf<String, DataDuty>()
        private set

    @Composable
    fun remember() {
        mapDuty = remember { mapDuty }
    }

    fun setDutyList(data: List<DataDuty>) {
        mapDuty.clear()
        mapDuty.putAll(data.associateByTo(HashMap()) { it.id })
    }
}