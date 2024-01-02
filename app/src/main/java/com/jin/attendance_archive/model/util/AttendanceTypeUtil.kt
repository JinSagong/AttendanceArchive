package com.jin.attendance_archive.model.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.jin.attendance_archive.model.data.DataAttendanceType
import com.jin.attendance_archive.util.compose.ComposeState

object AttendanceTypeUtil {
    val listAttendanceType = ComposeState<List<DataAttendanceType>>(emptyList())
    var mapAttendanceType = mutableStateMapOf<String, DataAttendanceType>()
        private set

    @Composable
    fun remember() {
        listAttendanceType.remember()
        mapAttendanceType = remember { mapAttendanceType }
    }

    fun setAttendanceTypeList(data: List<DataAttendanceType>) {
        listAttendanceType.value = data.sortedBy { it.id }
        mapAttendanceType.clear()
        mapAttendanceType.putAll(data.associateBy { it.id })
    }
}