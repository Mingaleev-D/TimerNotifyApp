package com.example.timernotify.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.timernotify.data.manager.Constants
import com.example.timernotify.data.manager.PrefManager
import com.example.timernotify.data.manager.TimerNotifier
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var timerNotifier: TimerNotifier

    override fun onReceive(
           context: Context,
           intent: Intent
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (prefManager.isTimerRunning) {
                val endTime = prefManager.endTimeMillis
                val currentTime = System.currentTimeMillis()

                if (currentTime < endTime) {
                    val serviceIntent = Intent(context, TimerService::class.java).apply {
                        putExtra(Constants.KEY_END_TIME, endTime)
                    }
                    ContextCompat.startForegroundService(context, serviceIntent)
                    val bootTime = android.text.format.DateFormat.format("HH:mm", currentTime)
                    val resumeTime = android.text.format.DateFormat.format("HH:mm", endTime)
                    timerNotifier.notifyRestored(bootTime.toString(), resumeTime.toString())
                } else {
                    prefManager.isTimerRunning = false
                }
            }
        }
    }
}
