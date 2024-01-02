package com.jin.attendance_archive.model.data

import com.google.gson.JsonPrimitive
import com.google.gson.internal.LinkedTreeMap
import com.jin.attendance_archive.util.db.asInt
import java.io.Serializable

data class DataVersion(
    val duty: Int = 0,
    val organization: Int = 0,
    val people: Int = 0
) : Serializable

fun fetchDataVersion(data: LinkedTreeMap<*, *>?) = data.let { item ->
    var duty = 0
    var organization = 0
    var people = 0
    item?.get("duty")?.let { it as? Number }?.let { duty = it.toInt() }
    item?.get("organization")?.let { it as? Number }?.let { organization = it.toInt() }
    item?.get("people")?.let { it as? Number }?.let { people = it.toInt() }
    DataVersion(duty, organization, people)
}

fun fetchDataVersion(map: HashMap<String, JsonPrimitive>) = map.let { item ->
    var duty = 0
    var organization = 0
    var people = 0
    item["duty"]?.asInt()?.let { duty = it }
    item["organization"]?.asInt()?.let { organization = it }
    item["people"]?.asInt()?.let { people = it }
    DataVersion(duty, organization, people)
}