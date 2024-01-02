package com.jin.attendance_archive.model.data

import com.google.gson.internal.LinkedTreeMap
import java.io.Serializable

data class DataUser(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val org: List<String> = emptyList()
) : Serializable

@Suppress("UNCHECKED_CAST")
fun fetchDataUser(data: LinkedTreeMap<*, *>) = data.let { item ->
    var id = ""
    var userId = ""
    var name = ""
    var org: List<String> = emptyList()
    item["id"]?.let { it as? String }?.let { id = it }
    item["userId"]?.let { it as? String }?.let { userId = it }
    item["name"]?.let { it as? String }?.let { name = it }
    item["org"]?.let { it as? String }
        ?.let { org = it.split("/").filter { item -> item.isNotEmpty() } }
    DataUser(id, userId, name, org)
}