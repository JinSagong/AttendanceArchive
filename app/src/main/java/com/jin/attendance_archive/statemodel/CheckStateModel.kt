package com.jin.attendance_archive.statemodel

import androidx.compose.runtime.*
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.data.*
import com.jin.attendance_archive.model.pref.ReasonPref
import com.jin.attendance_archive.model.repository.RepositoryAttendance
import com.jin.attendance_archive.model.util.AttendanceUtil
import com.jin.attendance_archive.model.util.OrganizationUtil
import com.jin.attendance_archive.model.util.PeopleUtil
import com.jin.attendance_archive.ui.component.MsgDialog
import com.jin.attendance_archive.ui.component.Toasty
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.DateTimeUtil
import com.jin.attendance_archive.util.compose.ComposeState
import com.jin.attendance_archive.util.db.addTo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job

class CheckStateModel private constructor() {
    private val repositoryAttendance by lazy { RepositoryAttendance() }

    private val compositeDisposable = CompositeDisposable()

    var attendanceType: DataAttendanceType? = null
        private set
    var listOrganization: List<DataOrganization> = emptyList()
        private set
    var listPeople =
        ComposeState<List<Pair<String, List<Triple<DataPeople, MutableState<Int>, MutableState<String>>>>>>(
            emptyList()
        )
        private set

    var mapFruitPeople = mutableStateMapOf<String, Pair<DataPeople, DataAttendanceItem>>()
        private set
    var mapFruit = mutableStateMapOf<String, DataFruit>()
        private set

    val fruitPeopleSelected = ComposeState<DataPeople?>(null)
    val fruitReason = ComposeState("")
    val fruitId = ComposeState("")
    val fruitTitle = ComposeState("")
    val fruitConfirm = ComposeState("")
    val fruitType = ComposeState(0)
    val fruitPeople = ComposeState("")
    val fruitBeliever = ComposeState("")
    val fruitTeacher = ComposeState("")
    val fruitAge = ComposeState(-1)
    val fruitPhone = ComposeState("")
    val fruitRemeet = ComposeState(false)
    val fruitFrequency = ComposeState(-1)
    val fruitPlace = ComposeState("")

    @Composable
    private fun fetch(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            compositeDisposable.clear()
        }

        listPeople = remember { listPeople }
        mapFruitPeople = remember { mapFruitPeople }
        mapFruit = remember { mapFruit }

        fruitPeopleSelected.remember()
        fruitReason.remember()
        fruitId.remember()
        fruitTitle.remember()
        fruitConfirm.remember()
        fruitType.remember()
        fruitPeople.remember()
        fruitBeliever.remember()
        fruitTeacher.remember()
        fruitAge.remember()
        fruitPhone.remember()
        fruitRemeet.remember()
        fruitFrequency.remember()
        fruitPlace.remember()
    }

    fun enterCheckScreen(
        attendanceType: DataAttendanceType?,
        organization: List<DataOrganization>
    ) {
        this.attendanceType = attendanceType
        listOrganization = organization
        val attendance = attendanceType?.id?.let { type ->
            organization.firstOrNull()?.orgId?.let { orgId ->
                AttendanceUtil.mapAttendance.toMap()[type]?.firstOrNull { it.org == orgId }
            }
        }

        val reasonSavedMap = ReasonPref.getReasonMap()
        listPeople.value = organization.sortedBy { it.id }.mapNotNull {
            PeopleUtil.mapPeopleByOrg[it.id]?.map { people ->
                val item = attendance?.items?.get(people.id)
                val checkedState = mutableStateOf(item?.checked ?: 0)
                val reasonState = mutableStateOf(
                    item?.reason.orEmpty().ifEmpty { reasonSavedMap[people.id].orEmpty() }
                )
                Triple(people, checkedState, reasonState)
            }?.sortedBy { people -> people.first.name }?.let { people -> it.category3 to people }
        }

        mapFruitPeople.clear()
        mapFruitPeople.putAll(attendance?.items?.values
            ?.filter { item -> item.checked == 1 || item.checked == 2 }
            ?.mapNotNull { item ->
                PeopleUtil.mapPeople[item.id]?.let { item2 -> Pair(item2, item) }
            }
            ?.associateBy { it.first.id }
            .orEmpty()
        )
        mapFruit.clear()
        mapFruit.putAll(attendance?.fruits.orEmpty())

        if (attendanceType?.hasFruit != true) ScreenManager.openCheckScreen() else ScreenManager.openCheckFruitScreen()
    }

    fun openSearchPeople(type: Int) {
        fruitTitle.value = when (type) {
            1 -> Strings.hasFruitAddAttendance
            2 -> Strings.hasFruitAddDedication
            else -> ""
        }
        fruitType.value = type
        fruitPeopleSelected.value = null
        fruitReason.value = ""
        ScreenManager.openFruitPeopleScreen()
    }

    fun selectFruitPeople(data: DataPeople) {
        fruitPeopleSelected.value = data
    }

    fun addFruitPeople(data: DataPeople) {
        if (fruitType.value == 2 && fruitReason.value.isEmpty()) {
            Toasty.show(Strings.hasFruitDedicationEmpty)
            return
        }
        if (mapFruitPeople.contains(data.id)) Toasty.show(Strings.hasFruitPeopleDuplicated)
        mapFruitPeople[data.id] =
            Pair(data, DataAttendanceItem(data.id, fruitType.value, fruitReason.value))
        ScreenManager.onBackPressed()
    }

    fun removeFruitPeople(id: String) {
        mapFruitPeople.remove(id)
    }

    fun openCreateFruit(type: Int) {
        fruitId.value = ""
        val data = DataFruit(type = type)
        fruitTitle.value = when (type) {
            0 -> Strings.hasFruitAddFruit1
            1 -> Strings.hasFruitAddFruit2
            else -> ""
        }
        fruitConfirm.value = Strings.hasFruitAdd
        fruitType.value = data.type
        fruitPeople.value = data.people
        fruitBeliever.value = data.believer
        fruitTeacher.value = data.teacher
        fruitAge.value = data.age
        fruitPhone.value = data.phone
        fruitRemeet.value = data.remeet
        fruitFrequency.value = data.frequency
        fruitPlace.value = data.place
        ScreenManager.openCreateFruitScreen()
    }

    fun openEditFruit(data: DataFruit) {
        fruitId.value = data.id
        fruitTitle.value = when (data.type) {
            0 -> Strings.hasFruitEditFruit1
            1 -> Strings.hasFruitEditFruit2
            else -> ""
        }
        fruitConfirm.value = Strings.hasFruitEdit
        fruitType.value = data.type
        fruitPeople.value = data.people
        fruitBeliever.value = data.believer
        fruitTeacher.value = data.teacher
        fruitAge.value = data.age
        fruitPhone.value = data.phone
        fruitRemeet.value = data.remeet
        fruitFrequency.value = data.frequency
        fruitPlace.value = data.place
        ScreenManager.openCreateFruitScreen()
    }

    fun addFruit() {
        when {
            fruitBeliever.value.isEmpty() -> Toasty.show(Strings.hasFruitBelieverEmpty)
            fruitPeople.value.isEmpty() -> Toasty.show(Strings.hasFruitPreacherEmpty)
            else -> {
                val id = "${fruitType.value}&${fruitPeople.value}&${fruitBeliever.value}"
                if (id in mapFruit.keys) Toasty.show(Strings.hasFruitDuplicated)
                else {
                    if (fruitId.value.isNotEmpty()) mapFruit.remove(fruitId.value)
                    val data = DataFruit(
                        id,
                        fruitType.value,
                        fruitPeople.value,
                        fruitBeliever.value,
                        fruitTeacher.value,
                        fruitAge.value,
                        fruitPhone.value,
                        fruitRemeet.value,
                        fruitFrequency.value,
                        fruitPlace.value
                    )
                    mapFruit[id] = data
                    ScreenManager.onBackPressed()
                }
            }
        }
    }

    fun removeFruit() {
        MsgDialog.withTwoBtn()
            .setMessage(Strings.hasFruitDeleteMsg)
            .setFinishMessage(Strings.hasFruitDeleteCompleted)
            .onConfirm {
                if (fruitId.value.isNotEmpty()) mapFruit.remove(fruitId.value)
                ScreenManager.createFruitScreen.value = Pair(false, false)
                ScreenManager.checkFruitScreen.value = Pair(true, false)
                it.invoke()
            }
            .show()
    }

    fun check(callback: (() -> Unit)) {
        val week = DateTimeUtil.getWeekValue()
        val attendanceTypeId = attendanceType?.id
        val org = listOrganization.firstOrNull()?.orgId
        if (attendanceTypeId == null || org == null) {
            Toasty.show(Strings.attendanceDoError)
            return
        }

        val data = DataAttendance(
            "${attendanceTypeId}n${week}n$org", week, attendanceTypeId, org,
            listPeople.value.flatMap { item -> item.second }.associate {
                it.first.id to DataAttendanceItem(
                    it.first.id,
                    it.second.value,
                    if (it.second.value == 1) "" else it.third.value
                )
            }.also { ReasonPref.setReasonList(it.values.map { item -> item.id to item.reason }) }
                    + mapFruitPeople.mapValues { item -> item.value.second },
            mapFruit.toMap()
        )

        repositoryAttendance.setAttendance(OrganizationUtil.getRegion(listOrganization), data)
            .subscribe(
                {
                    Toasty.show(Strings.attendanceDoCompleted)
                    callback.invoke()
                },
                { Toasty.show(Strings.attendanceDoError) }
            ).addTo(compositeDisposable)
    }

    companion object {
        private var instance: CheckStateModel? = null

        @Composable
        fun fetch(coroutineScope: CoroutineScope) = instance ?: run {
            val stateModel = CheckStateModel()
            stateModel.fetch(coroutineScope)
            instance = stateModel
            stateModel
        }
    }
}