package com.jin.attendance_archive.model.pref.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

internal fun DataStore<Preferences>.set(key: String, value: String) {
    runBlocking(Dispatchers.IO) { this@set.edit { it[stringPreferencesKey(key)] = value } }
}

internal fun DataStore<Preferences>.get(key: String, defaultValue: String) =
    runBlocking(Dispatchers.IO) {
        this@get.data
            .catch { emptyPreferences() }
            .map { it[stringPreferencesKey(key)] ?: defaultValue }
            .firstOrNull() ?: defaultValue
    }