package com.example.timernotify.data.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.timernotify.data.manager.Constants
import com.example.timernotify.data.manager.PrefManager
import com.example.timernotify.data.manager.TimerNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var timerNotifier: TimerNotifier
    private var job: Job? = null
    private var lastNotifiedMinute: Int = -1
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    @SuppressLint("DefaultLocale")
    override fun onStartCommand(
           intent: Intent?,
           flags: Int,
           startId: Int
    ): Int {
        val endTime = intent?.getLongExtra(Constants.KEY_END_TIME, 0L) ?: prefManager.endTimeMillis
        val initialFormattedTime = formatTime(endTime - System.currentTimeMillis())
        startForeground(1, timerNotifier.showForegroundNotification(initialFormattedTime))

        prefManager.lastUpdateMillis = System.currentTimeMillis()

        job = serviceScope.launch {
            while (System.currentTimeMillis() < endTime && prefManager.isTimerRunning) {
                prefManager.lastUpdateMillis = System.currentTimeMillis()
                val remaining = endTime - System.currentTimeMillis()
                val formattedTime = formatTime(remaining)
                timerNotifier.updateForegroundNotification(formattedTime)
                val currentMinute = (remaining / 60_000).toInt()
                if (currentMinute != lastNotifiedMinute) {
                    if (currentMinute > 0) {
                        timerNotifier.notifyEachMinute(currentMinute)
                    }
                    lastNotifiedMinute = currentMinute
                }

                delay(1000)
            }
            if (prefManager.isTimerRunning) {
                timerNotifier.notifyFinished()
                prefManager.isTimerRunning = false
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun formatTime(remainingMillis: Long): String {
        val minutes = (remainingMillis / 60_000).toInt()
        val seconds = ((remainingMillis % 60_000) / 1000).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

}
