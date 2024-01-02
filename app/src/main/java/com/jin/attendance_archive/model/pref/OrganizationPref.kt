package com.jin.attendance_archive.model.pref

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.jin.attendance_archive.model.data.DataOrganization
import com.jin.attendance_archive.model.pref.util.IntegerPreference
import com.jin.attendance_archive.util.android.AndroidManager
import com.jin.attendance_archive.model.pref.util.get
import com.jin.attendance_archive.model.pref.util.set

object OrganizationPref {
    private val Context.dataStore by preferencesDataStore("organization")
    private const val quantity = 100

    private var prefCount by IntegerPreference(
        AndroidManager.context.dataStore, "organizationPrefCount", 0
    )

    fun setOrganizationList(data: List<DataOrganization>) {
        val text = data.joinToString("/") {
            "${it.id},${it.orgId},${it.region},${it.category1},${it.category2},${it.category3}"
        }
        val count = text.length / quantity
        prefCount = count
        (0..count).forEach { n ->
            val subText = text.substring(
                n * quantity,
                ((n + 1) * quantity).let { if (it > text.length) text.length else it }
            )
            AndroidManager.context.dataStore.set("organization$n",subText)
        }
    }

    fun getOrganizationList() = (0..prefCount)
        .map { n -> AndroidManager.context.dataStore.get("organization$n", "") }
        .joinToString("") { it }
        .split("/")
        .mapNotNull {
            val data = it.split(",")
            if (data.size == 6) DataOrganization(
                data[0],
                data[1],
                data[2].toIntOrNull() ?: -1,
                data[3],
                data[4],
                data[5]
            ) else null
        }
}