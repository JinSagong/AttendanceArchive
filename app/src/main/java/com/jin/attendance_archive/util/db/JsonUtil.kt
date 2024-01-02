package com.jin.attendance_archive.util.db

import com.google.gson.JsonElement

fun JsonElement.asString() = try {
    this.asString
} catch (e: UnsupportedOperationException) {
    null
}

fun JsonElement.asInt() = try {
    this.asInt
} catch (e: UnsupportedOperationException) {
    null
}

fun JsonElement.asBoolean() = try {
    this.asBoolean
} catch (e: UnsupportedOperationException) {
    null
}