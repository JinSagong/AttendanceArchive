package com.jin.attendance_archive.model.data

import com.google.gson.JsonPrimitive
import com.google.gson.internal.LinkedTreeMap
import com.jin.attendance_archive.util.db.asBoolean
import com.jin.attendance_archive.util.db.asInt
import com.jin.attendance_archive.util.db.asString
import java.io.Serializable

data class DataAttendance(
    val id: String = "",
    val week: Int = -1,
    val attendanceType: String = "",
    val org: String = "",
    val items: Map<String, DataAttendanceItem> = emptyMap(),
    val fruits: Map<String, DataFruit> = emptyMap()
) : Serializable

@Suppress("UNCHECKED_CAST")
fun fetchDataAttendance(data: LinkedTreeMap<*, *>) = data.let { item ->
    var id = ""
    var week = -1
    var attendanceType = ""
    var org = ""
    var items: Map<String, DataAttendanceItem> = emptyMap()
    var fruits: Map<String, DataFruit> = emptyMap()
    item["id"]?.let { it as? String }?.let { id = it }
    item["week"]?.let { it as? Number }?.let { week = it.toInt() }
    item["attendanceType"]?.let { it as? String }?.let { attendanceType = it }
    item["org"]?.let { it as? String }?.let { org = it }
    item["items"]?.let { it as? LinkedTreeMap<*, *> }?.values
        ?.mapNotNull { it as? LinkedTreeMap<*, *> }
        ?.let { subItems ->
            items = subItems
                .associate { subItem -> fetchDataAttendanceItem(subItem).let { it.id to it } }
        }
    item["fruits"]?.let { it as? LinkedTreeMap<*, *> }?.values
        ?.mapNotNull { it as? LinkedTreeMap<*, *> }
        ?.let { subItems ->
            fruits = subItems.associate { subItem -> fetchDataFruit(subItem).let { it.id to it } }
        }
    DataAttendance(id, week, attendanceType, org, items, fruits)
}

fun fetchDataAttendance(map: HashMap<String, JsonPrimitive>) = map.let { item ->
    item.keys
        .map { Pair(it.split(","), it) }
        .groupBy { it.first.firstOrNull() }
        .filter { it.key != null }
        .values
        .map { list ->
            var id = ""
            var week = -1
            var attendanceType = ""
            var org = ""
            list.filter {
                it.first.getOrNull(1).let { first -> first != "items" && first != "fruits" }
            }.forEach { node ->
                if (node.first.size == 2) when (node.first[1]) {
                    "id" -> item[node.second]?.asString()?.let { id = it }
                    "week" -> item[node.second]?.asInt()?.let { week = it }
                    "attendanceType" -> item[node.second]?.asString()
                        ?.let { attendanceType = it }
                    "org" -> item[node.second]?.asString()?.let { org = it }
                }
            }
            val items = list
                .filter { it.first.getOrNull(1) == "items" }
                .groupBy { it.first.getOrNull(2) }
                .filter { it.key != null }
                .mapNotNull { map ->
                    var itemId = ""
                    var itemChecked = -1
                    var itemReason = ""
                    map.value.forEach { node ->
                        if (node.first.size == 4) when (node.first[3]) {
                            "id" -> item[node.second]?.asString()?.let { itemId = it }
                            "checked" -> item[node.second]?.asInt()?.let { itemChecked = it }
                            "reason" -> item[node.second]?.asString()?.let { itemReason = it }
                        }
                    }
                    if (map.key != null) map.key!! to DataAttendanceItem(
                        itemId,
                        itemChecked,
                        itemReason
                    ) else null
                }.toMap()
            val fruits = list
                .filter { it.first.getOrNull(1) == "fruits" }
                .groupBy { it.first.getOrNull(2) }
                .filter { it.key != null }
                .mapNotNull { map ->
                    var fruitId = ""
                    var fruitType = -1
                    var fruitPeople = ""
                    var fruitBeliever = ""
                    var fruitTeacher = ""
                    var fruitAge = -1
                    var fruitPhone = ""
                    var fruitRemeet = false
                    var fruitFrequency = -1
                    var fruitPlace = ""

                    map.value.forEach { node ->
                        if (node.first.size == 4) when (node.first[3]) {
                            "id" -> item[node.second]?.asString()?.let { fruitId = it }
                            "type" -> item[node.second]?.asInt()?.let { fruitType = it }
                            "people" -> item[node.second]?.asString()?.let { fruitPeople = it }
                            "believer" -> item[node.second]?.asString()?.let { fruitBeliever = it }
                            "teacher" -> item[node.second]?.asString()?.let { fruitTeacher = it }
                            "age" -> item[node.second]?.asInt()?.let { fruitAge = it }
                            "phone" -> item[node.second]?.asString()?.let { fruitPhone = it }
                            "remeet" -> item[node.second]?.asBoolean()?.let { fruitRemeet = it }
                            "frequency" -> item[node.second]?.asInt()?.let { fruitFrequency = it }
                            "place" -> item[node.second]?.asString()?.let { fruitPlace = it }
                        }
                    }
                    if (map.key != null) map.key!! to DataFruit(
                        fruitId,
                        fruitType,
                        fruitPeople,
                        fruitBeliever,
                        fruitTeacher,
                        fruitAge,
                        fruitPhone,
                        fruitRemeet,
                        fruitFrequency,
                        fruitPlace
                    ) else null
                }.toMap()
            DataAttendance(id, week, attendanceType, org, items, fruits)
        }
}