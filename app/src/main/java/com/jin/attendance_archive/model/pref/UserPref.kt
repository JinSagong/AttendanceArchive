package com.jin.attendance_archive.model.pref

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.jin.attendance_archive.model.data.DataUser
import com.jin.attendance_archive.model.pref.util.*
import com.jin.attendance_archive.model.pref.util.set
import com.jin.attendance_archive.util.android.AndroidManager

object UserPref {
    private val Context.dataStore by preferencesDataStore("user")
    private const val quantity = 100

    private var prefCount by IntegerPreference(
        AndroidManager.context.dataStore, "userPrefOrgCount", 0
    )
    private var id by StringPreference(AndroidManager.context.dataStore, "id", "")
    private var userId by StringPreference(AndroidManager.context.dataStore, "userId", "")
    private var name by StringPreference(AndroidManager.context.dataStore, "name", "")

    fun setUser(data: DataUser?) {
        if (data != null) {
            id = data.id
            userId = data.userId
            name = data.name
            val text = data.org.joinToString("/") { it }
            val count = text.length / quantity
            prefCount = count
            (0..count).forEach { n ->
                val subText = text.substring(
                    n * quantity,
                    ((n + 1) * quantity).let { if (it > text.length) text.length else it }
                )
                AndroidManager.context.dataStore.set("org$n", subText)
            }
        } else {
            id = ""
            userId = ""
            name = ""
            (0..prefCount).forEach { n -> AndroidManager.context.dataStore.set("org$n", "") }
            prefCount = 0
        }
    }

    fun getUser(): DataUser? {
        return if (id.isNotEmpty()) DataUser(
            id, userId, name,
            (0..prefCount)
                .map { n -> AndroidManager.context.dataStore.get("org$n", "") }
                .joinToString("") { it }
                .split("/")
                .filter { item -> item.isNotEmpty() }
        ) else null
    }
}