package com.jin.attendance_archive.statemodel

import androidx.compose.runtime.Composable
import com.jin.attendance_archive.model.data.DataAttendance
import com.jin.attendance_archive.model.repository.RepositoryAttendance
import com.jin.attendance_archive.model.util.AttendanceTypeUtil
import com.jin.attendance_archive.model.util.UserUtil
import com.jin.attendance_archive.ui.component.MsgDialog
import com.jin.attendance_archive.ui.component.Toasty
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.DateTimeUtil
import com.jin.attendance_archive.util.compose.ComposeState
import com.jin.attendance_archive.util.db.addTo
import com.jin.attendance_archive.util.file.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import java.io.File
import java.util.Calendar
import kotlin.coroutines.resume

class FilingStateModel private constructor() {
    private val repositoryAttendance by lazy { RepositoryAttendance() }

    private val compositeDisposable = CompositeDisposable()

    val yearPosition = ComposeState(DateTimeUtil.getYearPosition())
    val monthPosition = ComposeState(DateTimeUtil.getMonthPosition())
    val weekPosition = ComposeState(DateTimeUtil.getWeekPosition())
    val yearArray = DateTimeUtil.getYearArray()
    val monthArray = DateTimeUtil.getMonthArray()
    val weekArray
        get() = DateTimeUtil.getWeekArray()
            .take(DateTimeUtil.getMaxWeekCount(yearPosition.value, monthPosition.value))
    val option = ComposeState(0)

    val file = ComposeState<File?>(null)
    val fileGeneratorJob = ComposeState<Job?>(null)

    @Composable
    private fun fetch(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            compositeDisposable.clear()
        }

        yearPosition.remember()
        monthPosition.remember()
        weekPosition.remember()
        option.remember()
        file.remember()
        fileGeneratorJob.remember()
    }

    fun initPosition(isCurrentPosition: Boolean = false) {
        yearPosition.value =
            if (isCurrentPosition) DateTimeUtil.getYearPosition(Calendar.getInstance()) else DateTimeUtil.getYearPosition()
        monthPosition.value = DateTimeUtil.getMonthPosition()
        weekPosition.value = DateTimeUtil.getWeekPosition()
        option.value = 0
    }

    private suspend fun getAttendance1(week: Int) = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<List<DataAttendance>> { continuation ->
            repositoryAttendance.getAttendance(UserUtil.getRegion(), week)
                .subscribe(
                    { continuation.resume(it) },
                    { continuation.resume(emptyList()) }
                ).addTo(compositeDisposable)
        }
    }

    private suspend fun getAttendance2(startWeek: Int, endWeek: Int) = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<List<DataAttendance>> { continuation ->
            repositoryAttendance.getAttendance(UserUtil.getRegion(), startWeek, endWeek)
                .subscribe(
                    { continuation.resume(it) },
                    { continuation.resume(emptyList()) }
                ).addTo(compositeDisposable)
        }
    }

    private suspend fun getAttendance3(attendanceType: String, week: Int) =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<List<DataAttendance>> { continuation ->
                repositoryAttendance.getAttendance(UserUtil.getRegion(), attendanceType, week)
                    .subscribe(
                        { continuation.resume(it) },
                        { continuation.resume(emptyList()) }
                    ).addTo(compositeDisposable)
            }
        }

    private suspend fun getAttendance4(attendanceType: String, startWeek: Int, endWeek: Int) =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<List<DataAttendance>> { continuation ->
                repositoryAttendance.getAttendance(
                    UserUtil.getRegion(), attendanceType, startWeek, endWeek
                )
                    .subscribe(
                        { continuation.resume(it) },
                        { continuation.resume(emptyList()) }
                    ).addTo(compositeDisposable)
            }
        }

    fun generateWeeklyFile(coroutineScope: CoroutineScope) {
        val generate = {
            cancelGenerateFile()
            fileGeneratorJob.value = coroutineScope.launch(Dispatchers.Default) {
                val title = DateTimeUtil.getWeeklyFileTitle(
                    yearPosition.value, monthPosition.value, weekPosition.value
                )
                val week = DateTimeUtil.getWeekValue(
                    yearPosition.value, monthPosition.value, weekPosition.value
                )
                val list = getAttendance1(week)
                if (isActive) {
                    val fg = WeeklyFileGenerator(title)
                    fg.setData(list)
                    val fileCreated = if (isActive) fg.createFile() else null
                    if (isActive) file.value = fileCreated
                    if (isActive) Toasty.show(if (file.value != null) Strings.filingGenerateMsg else Strings.filingGenerateFailureMsg)
                }
            }.apply {
                invokeOnCompletion { fileGeneratorJob.value = null }
            }
        }
        if (file.value != null) {
            MsgDialog.withTwoBtn()
                .setMessage(Strings.filingRegenerateMsg)
                .onConfirm {
                    it.invoke()
                    generate()
                }
                .show()
        } else generate()
    }

    fun generateYearlyFile(coroutineScope: CoroutineScope) {
        val generate = {
            cancelGenerateFile()
            fileGeneratorJob.value = coroutineScope.launch(Dispatchers.Default) {
                if (option.value == 0) {
                    val startWeek =
                        DateTimeUtil.getStartWeekValueForYearly(yearPosition.value, Calendar.SUNDAY)
                    val endWeek =
                        DateTimeUtil.getEndWeekValueForYearly(yearPosition.value, Calendar.SUNDAY)
                    val title = DateTimeUtil.getYearlyFileTitle(yearPosition.value, "주일 출석부")
                    val list = DateTimeUtil.splitWeekValueRange(startWeek, endWeek)
                        .map { getAttendance2(it.first, it.second) }
                        .flatten()
                    if (isActive) {
                        val fg = YearlyFileGenerator(title)
                        fg.setData(true, list, startWeek, endWeek, Calendar.SUNDAY)
                        val fileCreated = if (isActive) fg.createFile() else null
                        if (isActive) file.value = fileCreated
                        if (isActive) Toasty.show(if (file.value != null) Strings.filingGenerateMsg else Strings.filingGenerateFailureMsg)
                    }
                } else {
                    AttendanceTypeUtil.listAttendanceType.value.filter { !it.hasFruit }
                        .getOrNull(option.value - 1)?.let { attendanceType ->
                            val day = when (attendanceType.id) {
                                "id000003" -> Calendar.WEDNESDAY
                                "id000004" -> Calendar.THURSDAY
                                "id000005" -> Calendar.FRIDAY
                                else -> Calendar.SUNDAY
                            }
                            val startWeek =
                                DateTimeUtil.getStartWeekValueForYearly(yearPosition.value, day)
                            val endWeek =
                                DateTimeUtil.getEndWeekValueForYearly(yearPosition.value, day)
                            val title = DateTimeUtil.getYearlyFileTitle(
                                yearPosition.value,
                                attendanceType.name + " 출석부"
                            )
                            val list = DateTimeUtil.splitWeekValueRange(startWeek, endWeek)
                                .map { getAttendance4(attendanceType.id, it.first, it.second) }
                                .flatten()
                            if (isActive) {
                                val fg = YearlyFileGenerator(title)
                                fg.setData(false, list, startWeek, endWeek, day)
                                val fileCreated = if (isActive) fg.createFile() else null
                                if (isActive) file.value = fileCreated
                                if (isActive) Toasty.show(if (file.value != null) Strings.filingGenerateMsg else Strings.filingGenerateFailureMsg)
                            }
                        } ?: run { if (isActive) Toasty.show(Strings.filingGenerateFailureMsg) }
                }
            }.apply {
                invokeOnCompletion { fileGeneratorJob.value = null }
            }
        }
        if (file.value != null) {
            MsgDialog.withTwoBtn()
                .setMessage(Strings.filingRegenerateMsg)
                .onConfirm {
                    it.invoke()
                    generate()
                }
                .show()
        } else generate()
    }

    fun generateFruitFile(coroutineScope: CoroutineScope) {
        val generate = {
            cancelGenerateFile()
            fileGeneratorJob.value = coroutineScope.launch(Dispatchers.Default) {
                AttendanceTypeUtil.listAttendanceType.value.firstOrNull { it.hasFruit }
                    ?.let { attendanceType ->
                        when (option.value) {
                            1 -> {
                                val title = DateTimeUtil.getMonthlyFileTitle(
                                    yearPosition.value, monthPosition.value, "전교인 주일 현장전도"
                                )
                                val startWeek = DateTimeUtil.getStartWeekValue(
                                    yearPosition.value, monthPosition.value
                                )
                                val endWeek = DateTimeUtil.getEndWeekValue(
                                    yearPosition.value, monthPosition.value
                                )
                                val list = DateTimeUtil.splitWeekValueRange(startWeek, endWeek)
                                    .map { getAttendance4(attendanceType.id, it.first, it.second) }
                                    .flatten()
                                if (isActive) {
                                    val fg = FruitFileGenerator(title)
                                    fg.setData(list, startWeek, endWeek, true)
                                    val fileCreated = if (isActive) fg.createFile() else null
                                    if (isActive) file.value = fileCreated
                                }
                            }

                            2 -> {
                                val title = DateTimeUtil.getYearlyFileTitle(
                                    yearPosition.value, "전교인 주일 현장전도"
                                )
                                val startWeek = DateTimeUtil.getStartWeekValue(yearPosition.value)
                                val endWeek = DateTimeUtil.getEndWeekValue(yearPosition.value)
                                val list = DateTimeUtil.splitWeekValueRange(startWeek, endWeek)
                                    .map { getAttendance4(attendanceType.id, it.first, it.second) }
                                    .flatten()
                                if (isActive) {
                                    val fg = FruitFileGenerator(title)
                                    fg.setData(list, startWeek, endWeek, true)
                                    val fileCreated = if (isActive) fg.createFile() else null
                                    if (isActive) file.value = fileCreated
                                }
                            }

                            else -> {
                                val title = DateTimeUtil.getWeeklyFileTitle(
                                    yearPosition.value,
                                    monthPosition.value,
                                    weekPosition.value,
                                    "전교인 주일 현장전도"
                                )
                                val week = DateTimeUtil.getWeekValue(
                                    yearPosition.value, monthPosition.value, weekPosition.value
                                )
                                val list = getAttendance3(attendanceType.id, week)
                                if (isActive) {
                                    val fg = FruitFileGenerator(title)
                                    fg.setData(list, week, week, false)
                                    val fileCreated = if (isActive) fg.createFile() else null
                                    if (isActive) file.value = fileCreated
                                }
                            }
                        }
                        if (isActive) Toasty.show(if (file.value != null) Strings.filingGenerateMsg else Strings.filingGenerateFailureMsg)
                    } ?: run { if (isActive) Toasty.show(Strings.filingGenerateFailureMsg) }
            }.apply {
                invokeOnCompletion { fileGeneratorJob.value = null }
            }
        }
        if (file.value != null) {
            MsgDialog.withTwoBtn()
                .setMessage(Strings.filingRegenerateMsg)
                .onConfirm {
                    it.invoke()
                    generate()
                }
                .show()
        } else generate()
    }

    fun cancelGenerateFile() {
        fileGeneratorJob.value?.cancel()
        file.value = null
    }

    fun openFile() {
        FileUtil.open(file.value)
    }

    fun shareFileViaKakaoTalk() {
        FileUtil.shareViaKakaoTalk(file.value)
    }

    fun shareFileViaEmail() {
        FileUtil.shareViaEmail(file.value)
    }

    fun downloadFile() {
        FileUtil.download(file.value)
    }

    companion object {
        private var instance: FilingStateModel? = null

        @Composable
        fun fetch(coroutineScope: CoroutineScope) = instance ?: run {
            val stateModel = FilingStateModel()
            stateModel.fetch(coroutineScope)
            instance = stateModel
            stateModel
        }
    }
}