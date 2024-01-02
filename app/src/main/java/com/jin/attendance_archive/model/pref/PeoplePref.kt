package com.jin.attendance_archive.model.pref

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.jin.attendance_archive.model.data.DataPeople
import com.jin.attendance_archive.model.pref.util.IntegerPreference
import com.jin.attendance_archive.util.android.AndroidManager
import com.jin.attendance_archive.model.pref.util.get
import com.jin.attendance_archive.model.pref.util.set

object PeoplePref {
    private val Context.dataStore by preferencesDataStore("people")
    private const val quantity = 100

    private var prefCount by IntegerPreference(
        AndroidManager.context.dataStore, "peoplePrefCount", 0
    )

    fun setPeopleList(data: List<DataPeople>) {
        val text = data.joinToString("/") {
            "${it.id},${it.name},${it.org.joinToString("@") { org -> org }},${it.duty},${it.activated}"
        }
        val count = text.length / quantity
        prefCount = count
        (0..count).forEach { n ->
            val subText = text.substring(
                n * quantity,
                ((n + 1) * quantity).let { if (it > text.length) text.length else it }
            )
            AndroidManager.context.dataStore.set("people$n", subText)
        }
    }

    fun getPeopleList() = (0..prefCount)
        .map { n -> AndroidManager.context.dataStore.get("people$n", "") }
        .joinToString("") { it }
        .split("/")
        .mapNotNull {
            val data = it.split(",")
            if (data.size == 5) DataPeople(
                data[0],
                data[1],
                data[2].split("@").filter { item -> item.isNotEmpty() },
                data[3],
                data[4].toBooleanStrictOrNull() ?: false
            ) else null
        }
}