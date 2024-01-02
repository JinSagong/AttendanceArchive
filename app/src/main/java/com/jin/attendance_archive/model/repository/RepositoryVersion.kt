package com.jin.attendance_archive.model.repository

import com.google.gson.internal.LinkedTreeMap
import com.jin.attendance_archive.model.data.DataVersion
import com.jin.attendance_archive.model.data.fetchDataVersion
import com.jin.attendance_archive.util.db.DataConverter
import com.jin.attendance_archive.util.db.DatabaseReference
import io.reactivex.Flowable
import io.reactivex.Single

class RepositoryVersion {
    private var refVersion: DatabaseReference? = null

    fun observeVersion(): Flowable<DataVersion> = DatabaseReference
        .child("version")
        .apply { refVersion = this }
        .observeValue()
        .map { DataConverter.fetchRealtimeData(DataVersion::class.java, it) ?: DataVersion() }

    private fun getVersion(): Single<DataVersion> = DatabaseReference
        .child("version")
        .getValue()
        .map { fetchDataVersion(it as? LinkedTreeMap<*, *>) }

    fun updateDutyVersion(): Single<Any> = getVersion()
        .flatMap {
            DatabaseReference
                .child("version")
                .child("duty")
                .setValue(it.duty + 1)
        }

    fun updateOrganizationVersion(): Single<Any> = getVersion()
        .flatMap {
            DatabaseReference
                .child("version")
                .child("organization")
                .setValue(it.organization + 1)
        }

    fun updatePeopleVersion(): Single<Any> = getVersion()
        .flatMap {
            DatabaseReference
                .child("version")
                .child("people")
                .setValue(it.people + 1)
        }

    fun clear() {
        refVersion?.stopObserving()
        refVersion = null
    }
}