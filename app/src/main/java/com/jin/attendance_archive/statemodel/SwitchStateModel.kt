package com.jin.attendance_archive.statemodel

import androidx.compose.runtime.*
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.model.repository.RepositoryAttendanceType
import com.jin.attendance_archive.model.util.AttendanceTypeUtil
import com.jin.attendance_archive.model.util.UserUtil
import com.jin.attendance_archive.ui.component.Toasty
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.Accessibility
import com.jin.attendance_archive.util.compose.ComposeState
import com.jin.attendance_archive.util.db.addTo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*

class SwitchStateModel private constructor() {
    private val repositoryAttendanceType by lazy { RepositoryAttendanceType() }

    private val compositeDisposable = CompositeDisposable()

    val switchConfirmedState = ComposeState(false)

    private var onFinishJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    private val onFinish = {
        onFinishJob = GlobalScope.launch {
            switchConfirmedState.value = true
            delay(1000L)
            closeSwitch()
        }
    }

    @Composable
    private fun fetch(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            compositeDisposable.clear()
        }

        switchConfirmedState.remember()
    }

    fun openSwitch() {
        if (Accessibility.switch) {
            switchConfirmedState.value = false
            ScreenManager.switchScreen.value = Pair(true, true)
        } else {
            Toasty.show(if (AttendanceTypeUtil.listAttendanceType.value.firstOrNull { item -> item.on } != null) Strings.switchOnMsg else Strings.switchOffMsg)
        }
    }

    fun confirmSwitch() {
        if (switchConfirmedState.value) cancelSwitch()
        else onFinish.invoke()
    }

    fun cancelSwitch() {
        onFinishJob?.cancel()
        onFinishJob = null
        closeSwitch()
    }

    private fun closeSwitch() {
        ScreenManager.switchScreen.value = Pair(false, false)
    }

    fun setSwitch(id: String, on: Boolean) {
        repositoryAttendanceType.setAttendanceType(UserUtil.getRegion(), id, on)
            .subscribe().addTo(compositeDisposable)
    }

    companion object {
        private var instance: SwitchStateModel? = null

        @Composable
        fun fetch(coroutineScope: CoroutineScope) = instance ?: run {
            val stateModel = SwitchStateModel()
            stateModel.fetch(coroutineScope)
            instance = stateModel
            stateModel
        }
    }
}