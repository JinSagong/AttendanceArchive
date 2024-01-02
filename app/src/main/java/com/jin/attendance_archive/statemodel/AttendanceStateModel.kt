package com.jin.attendance_archive.statemodel

import androidx.compose.runtime.Composable
import com.jin.attendance_archive.model.repository.RepositoryAttendance
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job

class AttendanceStateModel {
    private val repositoryAttendance by lazy { RepositoryAttendance() }

    private val compositeDisposable = CompositeDisposable()

    @Composable
    private fun fetch(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            compositeDisposable.clear()
        }
    }

    companion object {
        private var instance: AttendanceStateModel? = null

        @Composable
        fun fetch(coroutineScope: CoroutineScope) = instance ?: run {
            val stateModel = AttendanceStateModel()
            stateModel.fetch(coroutineScope)
            instance = stateModel
            stateModel
        }
    }
}