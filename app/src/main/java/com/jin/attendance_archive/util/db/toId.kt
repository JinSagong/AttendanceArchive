package com.jin.attendance_archive.util.db

fun Int.toId() = "id${this.toString().padStart(6, '0')}"

fun String.toNextId() = this.substring(2).toIntOrNull()?.let { it + 1 }?.toId()