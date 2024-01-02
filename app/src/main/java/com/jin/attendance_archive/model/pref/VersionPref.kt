package com.jin.attendance_archive.model.pref

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.jin.attendance_archive.model.data.DataVersion
import com.jin.attendance_archive.model.pref.util.IntegerPreference
import com.jin.attendance_archive.util.android.AndroidManager

object VersionPref {
    private val Context.dataStore by preferencesDataStore("version")

    private var versionDuty by IntegerPreference(
        AndroidManager.context.dataStore, "duty", -1
    )
    private var versionOrganization by IntegerPreference(
        AndroidManager.context.dataStore, "organization", -1
    )
    private var versionPeople by IntegerPreference(
        AndroidManager.context.dataStore, "people", -1
    )

    fun setVersion(data: DataVersion?) {
        versionDuty = data?.duty ?: -1
        versionOrganization = data?.organization ?: -1
        versionPeople = data?.people ?: -1

    }

    fun getDutyVersion() = versionDuty

    fun getOrganizationVersion() = versionOrganization

    fun getPeopleVersion() = versionPeople

    fun clear() {
        versionDuty = -1
        versionOrganization = -1
        versionPeople = -1
    }
}