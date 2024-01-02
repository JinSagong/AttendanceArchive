package com.jin.attendance_archive.statemodel

import androidx.compose.runtime.Composable
import com.jin.attendance_archive.model.data.DataUser
import com.jin.attendance_archive.model.repository.RepositoryUser
import com.jin.attendance_archive.model.util.OrganizationUtil
import com.jin.attendance_archive.model.util.UserUtil
import com.jin.attendance_archive.util.compose.ComposeState
import com.jin.attendance_archive.util.db.addTo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job

class UserStateModel {
    private val repositoryUser by lazy { RepositoryUser() }

    private val compositeDisposable = CompositeDisposable()

    val listUser = ComposeState(emptyList<DataUser>())

    @Composable
    private fun fetch(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            compositeDisposable.clear()
        }

        listUser.remember()
    }

    fun fetchUserList() {
        val idNotContained = arrayListOf("id000000", "id000001")
        repositoryUser.getUserList()
            .subscribe { data ->
                listUser.value = data
                    .filter { item ->
                        val org = item.org.firstOrNull()
                        val region =
                            if (org != null) OrganizationUtil.mapOrganizationByOrgId[org]?.firstOrNull()?.region else null
                        if (UserUtil.isGumi()) {
                            region == 1 || region == 3
                        } else {
                            region == 2 || region == 4
                        } && item.id !in idNotContained
                    }
                    .sortedBy { item -> item.id }
            }
            .addTo(compositeDisposable)
    }

    companion object {
        private var instance: UserStateModel? = null

        @Composable
        fun fetch(coroutineScope: CoroutineScope) = instance ?: run {
            val stateModel = UserStateModel()
            stateModel.fetch(coroutineScope)
            instance = stateModel
            stateModel
        }
    }
}