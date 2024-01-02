package com.jin.attendance_archive.model.repository

import com.jin.attendance_archive.model.data.DataAttendanceType
import com.jin.attendance_archive.util.db.DataConverter
import com.jin.attendance_archive.util.db.DatabaseReference
import io.reactivex.Flowable

class RepositoryAttendanceType {
    private var refAttendanceType: DatabaseReference? = null

    fun observeAttendanceType(region: Int): Flowable<List<DataAttendanceType>> = DatabaseReference
        .child("attendanceType")
        .apply { refAttendanceType = this }
        .child(region.toString())
        .observeValue()
        .map { DataConverter.fetchRealtimeDataList(DataAttendanceType::class.java, it) }

    fun setAttendanceType(region: Int, id: String, on: Boolean) = DatabaseReference
        .child("attendanceType")
        .child(region.toString())
        .child(id)
        .child("on")
        .setValue(on)

    fun clear() {
        refAttendanceType?.stopObserving()
        refAttendanceType = null
    }
}