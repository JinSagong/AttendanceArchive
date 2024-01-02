package com.jin.attendance_archive.statemodel

import androidx.compose.runtime.Composable
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.data.DataOrganization
import com.jin.attendance_archive.model.data.DataPeople
import com.jin.attendance_archive.model.repository.RepositoryLog
import com.jin.attendance_archive.model.repository.RepositoryOrganization
import com.jin.attendance_archive.model.repository.RepositoryPeople
import com.jin.attendance_archive.model.repository.RepositoryVersion
import com.jin.attendance_archive.model.util.LogUtil
import com.jin.attendance_archive.model.util.OrganizationUtil
import com.jin.attendance_archive.model.util.PeopleUtil
import com.jin.attendance_archive.model.util.UserUtil
import com.jin.attendance_archive.ui.component.MsgDialog
import com.jin.attendance_archive.ui.component.Toasty
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.compose.ComposeState
import com.jin.attendance_archive.util.db.addTo
import com.jin.attendance_archive.util.db.toNextId
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class PeopleStateModel {
    private val repositoryPeople by lazy { RepositoryPeople() }
    private val repositoryOrganization by lazy { RepositoryOrganization() }
    private val repositoryVersion by lazy { RepositoryVersion() }
    private val repositoryLog by lazy { RepositoryLog() }

    private val compositeDisposable = CompositeDisposable()

    val searchQuery = ComposeState("")

    private var prevData: DataPeople? = null
    val peopleId = ComposeState<String?>(null)
    val peopleNamePrev = ComposeState("")
    val peopleName = ComposeState("")
    val peopleDuty = ComposeState("")
    val peopleOrg = ComposeState(emptyList<DataOrganization>())
    val peopleActivated = ComposeState(true)
    private var peopleOrgIdx = 0

    private var peopleOrgId: String? = null
    val peopleOrgCategory1List = ComposeState(emptyList<String>())
    val peopleOrgCategory2List = ComposeState(emptyList<String>())
    val peopleOrgCategory3List = ComposeState(emptyList<String>())
    val peopleOrgRegion = ComposeState(UserUtil.getRegion())
    val peopleOrgCategory1 = ComposeState("")
    val peopleOrgCategory2 = ComposeState("")
    val peopleOrgCategory3 = ComposeState("")
    val peopleOrgCustomCategory3 = ComposeState<String?>(null)
    val peopleOrgResult = ComposeState<DataOrganization?>(null)

    @Composable
    private fun fetch(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            compositeDisposable.clear()
        }

        searchQuery.remember()
        peopleId.remember()
        peopleNamePrev.remember()
        peopleName.remember()
        peopleDuty.remember()
        peopleOrg.remember()
        peopleActivated.remember()

        peopleOrgCategory1List.remember()
        peopleOrgCategory2List.remember()
        peopleOrgCategory3List.remember()
        peopleOrgRegion.remember()
        peopleOrgCategory1.remember()
        peopleOrgCategory2.remember()
        peopleOrgCategory3.remember()
        peopleOrgCustomCategory3.remember()
        peopleOrgResult.remember()
    }

    fun initSearchQuery() {
        searchQuery.value = ""
    }

    fun searchPeopleResult(onlyActivated: Boolean = true) =
        if (searchQuery.value.isNotEmpty()) PeopleUtil.mapPeople.values
            .filter { it.name.contains(searchQuery.value) && (!onlyActivated || it.activated) }
            .sortedBy { it.name }
            .sortedBy { !it.name.startsWith(searchQuery.value) }
        else emptyList()

    fun moveToEditor(data: DataPeople?) {
        prevData = data
        peopleId.value = data?.id
        peopleNamePrev.value = data?.name ?: searchQuery.value
        peopleName.value = data?.name ?: searchQuery.value
        peopleDuty.value = data?.duty.orEmpty()
        peopleOrg.value =
            data?.org?.mapNotNull { item -> OrganizationUtil.mapOrganization[item] }.orEmpty()
        peopleActivated.value = data?.activated ?: true
        peopleOrgIdx = 0
        ScreenManager.openPeopleEditorScreen()
    }

    fun moveToOrgEditor(data: DataOrganization?) {
        peopleOrgIdx++
        peopleOrgId = data?.id
        peopleOrgRegion.value = data?.region ?: 1
        peopleOrgCategory1.value = data?.category1.orEmpty()
        peopleOrgCategory2.value = data?.category2.orEmpty()
        peopleOrgCategory3.value = data?.category3.orEmpty()
        updateOrgState()
        ScreenManager.openPeopleOrgEditorScreen()
    }

    fun checkOrg(data: DataOrganization) {
        peopleOrg.value = peopleOrg.value.sortedBy { item -> item != data }
    }

    fun deleteOrg(data: DataOrganization) {
        MsgDialog.withTwoBtn()
            .setMessage(Strings.managePeopleOrgDeleteMsg)
            .setFinishMessage(Strings.managePeopleOrgDeleteCompleted)
            .onConfirm {
                peopleOrg.value =
                    peopleOrg.value.toCollection(ArrayList()).apply { remove(data) }.toList()
                it.invoke()
            }
            .show()
    }

    fun updateOrgState(forUpdatingCustomCategory: Boolean = false) {
        var orgList = OrganizationUtil.mapOrganization.values.sortedBy { item -> item.id }
        orgList = orgList.filter { item -> item.region == peopleOrgRegion.value }
        peopleOrgCategory1List.value = orgList.groupBy { item -> item.category1 }.keys.toList()
        orgList = orgList.filter { item -> item.category1 == peopleOrgCategory1.value }
        peopleOrgCategory2List.value = orgList.groupBy { item -> item.category2 }.keys.toList()
        orgList = orgList.filter { item -> item.category2 == peopleOrgCategory2.value }
        peopleOrgCategory3List.value =
            orgList.groupBy { item -> item.category3 }.keys.filter { item -> item.isNotEmpty() }
        if (!forUpdatingCustomCategory) peopleOrgCustomCategory3.value = if (
            (peopleOrgRegion.value == 1 && peopleOrgCategory1.value == "기관" && peopleOrgCategory2.value == "고등부" && peopleOrgCategory3.value == "") ||
            (peopleOrgRegion.value == 1 && peopleOrgCategory1.value == "기관" && peopleOrgCategory2.value == "중등부" && peopleOrgCategory3.value == "") ||
            (peopleOrgRegion.value == 1 && peopleOrgCategory1.value == "기관" && peopleOrgCategory2.value == "초등부" && peopleOrgCategory3.value == "") ||
            (peopleOrgRegion.value == 1 && peopleOrgCategory1.value == "기관" && peopleOrgCategory2.value == "유년부" && peopleOrgCategory3.value == "")
        ) "" else null
        peopleOrgResult.value = if (peopleOrgCustomCategory3.value.isNullOrEmpty())
            orgList.firstOrNull { item -> item.category3 == peopleOrgCategory3.value }
        else orgList.firstOrNull { item -> item.category3 == peopleOrgCustomCategory3.value }
            ?: peopleOrg.value.firstOrNull { item -> item.category1 == peopleOrgCategory1.value && item.category2 == peopleOrgCategory2.value && item.category3 == peopleOrgCustomCategory3.value }
            ?: orgList.firstOrNull()?.copy(
                id = "temp$peopleOrgIdx",
                category3 = peopleOrgCustomCategory3.value.orEmpty()
            )
    }

    fun updateOrg() {
        if (peopleOrgResult.value == null) return
        if (peopleOrgResult.value!!.id in peopleOrg.value.map { item -> item.id }) {
            Toasty.show(Strings.managePeopleOrgDuplicated)
        } else {
            Toasty.cancel()
            val idxMatched =
                peopleOrg.value.indexOfFirst { item -> item.id == peopleOrgId }
            if (idxMatched == -1) peopleOrg.value += peopleOrgResult.value!!
            else {
                val tempArrayList = arrayListOf<DataOrganization>()
                peopleOrg.value.forEachIndexed { idx, item ->
                    tempArrayList.add(if (idx != idxMatched) item else peopleOrgResult.value!!)
                }
                peopleOrg.value = tempArrayList.toList()
            }
            ScreenManager.onBackPressed()
        }
    }

    fun update(coroutineScope: CoroutineScope) {
        when {
            peopleName.value.isEmpty() -> Toasty.show(Strings.managePeopleNameEmpty)
            peopleDuty.value.isEmpty() -> Toasty.show(Strings.managePeopleDutyEmpty)
            else -> {
                Toasty.cancel()
                MsgDialog.withTwoBtn()
                    .setMessage(if (peopleId.value == null) Strings.managePeopleCreateMsg else Strings.managePeopleEditMsg)
                    .onConfirm {
                        coroutineScope.launch(Dispatchers.IO) {
                            update()
                            it.invoke()
                        }
                    }
                    .show()
            }
        }
    }

    private suspend fun update() {
        val peopleWithSameName =
            PeopleUtil.mapPeople.values.firstOrNull { item -> item.name == peopleName.value }
        if (peopleWithSameName == null || peopleName.value == peopleNamePrev.value) {
            val id = suspendCancellableCoroutine { continuation ->
                if (peopleId.value == null) {
                    repositoryPeople.getLastPeople()
                        .subscribe(
                            { item -> continuation.resume(item.maxByOrNull { item2 -> item2.id }?.id?.toNextId()) },
                            { continuation.resume(null) }
                        ).addTo(compositeDisposable)
                } else continuation.resume(peopleId.value)
            }
            if (!id.isNullOrEmpty()) {
                val tempOrgMap = HashMap<String, DataOrganization>()
                var isOrgValid = true
                run {
                    peopleOrg.value.filter { item -> item.id.startsWith("temp") }
                        .forEach { item ->
                            isOrgValid = suspendCancellableCoroutine { continuation ->
                                var newOrg: DataOrganization? = null
                                repositoryOrganization.getLastOrganization()
                                    .flatMap {
                                        val orgId = it.firstOrNull()?.id?.toNextId() ?: "id000000"
                                        newOrg = item.copy(id = orgId)
                                        repositoryOrganization.setOrganization(newOrg!!)
                                    }
                                    .doOnSuccess {
                                        if (newOrg != null) tempOrgMap[item.id] = newOrg!!
                                    }
                                    .flatMap { repositoryVersion.updateOrganizationVersion() }
                                    .flatMap {
                                        repositoryLog.setLog(
                                            LogUtil.getDataLog("null", newOrg!!.toString())
                                        )
                                    }
                                    .subscribe(
                                        { continuation.resume(true) },
                                        { continuation.resume(false) }
                                    ).addTo(compositeDisposable)
                            }
                            if (!isOrgValid) return@run
                        }
                }
                val tempArrayList = arrayListOf<DataOrganization>()
                peopleOrg.value.forEach { item ->
                    val newOrg = tempOrgMap[item.id]
                    tempArrayList.add(newOrg ?: item)
                    if (newOrg != null) OrganizationUtil.update(newOrg)
                }
                peopleOrg.value = tempArrayList.toList()
                if (isOrgValid) {
                    val data = DataPeople(
                        id = id,
                        name = peopleName.value,
                        duty = peopleDuty.value,
                        org = peopleOrg.value.map { item -> item.id },
                        activated = peopleActivated.value
                    )
                    val result = suspendCancellableCoroutine { continuation ->
                        repositoryPeople.setPeople(data)
                            .flatMap { repositoryVersion.updatePeopleVersion() }
                            .flatMap {
                                repositoryLog.setLog(
                                    LogUtil.getDataLog(
                                        prevData?.toString() ?: "null",
                                        data.toString()
                                    )
                                )
                            }
                            .subscribe(
                                { continuation.resume(true) },
                                { continuation.resume(false) }
                            ).addTo(compositeDisposable)
                    }
                    withContext(Dispatchers.Default) {
                        if (result) {
                            PeopleUtil.update(data)
                            ScreenManager.peopleEditorScreen.value = Pair(false, false)
                            ScreenManager.managePeopleScreen.value = Pair(true, false)
                            Toasty.show(if (peopleId.value == null) Strings.managePeopleCreateCompleted else Strings.managePeopleEditCompleted)
                        } else Toasty.show(if (peopleId.value == null) Strings.managePeopleCreateFailure else Strings.managePeopleEditFailure)
                    }
                } else withContext(Dispatchers.Default) {
                    Toasty.show(if (peopleId.value == null) Strings.managePeopleCreateFailure else Strings.managePeopleEditFailure)
                }
            } else withContext(Dispatchers.Default) {
                Toasty.show(if (peopleId.value == null) Strings.managePeopleCreateFailure else Strings.managePeopleEditFailure)
            }
        } else withContext(Dispatchers.Default) {
            Toasty.show(Strings.managePeopleSameNameFailure)
        }
    }

    companion object {
        private var instance: PeopleStateModel? = null

        @Composable
        fun fetch(coroutineScope: CoroutineScope) = instance ?: run {
            val stateModel = PeopleStateModel()
            stateModel.fetch(coroutineScope)
            instance = stateModel
            stateModel
        }
    }
}