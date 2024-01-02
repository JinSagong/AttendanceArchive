package com.jin.attendance_archive.util

object SingleClickManager {
    private const val timeGab = 300L

    private var timeLastUpdated = 0L

    fun isAvailable(): Boolean {
        val timeCurrent = System.currentTimeMillis()
        return if (timeCurrent - timeLastUpdated > timeGab) {
            timeLastUpdated = timeCurrent
            true
        } else false
    }
}