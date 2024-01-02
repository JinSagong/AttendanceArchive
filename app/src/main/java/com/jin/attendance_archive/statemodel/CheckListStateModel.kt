package com.jin.attendance_archive.statemodel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.data.*
import com.jin.attendance_archive.model.util.AttendanceUtil
import com.jin.attendance_archive.model.util.OrganizationUtil
import com.jin.attendance_archive.ui.component.Toasty
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.compose.ComposeState

class CheckListStateModel private constructor() {
    val attendanceTypeSelected = ComposeState<DataAttendanceType?>(null)
    private val hasFruit get() = attendanceTypeSelected.value?.hasFruit == true

    @Composable
    private fun fetch() {
        attendanceTypeSelected.remember()
    }

    fun selectAttendanceType(data: DataAttendanceType) {
        if (OrganizationUtil.myOrganization(data.hasFruit).isNotEmpty()) {
            attendanceTypeSelected.value = data
            ScreenManager.openCheckListScreen()
        } else {
            Toasty.show(Strings.attendanceNotToDo)
        }
    }

    fun myCategory() = OrganizationUtil.myCategory(hasFruit)

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun categoryPagerState() = rememberPagerState(0) { myCategory().size }

    fun checkList(): List<Pair<List<DataOrganization>, Boolean>> {
        val checked = attendanceTypeSelected.value?.id?.let { attendanceType ->
            AttendanceUtil.mapAttendance[attendanceType]?.map { it.org }.orEmpty()
        }.orEmpty()
        return OrganizationUtil.myOrganization(hasFruit).map {
            it.value to (it.key in checked)
        }
    }

    companion object {
        private var instance: CheckListStateModel? = null

        @Composable
        fun fetch() = instance ?: run {
            val stateModel = CheckListStateModel()
            stateModel.fetch()
            instance = stateModel
            stateModel
        }
    }
}