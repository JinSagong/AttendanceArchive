package com.jin.attendance_archive.util.db

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

class SseDataUtil {
    val mapResult = hashMapOf<String, JsonPrimitive>()

    fun setMessage(path: List<String>, jsonElement: JsonElement) {
        val pathKey = path.joinToString(",") { it }
        when {
            jsonElement.isJsonNull -> removeMap(pathKey)
            jsonElement.isJsonPrimitive -> {
                val data = jsonElement.asJsonPrimitive
                if (pathKey.isNotEmpty()) mapResult[pathKey] = data
            }
            jsonElement.isJsonObject -> {
                removeMap(pathKey)
                val data = jsonElement.asJsonObject
                data.keySet().forEach { setMessage(path + it, data[it]) }
            }
        }
    }

    private fun removeMap(pathKey: String) {
        if (pathKey.isEmpty()) mapResult.clear()
        else mapResult.keys.filter { it.startsWith(pathKey) }
            .forEach { mapResult.remove(it) }
    }
}