package com.jin.attendance_archive.statemodel

import androidx.compose.runtime.Composable
import com.jin.attendance_archive.model.data.DataVersion
import com.jin.attendance_archive.model.pref.DutyPref
import com.jin.attendance_archive.model.pref.OrganizationPref
import com.jin.attendance_archive.model.pref.PeoplePref
import com.jin.attendance_archive.model.pref.VersionPref
import com.jin.attendance_archive.model.repository.*
import com.jin.attendance_archive.model.util.*
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.ui.component.Toasty
import com.jin.attendance_archive.util.DateTimeUtil
import com.jin.attendance_archive.util.db.addTo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class DataStateModel private constructor() {
    private val repositoryVersion by lazy { RepositoryVersion() }
    private val repositoryDuty by lazy { RepositoryDuty() }
    private val repositoryOrganization by lazy { RepositoryOrganization() }
    private val repositoryPeople by lazy { RepositoryPeople() }
    private val repositoryUser by lazy { RepositoryUser() }

    private val repositoryAttendanceType by lazy { RepositoryAttendanceType() }
    private val repositoryAttendance by lazy { RepositoryAttendance() }

    private val compositeDisposable = CompositeDisposable()

    private val usePref = true

    @Composable
    private fun fetch(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            compositeDisposable.clear()
            clear()
        }
    }

    private fun checkVersion(callback: ((DataVersion) -> Unit)) {
        repositoryVersion.observeVersion()
            .subscribe { callback.invoke(it) }
            .addTo(compositeDisposable)
    }

    fun fetchData(coroutineScope: CoroutineScope, callback: (() -> Unit)? = null) {
        checkVersion { dataVersion ->
            coroutineScope.launch(Dispatchers.IO) {
                val versionDuty = VersionPref.getDutyVersion()
                val versionOrganization = VersionPref.getOrganizationVersion()
                val versionPeople = VersionPref.getPeopleVersion()
                VersionPref.setVersion(dataVersion)

                suspendCancellableCoroutine { continuation ->
                    if (!usePref || dataVersion.duty > versionDuty) repositoryDuty
                        .getDutyList()
                        .subscribe(
                            { data ->
                                val list = data.sortedBy { it.id }
                                DutyPref.setDutyList(list)
                                DutyUtil.setDutyList(list)
                                continuation.resume(Unit)
                            },
                            {
                                DutyUtil.setDutyList(DutyPref.getDutyList())
                                continuation.resume(Unit)
                            }
                        ).addTo(compositeDisposable)
                    else {
                        DutyUtil.setDutyList(DutyPref.getDutyList())
                        continuation.resume(Unit)
                    }
                }

                suspendCancellableCoroutine { continuation ->
                    if (!usePref || dataVersion.organization > versionOrganization) repositoryOrganization
                        .getOrganizationList()
                        .subscribe(
                            { data ->
                                val list = data.sortedBy { it.id }.sortedBy { it.orgId }
                                OrganizationPref.setOrganizationList(list)
                                OrganizationUtil.setOrganizationList(list)
                                val userId = UserUtil.dataUser.value?.id
                                if (!userId.isNullOrEmpty()) repositoryUser.getUserByKey(userId)
                                    .subscribe(
                                        { items ->
                                            val user = items?.firstOrNull()
                                            if (user != null) UserUtil.setUser(user)
                                            continuation.resume(Unit)
                                        },
                                        { continuation.resume(Unit) }
                                    ).addTo(compositeDisposable)
                                else continuation.resume(Unit)
                            },
                            {
                                OrganizationUtil.setOrganizationList(OrganizationPref.getOrganizationList())
                                continuation.resume(Unit)
                            }
                        ).addTo(compositeDisposable)
                    else {
                        OrganizationUtil.setOrganizationList(OrganizationPref.getOrganizationList())
                        continuation.resume(Unit)
                    }
                }

                suspendCancellableCoroutine { continuation ->
                    if (!usePref || dataVersion.people > versionPeople) repositoryPeople
                        .getPeopleList()
                        .subscribe(
                            { data ->
                                val list = data.sortedBy { it.id }
                                PeoplePref.setPeopleList(list)
                                PeopleUtil.setPeopleList(list)
                                continuation.resume(Unit)
                            },
                            {
                                PeopleUtil.setPeopleList(PeoplePref.getPeopleList())
                                continuation.resume(Unit)
                            }
                        ).addTo(compositeDisposable)
                    else {
                        PeopleUtil.setPeopleList(PeoplePref.getPeopleList())
                        continuation.resume(Unit)
                    }
                }

                callback?.invoke()
            }
        }
    }

    fun observeData(coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            val region = UserUtil.getRegion()
            suspendCancellableCoroutine { continuation ->
                repositoryAttendance.observeAttendance(region, DateTimeUtil.getWeekValue())
                    .subscribe(
                        {
                            AttendanceUtil.setAttendanceList(it)
                            if (continuation.isActive) continuation.resume(Unit)
                        },
                        { if (continuation.isActive) continuation.resume(Unit) },
                        { if (continuation.isActive) continuation.resume(Unit) }
                    )
                    .addTo(compositeDisposable)
            }
            AttendanceUtil.loaded.value = true
            repositoryAttendanceType.observeAttendanceType(region)
                .subscribe { AttendanceTypeUtil.setAttendanceTypeList(it) }
                .addTo(compositeDisposable)
        }
    }

    fun clear() {
        repositoryVersion.clear()
        repositoryAttendanceType.clear()
        repositoryAttendance.clear()
    }

    companion object {
        private var instance: DataStateModel? = null

        @Composable
        fun fetch(coroutineScope: CoroutineScope) = instance ?: run {
            val stateModel = DataStateModel()
            stateModel.fetch(coroutineScope)
            instance = stateModel
            stateModel
        }
    }
}