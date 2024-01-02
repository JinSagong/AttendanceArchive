package com.jin.attendance_archive.util.db

import com.google.gson.JsonPrimitive
import com.google.gson.internal.LinkedTreeMap
import com.jin.attendance_archive.model.data.*

@Suppress("UNCHECKED_CAST")
object DataConverter {
    fun <T> convert(kClass: Class<T>, data: Any): List<T> {
        val array = getDataArray(data)
        return when (kClass) {
            DataAttendance::class.java -> array.map { fetchDataAttendance(it) } as List<T>
            DataAttendanceType::class.java -> array.map { fetchDataAttendanceType(it) } as List<T>
            DataDuty::class.java -> array.map { fetchDataDuty(it) } as List<T>
            DataFruit::class.java -> array.map { fetchDataFruit(it) } as List<T>
            DataLog::class.java -> array.map { fetchDataLog(it) } as List<T>
            DataOrganization::class.java -> array.map { fetchDataOrganization(it) } as List<T>
            DataPeople::class.java -> array.map { fetchDataPeople(it) } as List<T>
            DataUser::class.java -> array.map { fetchDataUser(it) } as List<T>
            DataVersion::class.java -> array.map { fetchDataVersion(it) } as List<T>
            else -> emptyList()
        }
    }

    private fun getDataArray(data: Any) = when (data) {
        is LinkedTreeMap<*, *> -> data.values.toTypedArray()
        is ArrayList<*> -> data.toTypedArray()
        else -> emptyArray<Any>()
    }.mapNotNull { it as? LinkedTreeMap<*, *> }

    fun <T> fetchRealtimeData(kClass: Class<T>, map: HashMap<String, JsonPrimitive>): T? =
        when (kClass) {
            DataVersion::class.java -> fetchDataVersion(map) as T
            else -> null
        }

    fun <T> fetchRealtimeDataList(kClass: Class<T>, map: HashMap<String, JsonPrimitive>): List<T> =
        when (kClass) {
            DataAttendanceType::class.java -> fetchDataAttendanceType(map) as List<T>
            DataAttendance::class.java -> fetchDataAttendance(map) as List<T>
            else -> emptyList()
        }
}