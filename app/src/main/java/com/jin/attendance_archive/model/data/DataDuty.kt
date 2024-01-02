package com.jin.attendance_archive.model.data

import com.google.gson.internal.LinkedTreeMap
import java.io.Serializable

data class DataDuty(
    val id: String = "",
    val name: String = ""
) : Serializable

@Suppress("UNCHECKED_CAST")
fun fetchDataDuty(data: LinkedTreeMap<*, *>) = data.let { item ->
    var id = ""
    var name = ""
    item["id"]?.let { it as? String }?.let { id = it }
    item["name"]?.let { it as? String }?.let { name = it }
    DataDuty(id, name)
}
