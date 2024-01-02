package com.jin.attendance_archive.model.pref

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.jin.attendance_archive.model.pref.util.IntegerPreference
import com.jin.attendance_archive.util.android.AndroidManager
import com.jin.attendance_archive.model.pref.util.get
import com.jin.attendance_archive.model.pref.util.set

object ReasonPref {
    private val Context.dataStore by preferencesDataStore("reason")
    private const val quantity = 100

    private var prefCount by IntegerPreference(
        AndroidManager.context.dataStore, "reasonPrefCount", 0
    )

    fun setReasonList(data: List<Pair<String, String>>) {
        val text = data.joinToString("/") { "${it.first},${it.second}" }
        val count = text.length / quantity
        prefCount = count
        (0..count).forEach { n ->
            val subText = text.substring(
                n * quantity,
                ((n + 1) * quantity).let { if (it > text.length) text.length else it }
            )
            AndroidManager.context.dataStore.set("reason$n", subText)
        }
    }

    fun getReasonMap() = (0..prefCount)
        .map { n -> AndroidManager.context.dataStore.get("reason$n", "") }
        .joinToString("") { it }
        .split("/")
        .mapNotNull {
            val data = it.split(",")
            val key = data.getOrNull(0)
            val value = data.getOrNull(1)
            if (key != null && value != null) key to value else null
        }
        .toMap()
}