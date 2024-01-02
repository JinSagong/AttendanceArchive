package com.jin.attendance_archive.model.data

import com.google.gson.internal.LinkedTreeMap
import java.io.Serializable

data class DataFruit(
    val id: String = "",
    val type: Int = -1,
    val people: String = "",
    val believer: String = "",
    val teacher: String = "",
    val age: Int = -1,
    val phone: String = "",
    val remeet: Boolean = false,
    val frequency: Int = -1,
    val place: String = ""
) : Serializable

@Suppress("UNCHECKED_CAST")
fun fetchDataFruit(data: LinkedTreeMap<*, *>) = data.let { item ->
    var id = ""
    var type = -1
    var people = ""
    var believer = ""
    var teacher = ""
    var age = -1
    var phone = ""
    var remeet = false
    var frequency = -1
    var place = ""
    item["id"]?.let { it as? String }?.let { id = it }
    item["type"]?.let { it as? Number }?.let { type = it.toInt() }
    item["people"]?.let { it as? String }?.let { people = it }
    item["believer"]?.let { it as? String }?.let { believer = it }
    item["teacher"]?.let { it as? String }?.let { teacher = it }
    item["age"]?.let { it as? Number }?.let { age = it.toInt() }
    item["phone"]?.let { it as? String }?.let { phone = it }
    item["remeet"]?.let { it as? Boolean }?.let { remeet = it }
        ?: item["remeet"]?.let { it as? String }?.lowercase()?.toBooleanStrictOrNull()
            ?.let { remeet = it }
    item["frequency"]?.let { it as? Number }?.let { frequency = it.toInt() }
    item["place"]?.let { it as? String }?.let { place = it }
    DataFruit(id, type, people, believer, teacher, age, phone, remeet, frequency, place)
}
