package com.jin.attendance_archive.model.util

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import com.jin.attendance_archive.model.data.DataAttendance
import com.jin.attendance_archive.util.compose.ComposeState

object AttendanceUtil {
    val listAttendance = ComposeState<List<DataAttendance>>(emptyList())
    val mapAttendance = mutableStateMapOf<String, List<DataAttendance>>() // not remember!
    val loaded = ComposeState(false)

    var countAttendance: Map<String, State<Float>> = emptyMap()

    @Composable
    fun remember() {
        listAttendance.remember()
        loaded.remember()
        countAttendance = listAttendance.value.groupingBy { it.attendanceType }.eachCount()
            .mapValues {
                updateTransition(it.value).animateFloat { value -> value.toFloat() }
            }
    }

    fun setAttendanceList(data: List<DataAttendance>) {
        listAttendance.value = data.sortedBy { it.id }
        mapAttendance.clear()
        mapAttendance.putAll(data.sortedBy { it.id }.groupBy { it.attendanceType })
    }
}