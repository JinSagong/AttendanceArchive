package com.jin.attendance_archive

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import com.jin.attendance_archive.util.android.AndroidColorScheme
import com.jin.attendance_archive.util.android.UpdateUtil

class MainActivity : ComponentActivity() {
    private val updateUtil by lazy { UpdateUtil(this) }
    private var stopped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stopped = false

        updateUtil.onCreate { pass ->
            if (pass) {
                setContent {
                    AndroidColorScheme(dynamicColor = false) { colorScheme ->
                        Launcher(ScreenManager.MODE_ANDROID, colorScheme)
                    }
                }

                onBackPressedDispatcher.addCallback {
                    ScreenManager.onBackPressed { finish() }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        updateUtil.setNewIntent()
    }

    override fun onResume() {
        super.onResume()
        updateUtil.onResume()
    }

    override fun onStart() {
        super.onStart()
        if (ScreenManager.errorScreen.value.first && stopped) {
            finish()
            startActivity(intent)
        }
        stopped = false
    }

    override fun onStop() {
        super.onStop()
        stopped = true
    }
}