package com.jin.attendance_archive

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.jin.attendance_archive.model.util.*
import com.jin.attendance_archive.statemodel.*
import com.jin.attendance_archive.ui.component.MsgDialog
import com.jin.attendance_archive.ui.component.Toasty
import com.jin.attendance_archive.res.MyTheme
import com.jin.attendance_archive.ui.screen.*
import com.jin.attendance_archive.ui.screen.fruit.CheckFruitScreen
import com.jin.attendance_archive.ui.screen.fruit.CreateFruitScreen
import com.jin.attendance_archive.ui.screen.fruit.FruitPeopleScreen
import com.jin.attendance_archive.ui.screen.management.*
import com.jin.attendance_archive.ui.screen.management.filing.FilingFruitScreen
import com.jin.attendance_archive.ui.screen.management.filing.FilingWeeklyScreen
import com.jin.attendance_archive.ui.screen.management.filing.FilingYearlyScreen
import com.jin.attendance_archive.ui.screen.management.people.ManagePeopleScreen
import com.jin.attendance_archive.ui.screen.management.people.PeopleEditorScreen
import com.jin.attendance_archive.ui.screen.management.people.PeopleOrgEditorScreen

var platformMode = ""

@Composable
fun Launcher(mode: String, colorScheme: ColorScheme? = null) {
    platformMode = mode

    val coroutineScope = rememberCoroutineScope()

    /** Fetch Data */

    UserUtil.remember()
    DutyUtil.remember()
    OrganizationUtil.remember()
    PeopleUtil.remember()
    AttendanceTypeUtil.remember()
    AttendanceUtil.remember()

    val dataStateModel = DataStateModel.fetch(coroutineScope)
    val signStateModel = SignStateModel.fetch(coroutineScope)
    val switchStateModel = SwitchStateModel.fetch(coroutineScope)
    val checkListStateModel = CheckListStateModel.fetch()
    val checkStateModel = CheckStateModel.fetch(coroutineScope)
    val filingStateModel = FilingStateModel.fetch(coroutineScope)
    val peopleStateModel = PeopleStateModel.fetch(coroutineScope)
    val userStateModel = UserStateModel.fetch(coroutineScope)
    val logStateModel = LogStateModel.fetch(coroutineScope)

    LaunchedEffect(UserUtil.dataUser.value) {
        if (UserUtil.dataUser.value == null) {
            dataStateModel.clear()
        } else {
            var isObserveOn = false
            dataStateModel.fetchData(coroutineScope) {
                if (!isObserveOn) {
                    isObserveOn = true
                    dataStateModel.observeData(coroutineScope)
                }
            }
        }
    }


    /** Init */

    ScreenManager.setStateModels(switchStateModel)
    ScreenManager.remember()

    ScreenManager.clearScreens()
    MsgDialog.close()


    /** Launcher Screen */

    if (UserUtil.dataUser.value == null) {
        ScreenManager.openSignInScreen()
    } else {
        ScreenManager.openMainScreen()
    }


    /** Screens **/

    MyTheme(colorScheme) {
        SignInScreen(signStateModel)
        MainScreen(signStateModel, switchStateModel, checkListStateModel)
        SwitchScreen(switchStateModel)
        CheckListScreen(checkListStateModel, checkStateModel)
        CheckScreen(checkStateModel)
        CheckFruitScreen(checkStateModel, peopleStateModel)
        CreateFruitScreen(checkStateModel)
        FruitPeopleScreen(checkStateModel, peopleStateModel)
        ManagementScreen(peopleStateModel)
        FilingWeeklyScreen(filingStateModel)
        FilingYearlyScreen(filingStateModel)
        FilingFruitScreen(filingStateModel)
        ManagePeopleScreen(peopleStateModel)
        PeopleEditorScreen(peopleStateModel)
        PeopleOrgEditorScreen(peopleStateModel)
        ManageUserScreen(userStateModel)
        LogScreen(logStateModel)

        MsgDialog.MsgDialogScreen(coroutineScope)
        Toasty.ToastyScreen(coroutineScope)

        ErrorScreen()
    }
}