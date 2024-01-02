package com.jin.attendance_archive.model.util

import androidx.compose.runtime.Composable
import com.jin.attendance_archive.model.data.DataUser
import com.jin.attendance_archive.model.pref.UserPref
import com.jin.attendance_archive.util.compose.ComposeState

object UserUtil {
    val dataUser = ComposeState(UserPref.getUser())

    @Composable
    fun remember() {
        dataUser.remember()
    }

    fun setUser(data: DataUser?) {
        UserPref.setUser(data)
        dataUser.value = data
    }

    fun isGumi() = dataUser.value
        ?.org
        ?.firstOrNull()
        ?.let { OrganizationUtil.mapOrganizationByOrgId[it] }
        ?.firstOrNull()
        ?.region
        ?.let { it == 1 || it == 3 }
        ?: true

    fun getRegion() = if (isGumi()) 1 else 2
}