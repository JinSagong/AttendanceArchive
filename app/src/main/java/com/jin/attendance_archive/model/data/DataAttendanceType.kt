package com.jin.attendance_archive.model.data

import com.google.gson.JsonPrimitive
import com.google.gson.internal.LinkedTreeMap
import com.jin.attendance_archive.util.db.asBoolean
import com.jin.attendance_archive.util.db.asString

data class DataAttendanceType(
    val id: String = "",
    val name: String = "",
    val hasFruit: Boolean = false,
    val on: Boolean = false
)

@Suppress("UNCHECKED_CAST")
fun fetchDataAttendanceType(data: LinkedTreeMap<*, *>) = data.let { item ->
    var id = ""
    var name = ""
    var hasFruit = false
    var on = false
    item["id"]?.let { it as? String }?.let { id = it }
    item["name"]?.let { it as? String }?.let { name = it }
    item["hasFruit"]?.let { it as? Boolean }?.let { hasFruit = it }
        ?: item["hasFruit"]?.let { it as? String }?.lowercase()?.toBooleanStrictOrNull()
            ?.let { hasFruit = it }
    item["on"]?.let { it as? Boolean }?.let { on = it }
        ?: item["on"]?.let { it as? String }?.lowercase()?.toBooleanStrictOrNull()
            ?.let { on = it }
    DataAttendanceType(id, name, hasFruit, on)
}

fun fetchDataAttendanceType(map: HashMap<String, JsonPrimitive>) = map.let { item ->
    item.keys
        .map { Pair(it.split(","), it) }
        .groupBy { it.first.firstOrNull() }
        .filter { it.key != null }
        .values
        .map { list ->
            var id = ""
            var name = ""
            var hasFruit = false
            var on = false
            list.forEach { node ->
                if (node.first.size == 2) when (node.first[1]) {
                    "id" -> item[node.second]?.asString()?.let { id = it }
                    "name" -> item[node.second]?.asString()?.let { name = it }
                    "hasFruit" -> item[node.second]?.asBoolean()?.let { hasFruit = it }
                    "on" -> item[node.second]?.asBoolean()?.let { on = it }
                }
            }
            DataAttendanceType(id, name, hasFruit, on)
        }
}
