package com.jin.attendance_archive.model.data

import com.google.gson.internal.LinkedTreeMap
import java.io.Serializable

data class DataAttendanceItem(
    val id: String = "",
    val checked: Int = -1,
    val reason: String = ""
) : Serializable

@Suppress("UNCHECKED_CAST")
fun fetchDataAttendanceItem(data: LinkedTreeMap<*, *>) = data.let { item ->
    var id = ""
    var checked = -1
    var reason = ""
    item["id"]?.let { it as? String }?.let { id = it }
    item["checked"]?.let { it as? Number }?.let { checked = it.toInt() }
    item["reason"]?.let { it as? String }?.let { reason = it }
    DataAttendanceItem(id, checked, reason)
}
