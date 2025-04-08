package com.example.timernotify.data.service

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Foreground-сервис, который управляет логикой таймера:
 * - Обрабатывает запуск/остановку таймера
 * - Обновляет уведомления каждую минуту
 * - Сохраняет и восстанавливает состояние таймера после перезапуска устройства
 * - Отправляет финальное уведомление с сигналом по завершении таймера
 */
@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var timerNotifier: TimerNotifier
    private var job: Job? = null

    override fun onStartCommand(
           intent: Intent?,
           flags: Int,
           startId: Int
    ): Int {
        val endTime = intent?.getLongExtra(Constants.KEY_END_TIME, 0L) ?: prefManager.endTimeMillis

        startForeground(1, timerNotifier.showForegroundNotification("00:00"))

        job = CoroutineScope(Dispatchers.Default).launch {
            while (System.currentTimeMillis() < endTime && prefManager.isTimerRunning) {
                val remaining = endTime - System.currentTimeMillis()
                val remainingSecondsTotal = TimeUnit.MILLISECONDS.toSeconds(remaining)
                val minutes = remainingSecondsTotal / 60
                val seconds = remainingSecondsTotal % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                timerNotifier.updateForegroundNotification(formattedTime)
                val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
                timerNotifier.notifyEachMinute(remainingMinutes.toInt())

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
        job?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
