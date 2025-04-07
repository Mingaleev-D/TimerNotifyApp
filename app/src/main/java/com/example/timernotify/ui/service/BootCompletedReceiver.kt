package com.example.timernotify.ui.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.timernotify.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver, который срабатывает при завершении загрузки устройства.
 * Используется для восстановления работы таймера, если он был активен до перезагрузки.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(
           context: Context,
           intent: Intent
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (TimerService.isTimerRunning(context)) {
                val serviceIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_START
                }
                ContextCompat.startForegroundService(context, serviceIntent)
                // Показываем Toast
                Toast
                    .makeText(context, "Таймер восстановлен после перезагрузки", Toast.LENGTH_LONG)
                    .show()

            }
        }
    }
}
// Показываем уведомление (опционально, если хочешь сделать вручную)
// showRestoreNotification(context)
//    private fun showRestoreNotification(context: Context) {
//        val notificationManager =
//               context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val channelId = "timer_channel"
//        val channelName = "Таймер"
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                   channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
//            ).apply {
//                description = "Уведомления от таймера"
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//        val notification = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.ic_launcher_background)
//            .setContentTitle("Таймер восстановлен")
//            .setContentText("Продолжаем отсчёт после перезагрузки")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        notificationManager.notify(1001, notification)
//    }
