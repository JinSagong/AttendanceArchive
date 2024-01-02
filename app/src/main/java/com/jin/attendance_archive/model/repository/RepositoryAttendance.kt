package com.jin.attendance_archive.model.repository

import com.jin.attendance_archive.model.data.DataAttendance
import com.jin.attendance_archive.util.db.DataConverter
import com.jin.attendance_archive.util.db.DatabaseReference
import io.reactivex.Flowable
import io.reactivex.Single

class RepositoryAttendance {
    private var refAttendance: DatabaseReference? = null

    fun observeAttendance(region: Int, week: Int): Flowable<List<DataAttendance>> =
        DatabaseReference
            .child("attendance")
            .apply { refAttendance = this }
            .child(region.toString())
            .orderByChild("week")
            .equalTo(week)
            .observeValue()
            .map { DataConverter.fetchRealtimeDataList(DataAttendance::class.java, it) }

    fun getAttendance(region: Int, week: Int): Single<List<DataAttendance>> = DatabaseReference
        .child("attendance")
        .child(region.toString())
        .orderByChild("week")
        .equalTo(week)
        .getValue()
        .map { DataConverter.convert(DataAttendance::class.java, it) }

    fun getAttendance(region: Int, startWeek: Int, endWeek: Int): Single<List<DataAttendance>> =
        DatabaseReference
            .child("attendance")
            .child(region.toString())
            .orderByChild("week")
            .startAt(startWeek)
            .endAt(endWeek)
            .getValue()
            .map { DataConverter.convert(DataAttendance::class.java, it) }

    fun getAttendance(
        region: Int,
        attendanceType: String,
        week: Int
    ): Single<List<DataAttendance>> = DatabaseReference
        .child("attendance")
        .child(region.toString())
        .orderByKey()
        .startAt("${attendanceType}n$week")
        .endAt("${attendanceType}n$week\\uf8ff")
        .getValue()
        .map { DataConverter.convert(DataAttendance::class.java, it) }

    fun getAttendance(
        region: Int,
        attendanceType: String,
        startWeek: Int,
        endWeek: Int
    ): Single<List<DataAttendance>> = DatabaseReference
        .child("attendance")
        .child(region.toString())
        .orderByKey()
        .startAt("${attendanceType}n$startWeek")
        .endAt("${attendanceType}n$endWeek\\uf8ff")
        .getValue()
        .map { DataConverter.convert(DataAttendance::class.java, it) }

    fun setAttendance(region: Int, data: DataAttendance) = DatabaseReference
        .child("attendance")
        .child(region.toString())
        .child(data.id)
        .setValue(data)

    fun clear() {
        refAttendance?.stopObserving()
        refAttendance = null
    }
}