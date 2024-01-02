package com.jin.attendance_archive.model.repository

import com.jin.attendance_archive.model.data.DataPeople
import com.jin.attendance_archive.util.db.DataConverter
import com.jin.attendance_archive.util.db.DatabaseReference
import io.reactivex.Single

class RepositoryPeople {
    fun getPeopleList(): Single<List<DataPeople>> = DatabaseReference
        .child("people")
        .getValue()
        .map { DataConverter.convert(DataPeople::class.java, it) }

    fun getLastPeople(): Single<List<DataPeople>> = DatabaseReference
        .child("people")
        .orderByKey()
        .limitToLast(1)
        .getValue()
        .map { DataConverter.convert(DataPeople::class.java, it) }

    fun setPeople(data: DataPeople) = DatabaseReference
        .child("people")
        .child(data.id)
        .setValue(data.toImpl())
}