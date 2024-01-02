package com.jin.attendance_archive.model.data

import com.google.gson.internal.LinkedTreeMap
import java.io.Serializable

data class DataLog(
    val id: String = "",
    val userName: String = "",
    val prevState: String = "",
    val newState: String = "",
    val time: Long = -1L
) : Serializable

@Suppress("UNCHECKED_CAST")
fun fetchDataLog(data: LinkedTreeMap<*, *>) = data.let { item ->
    var id = ""
    var userName = ""
    var prevState = ""
    var newState = ""
    var time = -1L
    item["id"]?.let { it as? String }?.let { id = it }
    item["userName"]?.let { it as? String }?.let { userName = it }
    item["prevState"]?.let { it as? String }?.let { prevState = it }
    item["newState"]?.let { it as? String }?.let { newState = it }
    item["time"]?.let { it as? Number }?.let { time = it.toLong() }
    DataLog(id, userName, prevState, newState, time)
}