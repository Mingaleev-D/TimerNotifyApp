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
/**
 * BroadcastReceiver, который срабатывает при завершении загрузки устройства.
 * Используется для восстановления работы таймера, если он был активен до перезагрузки.
 */

//@AndroidEntryPoint
//class BootCompletedReceiver : BroadcastReceiver() {
//
//    /**
//     * Метод вызывается системой при получении события BOOT_COMPLETED.
//     * Если таймер был активен до перезагрузки, он автоматически запускает TimerService.
//     */
//
//    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
//            // Проверяем, был ли таймер запущен до перезагрузки
//            if (TimerService.isTimerRunning(context)) {
//
//                // Создаём Intent для запуска TimerService
//                // Время не передаём — оно будет восстановлено внутри сервиса
//                val serviceIntent = Intent(context, TimerService::class.java).apply {
//                    // Не передаем время, сервис сам восстановит из SharedPreferences
//                    action = TimerService.ACTION_START // Используем START, чтобы инициировать логику восстановления
//                }
//
//                // Запускаем Foreground Service безопасно
//                ContextCompat.startForegroundService(context, serviceIntent)
//
//            }
//        }
//    }
//}

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (TimerService.isTimerRunning(context)) {

                val serviceIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_START
                }
                ContextCompat.startForegroundService(context, serviceIntent)

                // Показываем Toast
                Toast
                    .makeText(context, "Таймер восстановлен после перезагрузки", Toast.LENGTH_LONG).show()

                // Показываем уведомление (опционально, если хочешь сделать вручную)
                showRestoreNotification(context)
            }
        }
    }

    private fun showRestoreNotification(context: Context) {
        val notificationManager =
               context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "timer_channel"
        val channelName = "Таймер"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                   channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления от таймера"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background) // Добавь свою иконку в drawable
            .setContentTitle("Таймер восстановлен")
            .setContentText("Продолжаем отсчёт после перезагрузки")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1001, notification)
    }
}
