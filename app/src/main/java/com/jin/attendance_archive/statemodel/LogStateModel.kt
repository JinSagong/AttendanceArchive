package com.jin.attendance_archive.statemodel

import androidx.compose.runtime.Composable
import com.jin.attendance_archive.model.data.DataLog
import com.jin.attendance_archive.model.repository.RepositoryLog
import com.jin.attendance_archive.util.compose.ComposeState
import com.jin.attendance_archive.util.db.addTo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job

class LogStateModel {
    private val repositoryLog by lazy { RepositoryLog() }

    private val compositeDisposable = CompositeDisposable()

    val listLog = ComposeState(emptyList<DataLog>())

    @Composable
    private fun fetch(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            compositeDisposable.clear()
        }

        listLog.remember()
    }

    fun fetchLogList() {
        repositoryLog.getLogList()
            .subscribe { data -> listLog.value = data.sortedByDescending { item -> item.id } }
            .addTo(compositeDisposable)
    }

    companion object {
        private var instance: LogStateModel? = null

        @Composable
        fun fetch(coroutineScope: CoroutineScope) = instance ?: run {
            val stateModel = LogStateModel()
            stateModel.fetch(coroutineScope)
            instance = stateModel
            stateModel
        }
    }
}