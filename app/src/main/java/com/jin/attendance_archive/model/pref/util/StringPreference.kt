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
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNUSED")
class StringPreference(
    private val preferences: DataStore<Preferences>,
    name: String,
    private val defaultValue: String = ""
) : ReadWriteProperty<Any, String> {
    private val key = stringPreferencesKey(name)

    override fun getValue(thisRef: Any, property: KProperty<*>): String {
        return runBlocking(Dispatchers.IO) {
            preferences.data
                .catch { emptyPreferences() }
                .map { it[key] ?: defaultValue }
                .firstOrNull() ?: defaultValue
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        runBlocking(Dispatchers.IO) { preferences.edit { it[key] = value } }
    }
}