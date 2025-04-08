package com.example.timernotify.data.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.timernotify.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerNotifier @Inject constructor(
       @ApplicationContext private val context: Context,
       private val notificationManager: NotificationManager
) {

    companion object {

        private const val CHANNEL_ID = "timer_channel"
        private const val CHANNEL_NAME = "Timer Notifications"
        private const val NOTIFICATION_ID_FOREGROUND = 1
        private const val NOTIFICATION_ID_MINUTE = 2
        private const val NOTIFICATION_ID_FINISH = 3
        private const val NOTIFICATION_ID_RESTORE = 4
    }

    private var mediaPlayer: MediaPlayer? = null
    private val foregroundNotificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle("Таймер")
        .setOngoing(true)

    init {
        createNotificationChannel()
        mediaPlayer = MediaPlayer.create(context, R.raw.budilnika) // Replace with your sound file
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                   CHANNEL_ID,
                   CHANNEL_NAME,
                   NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления таймера"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showForegroundNotification(initialTime: String): Notification {
        return foregroundNotificationBuilder
            .setContentText("Осталось: $initialTime")
            .build()
    }

    fun updateForegroundNotification(remainingTime: String) {
        val notification = foregroundNotificationBuilder
            .setContentText("Осталось: $remainingTime")
            .build()
        notificationManager.notify(NOTIFICATION_ID_FOREGROUND, notification)
    }

    fun notifyEachMinute(remainingMinutes: Int) {
        val text = "Осталось $remainingMinutes мин."
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Таймер")
            .setContentText(text)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID_MINUTE, notification)
    }

    fun notifyFinished() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Таймер завершён")
            .setContentText("Время вышло!")
            .setVibrate(longArrayOf(0, 300, 300, 300))
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID_FINISH, notification)
        playCompletionSound()
    }

    private fun playCompletionSound() {
        mediaPlayer?.start()
    }

    fun notifyRestored(
           bootTime: String,
           resumeTime: String
    ) {
        val text = "Таймер приостановлен в $bootTime, завершится в $resumeTime"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Таймер восстановлен")
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID_RESTORE, notification)
    }
}
