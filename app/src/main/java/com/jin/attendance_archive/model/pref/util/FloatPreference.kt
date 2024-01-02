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
class FloatPreference(
    private val preferences: DataStore<Preferences>,
    name: String,
    private val defaultValue: Float = 0f
) : ReadWriteProperty<Any, Float> {
    private val key = floatPreferencesKey(name)

    override fun getValue(thisRef: Any, property: KProperty<*>): Float {
        return runBlocking(Dispatchers.IO) {
            preferences.data
                .catch { emptyPreferences() }
                .map { it[key] ?: defaultValue }
                .firstOrNull() ?: defaultValue
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) {
        runBlocking(Dispatchers.IO) { preferences.edit { it[key] = value } }
    }
}