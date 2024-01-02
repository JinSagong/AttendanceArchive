package com.jin.attendance_archive.util.file

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.content.FileProvider
import com.jin.attendance_archive.ui.component.Toasty
import com.jin.attendance_archive.res.Strings
import com.jin.attendance_archive.util.android.AndroidManager
import java.io.File

@Suppress("UNUSED_PARAMETER")
object FileUtil {
    fun download(file: File?) = Unit

    fun open(file: File?) {
        if (file == null) {
            Toasty.show(Strings.filingNoFileMsg)
            return
        }
        val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(
                FileProvider.getUriForFile(
                    AndroidManager.context, AndroidManager.applicationId + ".provider", file
                ),
                "application/vnd.ms-excel"
            )
        }
        AndroidManager.context.startActivity(
            Intent.createChooser(
                openFileIntent, Strings.filingChooserTitleToOpen
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
    }

    fun shareViaKakaoTalk(file: File?) {
        if (file == null) {
            Toasty.show(Strings.filingNoFileMsg)
            return
        }
        val kakaoIntent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "application/vnd.ms-excel"
            val uri = FileProvider.getUriForFile(
                AndroidManager.context, AndroidManager.applicationId + ".provider", file
            )
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            `package` = "com.kakao.talk"
        }
        try {
            AndroidManager.context.startActivity(kakaoIntent)
        } catch (e: ActivityNotFoundException) {
            Toasty.show(Strings.filingNoKakaoTalkMsg)
        }
    }

    fun shareViaEmail(file: File?) {
        if (file == null) {
            Toasty.show(Strings.filingNoFileMsg)
            return
        }
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "application/vnd.ms-excel"
            val uri = FileProvider.getUriForFile(
                AndroidManager.context, AndroidManager.applicationId + ".provider", file
            )
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
        }
        AndroidManager.context.startActivity(
            Intent.createChooser(
                emailIntent, Strings.filingChooserTitleToSend
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
    }
}