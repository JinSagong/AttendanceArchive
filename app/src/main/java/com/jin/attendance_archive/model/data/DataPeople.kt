package com.jin.attendance_archive.model.data

import com.google.gson.internal.LinkedTreeMap
import java.io.Serializable

data class DataPeople(
    val id: String = "",
    val name: String = "",
    val org: List<String> = emptyList(),
    val duty: String = "",
    val activated: Boolean = false
) : Serializable {
    fun toImpl() = DataPeopleImpl(id, name, org.joinToString("/") { it }, duty, activated)
}

data class DataPeopleImpl(
    val id: String = "",
    val name: String = "",
    val org: String = "",
    val duty: String = "",
    val activated: Boolean = false
) : Serializable

@Suppress("UNCHECKED_CAST")
fun fetchDataPeople(data: LinkedTreeMap<*, *>) = data.let { item ->
    var id = ""
    var name = ""
    var org: List<String> = emptyList()
    var duty = ""
    var activated = false
    item["id"]?.let { it as? String }?.let { id = it }
    item["name"]?.let { it as? String }?.let { name = it }
    item["activated"]?.let { it as? Boolean }?.let { activated = it }
        ?: item["activated"]?.let { it as? String }?.lowercase()?.toBooleanStrictOrNull()
            ?.let { activated = it }
    item["org"]?.let { it as? String }
        ?.let { org = it.split("/").filter { item -> item.isNotEmpty() } }
    item["duty"]?.let { it as? String }?.let { duty = it }
    DataPeople(id, name, org, duty, activated)
}
