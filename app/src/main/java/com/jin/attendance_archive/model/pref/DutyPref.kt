package com.jin.attendance_archive.model.pref

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.jin.attendance_archive.model.data.DataDuty
import com.jin.attendance_archive.model.pref.util.*
import com.jin.attendance_archive.model.pref.util.get
import com.jin.attendance_archive.util.android.AndroidManager

object DutyPref {
    private val Context.dataStore by preferencesDataStore("duty")
    private const val quantity = 100

    private var prefCount by IntegerPreference(
        AndroidManager.context.dataStore, "dutyPrefCount", 0
    )

    fun setDutyList(data: List<DataDuty>) {
        val text = data.joinToString("/") {
            "${it.id},${it.name}"
        }
        val count = text.length / quantity
        prefCount = count
        (0..count).forEach { n ->
            val subText = text.substring(
                n * quantity,
                ((n + 1) * quantity).let { if (it > text.length) text.length else it }
            )
            AndroidManager.context.dataStore.set("duty$n", subText)
        }
    }

    fun getDutyList() = (0..prefCount)
        .map { n -> AndroidManager.context.dataStore.get("duty$n", "") }
        .joinToString("") { it }
        .split("/")
        .mapNotNull {
            val data = it.split(",")
            if (data.size == 2) DataDuty(data[0], data[1]) else null
        }
}