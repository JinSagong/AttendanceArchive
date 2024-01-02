package com.jin.attendance_archive.util.android

import android.app.Activity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.jin.attendance_archive.util.Debug
import kotlinx.coroutines.launch

@Suppress("UNUSED")
class UpdateUtil(private val activity: ComponentActivity) {
    private var mCallback: ((Boolean) -> Unit)? = null
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var newResult = false
    private var newIntent = true

    companion object {
        const val CHECK_UPDATE = true
    }

    fun onCreate(callback: ((Boolean) -> Unit)?) {
        if (CHECK_UPDATE) {
            mCallback = callback
            activityResultLauncher =
                activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                    newResult = true
                    when (result.resultCode) {
                        Activity.RESULT_OK -> Debug.i("updateUtilResult=RESULT_OK")
                        Activity.RESULT_CANCELED -> Debug.i("updateUtilResult=RESULT_CANCELED")
                        RESULT_IN_APP_UPDATE_FAILED -> Debug.i("updateUtilResult=RESULT_IN_APP_UPDATE_FAILED")
                    }
                }
        } else callback?.invoke(true)
    }

    fun onResume() {
        if (CHECK_UPDATE && activityResultLauncher != null) {
            if (newResult && !newIntent) {
                activity.finish()
                return
            }
            newResult = false
            newIntent = false

            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                val updateAvailability = appUpdateInfo.updateAvailability()
                Debug.i(
                    "[onResume]updateAvailability=${
                        when (updateAvailability) {
                            UpdateAvailability.UPDATE_NOT_AVAILABLE -> "UPDATE_NOT_AVAILABLE"
                            UpdateAvailability.UPDATE_AVAILABLE -> "UPDATE_AVAILABLE"
                            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS"
                            else -> "UNKNOWN"
                        }
                    }"
                )

                if (updateAvailability == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                    mCallback?.invoke(true)
                } else if (updateAvailability != UpdateAvailability.UNKNOWN
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activityResultLauncher!!,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }.addOnFailureListener { e ->
                Debug.e("UpdateUtilException=${e.message}")
                if (e is InstallException) {
                    mCallback?.invoke(false)
                    activity.lifecycleScope.launch {
                        Toast.makeText(
                            activity,
                            "앱 업데이트를 위해 구글플레이를 활성화하거나 로그인하세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun setNewIntent() {
        newIntent = true
    }
}