package com.jin.attendance_archive

import androidx.compose.runtime.Composable
import com.jin.attendance_archive.statemodel.SwitchStateModel
import com.jin.attendance_archive.ui.component.MsgDialog
import com.jin.attendance_archive.util.compose.ComposeState

object ScreenManager {
    const val MODE_ANDROID = "android"
    const val MODE_DESKTOP = "desktop"

    // Pair<visibility, enter/exit>
    val errorScreen = ComposeState(Pair(false, false))
    val signInScreen = ComposeState(Pair(false, false))
    val mainScreen = ComposeState(Pair(false, false))
    val switchScreen = ComposeState(Pair(false, false))
    val checkListScreen = ComposeState(Pair(false, false))
    val checkScreen = ComposeState(Pair(false, false))
    val checkFruitScreen = ComposeState(Pair(false, false))
    val createFruitScreen = ComposeState(Pair(false, false))
    val fruitPeopleScreen = ComposeState(Pair(false, false))
    val managementScreen = ComposeState(Pair(false, false))
    val filingWeeklyScreen = ComposeState(Pair(false, false))
    val filingYearlyScreen = ComposeState(Pair(false, false))
    val filingFruitScreen = ComposeState(Pair(false, false))
    val managePeopleScreen = ComposeState(Pair(false, false))
    val peopleEditorScreen = ComposeState(Pair(false, false))
    val peopleOrgEditorScreen = ComposeState(Pair(false, false))
    val manageUserScreen = ComposeState(Pair(false, false))
    val logScreen = ComposeState(Pair(false, false))

    private var switchStateModel: SwitchStateModel? = null

    fun setStateModels(switchStateModel: SwitchStateModel) {
        this.switchStateModel = switchStateModel
    }

    @Composable
    fun remember() {
        errorScreen.remember()
        signInScreen.remember()
        mainScreen.remember()
        switchScreen.remember()
        checkListScreen.remember()
        checkScreen.remember()
        checkFruitScreen.remember()
        createFruitScreen.remember()
        fruitPeopleScreen.remember()
        managementScreen.remember()
        filingWeeklyScreen.remember()
        filingYearlyScreen.remember()
        filingFruitScreen.remember()
        managePeopleScreen.remember()
        peopleEditorScreen.remember()
        peopleOrgEditorScreen.remember()
        manageUserScreen.remember()
        logScreen.remember()
    }

    fun clearScreens() {
        errorScreen.value = Pair(false, false)
        signInScreen.value = Pair(false, false)
        mainScreen.value = Pair(false, false)
        switchScreen.value = Pair(false, false)
        checkListScreen.value = Pair(false, false)
        checkScreen.value = Pair(false, false)
        checkFruitScreen.value = Pair(false, false)
        createFruitScreen.value = Pair(false, false)
        fruitPeopleScreen.value = Pair(false, false)
        managementScreen.value = Pair(false, false)
        filingWeeklyScreen.value = Pair(false, false)
        filingYearlyScreen.value = Pair(false, false)
        filingFruitScreen.value = Pair(false, false)
        managePeopleScreen.value = Pair(false, false)
        peopleEditorScreen.value = Pair(false, false)
        peopleOrgEditorScreen.value = Pair(false, false)
        manageUserScreen.value = Pair(false, false)
        logScreen.value = Pair(false, false)
    }

    fun onBackPressed(superCallback: (() -> Unit)? = null) {
        when {
            errorScreen.value.first -> superCallback?.invoke()
            MsgDialog.isOpened -> MsgDialog.cancel()
            switchScreen.value.first -> switchStateModel?.cancelSwitch() ?: {
                switchScreen.value = Pair(false, false)
            }
            checkListScreen.value.first -> {
                checkListScreen.value = Pair(false, false)
                mainScreen.value = Pair(true, false)
            }
            checkScreen.value.first -> {
                checkScreen.value = Pair(false, false)
                checkListScreen.value = Pair(true, false)
            }
            checkFruitScreen.value.first -> {
                checkFruitScreen.value = Pair(false, false)
                checkListScreen.value = Pair(true, false)
            }
            createFruitScreen.value.first -> {
                createFruitScreen.value = Pair(false, false)
                checkFruitScreen.value = Pair(true, false)
            }
            fruitPeopleScreen.value.first -> {
                fruitPeopleScreen.value = Pair(false, false)
                checkFruitScreen.value = Pair(true, false)
            }
            managementScreen.value.first -> {
                managementScreen.value = Pair(false, false)
                mainScreen.value = Pair(true, false)
            }
            filingWeeklyScreen.value.first -> {
                filingWeeklyScreen.value = Pair(false, false)
                managementScreen.value = Pair(true, false)
            }
            filingYearlyScreen.value.first -> {
                filingYearlyScreen.value = Pair(false, false)
                managementScreen.value = Pair(true, false)
            }
            filingFruitScreen.value.first -> {
                filingFruitScreen.value = Pair(false, false)
                managementScreen.value = Pair(true, false)
            }
            managePeopleScreen.value.first -> {
                managePeopleScreen.value = Pair(false, false)
                managementScreen.value = Pair(true, false)
            }
            peopleEditorScreen.value.first -> {
                peopleEditorScreen.value = Pair(false, false)
                managePeopleScreen.value = Pair(true, false)
            }
            peopleOrgEditorScreen.value.first -> {
                peopleOrgEditorScreen.value = Pair(false, false)
                peopleEditorScreen.value = Pair(true, false)
            }
            manageUserScreen.value.first -> {
                manageUserScreen.value = Pair(false, false)
                managementScreen.value = Pair(true, false)
            }
            logScreen.value.first -> {
                logScreen.value = Pair(false, false)
                managementScreen.value = Pair(true, false)
            }
            signInScreen.value.first -> superCallback?.invoke()
            mainScreen.value.first -> superCallback?.invoke()
            else -> superCallback?.invoke()
        }
    }

    fun openErrorScreen() {
        errorScreen.value = Pair(true, false)
    }

    fun openSignInScreen() {
        mainScreen.value = Pair(false, false)
        signInScreen.value = Pair(true, false)
    }

    fun openMainScreen() {
        signInScreen.value = Pair(false, true)
        mainScreen.value = Pair(true, true)
    }

    fun openCheckListScreen() {
        mainScreen.value = Pair(false, true)
        checkListScreen.value = Pair(true, true)
    }

    fun openCheckScreen() {
        checkListScreen.value = Pair(false, true)
        checkScreen.value = Pair(true, true)
    }

    fun openCheckFruitScreen() {
        checkListScreen.value = Pair(false, true)
        checkFruitScreen.value = Pair(true, true)
    }

    fun openCreateFruitScreen() {
        checkFruitScreen.value = Pair(false, true)
        createFruitScreen.value = Pair(true, true)
    }

    fun openFruitPeopleScreen() {
        checkFruitScreen.value = Pair(false, true)
        fruitPeopleScreen.value = Pair(true, true)
    }

    fun openManagementScreen() {
        mainScreen.value = Pair(false, true)
        managementScreen.value = Pair(true, true)
    }

    fun openFilingWeeklyScreen() {
        managementScreen.value = Pair(false, true)
        filingWeeklyScreen.value = Pair(true, true)
    }

    fun openFilingYearlyScreen() {
        managementScreen.value = Pair(false, true)
        filingYearlyScreen.value = Pair(true, true)
    }

    fun openFilingFruitScreen() {
        managementScreen.value = Pair(false, true)
        filingFruitScreen.value = Pair(true, true)
    }

    fun openManagePeopleScreen() {
        managementScreen.value = Pair(false, true)
        managePeopleScreen.value = Pair(true, true)
    }

    fun openPeopleEditorScreen() {
        managePeopleScreen.value = Pair(false, true)
        peopleEditorScreen.value = Pair(true, true)
    }

    fun openPeopleOrgEditorScreen() {
        peopleEditorScreen.value = Pair(false, true)
        peopleOrgEditorScreen.value = Pair(true, true)
    }

    fun openManageUserScreen() {
        managementScreen.value = Pair(false, true)
        manageUserScreen.value = Pair(true, true)
    }

    fun openLogScreen() {
        managementScreen.value = Pair(false, true)
        logScreen.value = Pair(true, true)
    }
}