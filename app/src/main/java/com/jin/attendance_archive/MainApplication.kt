package com.jin.attendance_archive

import android.app.Application
import com.jin.attendance_archive.util.android.AndroidManager
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidManager.setContext(this).setApplicationId(BuildConfig.APPLICATION_ID)
        RxJavaPlugins.setErrorHandler { e -> if (e is UndeliverableException) return@setErrorHandler }
    }
}