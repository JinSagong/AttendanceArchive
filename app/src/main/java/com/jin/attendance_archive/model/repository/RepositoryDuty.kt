package com.jin.attendance_archive.model.repository

import com.jin.attendance_archive.model.data.DataDuty
import com.jin.attendance_archive.util.db.DataConverter
import com.jin.attendance_archive.util.db.DatabaseReference
import io.reactivex.Single

class RepositoryDuty {
    fun getDutyList(): Single<List<DataDuty>> = DatabaseReference
        .child("duty")
        .getValue()
        .map { DataConverter.convert(DataDuty::class.java, it) }
}