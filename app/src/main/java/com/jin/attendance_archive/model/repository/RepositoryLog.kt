package com.jin.attendance_archive.model.repository

import com.jin.attendance_archive.model.data.DataLog
import com.jin.attendance_archive.util.db.DataConverter
import com.jin.attendance_archive.util.db.DatabaseReference
import io.reactivex.Single

class RepositoryLog {
    fun setLog(data: DataLog) = DatabaseReference
        .child("log")
        .child(data.id)
        .setValue(data)

    fun getLogList(): Single<List<DataLog>> = DatabaseReference
        .child("log")
        .orderByKey()
        .limitToLast(500)
        .getValue()
        .map { DataConverter.convert(DataLog::class.java, it) }
}