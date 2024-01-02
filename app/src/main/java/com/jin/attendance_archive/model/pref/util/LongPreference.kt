package com.jin.attendance_archive.model.pref.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNUSED")
class LongPreference(
    private val preferences: DataStore<Preferences>,
    name: String,
    private val defaultValue: Long = 0L
) : ReadWriteProperty<Any, Long> {
    private val key = longPreferencesKey(name)

    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return runBlocking(Dispatchers.IO) {
            preferences.data
                .catch { emptyPreferences() }
                .map { it[key] ?: defaultValue }
                .firstOrNull() ?: defaultValue
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        runBlocking(Dispatchers.IO) { preferences.edit { it[key] = value } }
    }
}