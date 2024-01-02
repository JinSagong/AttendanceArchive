package com.jin.attendance_archive.model.repository

import com.jin.attendance_archive.model.data.DataUser
import com.jin.attendance_archive.util.db.DataConverter
import com.jin.attendance_archive.util.db.DatabaseReference
import io.reactivex.Single

class RepositoryUser {
    fun getUser(id: String): Single<List<DataUser>> = DatabaseReference
        .child("user")
        .orderByChild("userId")
        .equalTo(id)
        .getValue()
        .map { DataConverter.convert(DataUser::class.java, it) }

    fun getUserByKey(id: String): Single<List<DataUser>> = DatabaseReference
        .child("user")
        .orderByKey()
        .equalTo(id)
        .getValue()
        .map { DataConverter.convert(DataUser::class.java, it) }

    fun getUserList(): Single<List<DataUser>> = DatabaseReference
        .child("user")
        .getValue()
        .map { DataConverter.convert(DataUser::class.java, it) }
}