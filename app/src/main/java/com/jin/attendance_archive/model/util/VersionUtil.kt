package com.jin.attendance_archive.model.util

import com.jin.attendance_archive.model.data.DataVersion

object VersionUtil {
    fun setVersion(data:DataVersion) {
        data.duty
        data.organization
        data.people
    }
}