package com.jin.attendance_archive.model.repository

import com.jin.attendance_archive.model.data.DataOrganization
import com.jin.attendance_archive.util.db.DataConverter
import com.jin.attendance_archive.util.db.DatabaseReference
import io.reactivex.Single

class RepositoryOrganization {
    fun getOrganizationList(): Single<List<DataOrganization>> = DatabaseReference
        .child("organization")
        .getValue()
        .map { DataConverter.convert(DataOrganization::class.java, it) }

    fun getLastOrganization(): Single<List<DataOrganization>> = DatabaseReference
        .child("organization")
        .orderByKey()
        .limitToLast(1)
        .getValue()
        .map { DataConverter.convert(DataOrganization::class.java, it) }

    fun setOrganization(data: DataOrganization) = DatabaseReference
        .child("organization")
        .child(data.id)
        .setValue(data)
}