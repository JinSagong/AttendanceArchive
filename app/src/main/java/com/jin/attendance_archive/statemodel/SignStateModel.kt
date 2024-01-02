package com.jin.attendance_archive.statemodel

import androidx.compose.runtime.*
import com.jin.attendance_archive.model.util.UserUtil
import com.jin.attendance_archive.model.repository.RepositoryUser
import com.jin.attendance_archive.ui.component.Toasty
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.compose.ComposeState
import com.jin.attendance_archive.util.db.addTo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job

class SignStateModel private constructor() {
    private val repositoryUser by lazy { RepositoryUser() }

    private val compositeDisposable = CompositeDisposable()

    val idState = ComposeState("")

    @Composable
    private fun fetch(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            compositeDisposable.clear()
        }

        idState.remember()
    }

    fun signIn() {
        if (idState.value.trim().isEmpty()) {
            Toasty.show(Strings.signInIdEmpty)
            return
        }
        repositoryUser.getUser(idState.value.trim())
            .subscribe(
                { items ->
                    val user = items?.firstOrNull()
                    if (user != null) {
                        UserUtil.setUser(user)
                        idState.value = ""
                    } else Toasty.show(Strings.signInIdWrong)
                },
                { Toasty.show(Strings.signInIdError) }
            ).addTo(compositeDisposable)
    }

    fun signOut() {
        UserUtil.setUser(null)
    }

    companion object {
        private var instance: SignStateModel? = null

        @Composable
        fun fetch(coroutineScope: CoroutineScope) = instance ?: run {
            val stateModel = SignStateModel()
            stateModel.fetch(coroutineScope)
            instance = stateModel
            stateModel
        }
    }
}