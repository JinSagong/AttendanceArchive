package com.jin.attendance_archive.util

import com.jin.attendance_archive.model.util.UserUtil

object Accessibility {
    val switch
        get() = when (UserUtil.dataUser.value?.id) {
            "id000000", "id000001", "id000002", "id000003", "id000004", "id000005" -> true
            else -> false
        }
    val management get() = filingAttendance || filingFruit || managePeople || manageUser || log
    val filingAttendance
        get() = when (UserUtil.dataUser.value?.id) {
            "id000000", "id000001", "id000002", "id000003" -> true
            else -> false
        }
    val filingFruit
        get() = when (UserUtil.dataUser.value?.id) {
            "id000000", "id000001", "id000002", "id000003", "id000004", "id000005" -> true
            else -> false
        }
    val managePeople
        get() = when (UserUtil.dataUser.value?.id) {
            "id000000", "id000001", "id000002", "id000003" -> true
            else -> false
        }
    val manageUser
        get() = when (UserUtil.dataUser.value?.id) {
            "id000000", "id000001", "id000002", "id000003" -> true
            else -> false
        }
    val log
        get() = when (UserUtil.dataUser.value?.id) {
            "id000000", "id000001", "id000002", "id000003" -> true
            else -> false
        }
}