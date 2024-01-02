package com.jin.attendance_archive.util.android

import android.content.Context
import java.lang.ref.WeakReference

object AndroidManager {
    private var contextReference: WeakReference<Context>? = null
    val context get() = contextReference?.get()!!
    lateinit var applicationId: String
        private set

    fun setContext(context: Context) = apply {
        contextReference = WeakReference(context)
    }

    fun setApplicationId(id: String) = apply {
        applicationId = id
    }
}