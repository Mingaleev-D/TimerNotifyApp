package com.example.timernotify.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.timernotify.MainActivity
import com.example.timernotify.R
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
class TimerService : LifecycleService() {

    private val binder = LocalBinder()
    private var serviceJob: Job? = null
    private var targetEndTimeMillis: Long = 0L

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    @ApplicationContext
    lateinit var context: Context

    companion object {
        // Команды для управления сервисом
        const val ACTION_START = "com.example.timerapp.action.START"
        const val ACTION_STOP = "com.example.timerapp.action.STOP"
        const val EXTRA_DURATION_MINUTES = "com.example.timerapp.extra.DURATION_MINUTES"
        // Уведомления
        const val NOTIFICATION_CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        const val FINISHED_NOTIFICATION_ID = 2
        // SharedPreferences для сохранения состояния
        private const val PREFS_NAME = "TimerPrefs"
        private const val KEY_TARGET_END_TIME = "targetEndTime"
        private const val KEY_IS_RUNNING = "isRunning"

        /**
         * Проверяет, активен ли был таймер до перезагрузки устройства.
         */
        fun isTimerRunning(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_IS_RUNNING, false)
        }
        /**
         * Получает сохранённое время окончания таймера, если оно было сохранено.
         */
        fun getTargetEndTime(context: Context): Long? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val time = prefs.getLong(KEY_TARGET_END_TIME, -1L)
            return if (time != -1L) time else null
        }
    }
    /**
     * Инициализация сервиса: создаём каналы уведомлений и восстанавливаем состояние таймера.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        restoreTimerState()
    }
    /**
     * Обработка входящих интентов:
     * - ACTION_START: старт нового таймера или восстановление старого
     * - ACTION_STOP: остановка таймера
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                val durationMinutes = intent.getLongExtra(EXTRA_DURATION_MINUTES, 0L)
                if (durationMinutes > 0) {
                    // Новый запуск таймера
                    val durationMillis = TimeUnit.MINUTES.toMillis(durationMinutes)
                    targetEndTimeMillis = System.currentTimeMillis() + durationMillis
                    saveTimerState(targetEndTimeMillis, true)
                    startForegroundTimer(targetEndTimeMillis)
                    startForeground(NOTIFICATION_ID, createNotification("Таймер запущен...", initial = true))
                } else {
                    // Попытка восстановления из сохранённого состояния
                    restoreTimerState()
                    val savedTargetTime = getTargetEndTime(this)
                    if (savedTargetTime != null && isTimerRunning(this)) {
                        if (System.currentTimeMillis() < savedTargetTime) {
                            targetEndTimeMillis = savedTargetTime
                            startForegroundTimer(targetEndTimeMillis, true)
                        } else {
                            // Таймер завершился во время перезагрузки
                            handleTimerFinish(wasRestarted = true)
                            stopForegroundService()
                        }
                    } else {
                        stopForegroundService()
                    }
                }
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
        }
        return START_STICKY
    }
    /**
     * Запускает корутину, которая каждую секунду обновляет состояние таймера и уведомления.
     */
    private fun startForegroundTimer(targetTimeMillis: Long, wasRestored: Boolean = false) {
        serviceJob?.cancel()
        // Запускаем уведомление и обновляем состояние
        startForeground(NOTIFICATION_ID, createNotification("Таймер запущен...", initial = true, restored = wasRestored))
        TimerState.setRunning(true)
        TimerState.setTargetEndTime(targetTimeMillis)
        // Основная логика таймера
        serviceJob = lifecycleScope.launch(Dispatchers.IO) {
            val ticker = tickerFlow(1000L) // Каждую секунду

            ticker.collect {
                val currentTime = System.currentTimeMillis()
                val remainingMillis = targetTimeMillis - currentTime

                if (remainingMillis <= 0) {
                    // Таймер завершился
                    withContext(Dispatchers.Main) {
                        handleTimerFinish()
                        stopForegroundService()
                    }
                    cancel()
                } else {
                    // Обновляем оставшееся время в состоянии
                    withContext(Dispatchers.Main) {
                        TimerState.updateRemainingTime(remainingMillis)
                    }
                    // Формируем строку времени (мм:сс)
                    val remainingSecondsTotal = TimeUnit.MILLISECONDS.toSeconds(remainingMillis)
                    val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
                    val remainingSeconds = remainingSecondsTotal % 60

                    val timeString = String.format("%02d:%02d", remainingMinutes, remainingSeconds)
                    // Каждую минуту — отдельное уведомление
                    if (remainingSeconds == 0L && remainingMinutes > 0) {
                        updateNotification("Осталось ${remainingMinutes + 1} мин.")
                    } else if (remainingMinutes == 0L && remainingSeconds > 0 && remainingSeconds % 10 == 0L) {
                        updateNotification("Осталось $timeString")
                    } else if (remainingMinutes > 0 && remainingSeconds == 59L){
                        updateNotification("Осталось $timeString")
                    }

                    if (remainingSeconds == 0L && remainingMinutes >= 0) {
                        val minutesLeft = remainingMinutes + if(remainingSecondsTotal > 0) 1 else 0
                        if (minutesLeft > 0) {
                            sendMinuteNotification(minutesLeft)
                        }
                    }
                }
            }
        }
    }
    /**
     * Завершает работу сервиса, сбрасывает состояние и закрывает уведомления.
     */
    private fun stopForegroundService() {
        serviceJob?.cancel()
        saveTimerState(null, false)
        TimerState.setRunning(false)
        startForeground(NOTIFICATION_ID, createNotification("Таймер остановлен"))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    /**
     * Обработка завершения таймера: сброс состояния, отправка финального уведомления.
     */
    private fun handleTimerFinish(wasRestarted: Boolean = false) {
        TimerState.reset()
        saveTimerState(null, false)

        // Отправляем вибрацию, звук и уведомление (если не было перезапуска)
        if (!wasRestarted) {
            notifyTimerFinished()
        }

        // Уведомление о завершении (с текстом "таймер завершён во время перезагрузки" если нужно)
        sendFinalNotification(wasRestarted)
//        TimerState.reset()
//        saveTimerState(null, false)
//        sendFinalNotification(wasRestarted)
    }
    /**
     * Создаёт каналы уведомлений (требование Android 8+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Таймер"
            val descriptionText = "Уведомления от таймера"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null)
            }

            val finishedChannelId = "${NOTIFICATION_CHANNEL_ID}_finished"
            val finishedName = "Завершение таймера"
            val finishedImportance = NotificationManager.IMPORTANCE_HIGH
            val finishedChannel = NotificationChannel(finishedChannelId, finishedName, finishedImportance).apply {
                description = "Уведомление о завершении таймера"
                enableVibration(true)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(soundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT)
            }

            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(finishedChannel)
        }
    }
    /**
     * Создаёт уведомление с заданным текстом.
     */
    private fun createNotification(text: String, initial: Boolean = false, restored: Boolean = false): Notification {
        val title = if (restored) "Таймер восстановлен" else "Таймер работает"
        val contentText = if (restored) "Таймер продолжает работу. $text" else text

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlags)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }
    /**
     * Обновляет активное уведомление.
     */
    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    /**
     * Отправляет отдельное уведомление каждую минуту.
     */
    private fun sendMinuteNotification(minutesLeft: Long) {
        val text = "Осталось $minutesLeft ${getMinuteWord(minutesLeft)}"
        updateNotification(text)
    }
    /**
     * Отправляет уведомление о завершении таймера.
     */
    private fun sendFinalNotification(wasRestarted: Boolean) {
        val finishedChannelId = "${NOTIFICATION_CHANNEL_ID}_finished"
        val contentText = if(wasRestarted) "Таймер завершился во время перезагрузки." else "Время вышло!"

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlags)

        val notification = NotificationCompat.Builder(this, finishedChannelId)
            .setContentTitle("Таймер завершен")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(FINISHED_NOTIFICATION_ID, notification)
    }
    /**
     * Выбирает правильную форму слова "минута" по числу.
     */
    private fun getMinuteWord(minutes: Long): String {
        return when {
            minutes % 10 == 1L && minutes % 100 != 11L -> "минута"
            minutes % 10 in 2..4 && minutes % 100 !in 12..14 -> "минуты"
            else -> "минут"
        }
    }
    /**
     * Сохраняет текущее состояние таймера в SharedPreferences.
     */
    private fun saveTimerState(targetTime: Long?, running: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        if (targetTime != null) {
            prefs.putLong(KEY_TARGET_END_TIME, targetTime)
        } else {
            prefs.remove(KEY_TARGET_END_TIME)
        }
        prefs.putBoolean(KEY_IS_RUNNING, running)
        prefs.apply()
    }
    /**
     * Восстанавливает состояние таймера при старте сервиса.
     */
    private fun restoreTimerState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTargetTime = prefs.getLong(KEY_TARGET_END_TIME, -1L)
        val wasRunning = prefs.getBoolean(KEY_IS_RUNNING, false)

        if (wasRunning && savedTargetTime != -1L) {
            val currentTime = System.currentTimeMillis()
            if (currentTime < savedTargetTime) {
                TimerState.setTargetEndTime(savedTargetTime)
                TimerState.setRunning(true)
                TimerState.updateRemainingTime(savedTargetTime - currentTime)
            } else {
                handleTimerFinish(wasRestarted = true)
                saveTimerState(null, false)
            }
        } else {
            TimerState.reset()
            saveTimerState(null, false)
        }
    }
    /**
     * Поток-тикер, испускающий сигнал каждую секунду.
     */
    private fun tickerFlow(delayMillis: Long, initialDelayMillis: Long = 0L) = flow {
        delay(initialDelayMillis)
        while (true) {
            emit(Unit)
            delay(delayMillis)
        }
    }

    // Реализация LocalBinder для связи с Activity, если потребуется
    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    private fun notifyTimerFinished() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Проверяем, не в беззвучном ли режиме
        val isSilent = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
        // Вибрация
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 300, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
        // Звук, если не в тихом режиме
        if (!isSilent) {
            val mediaPlayer = MediaPlayer.create(this, R.raw.budilnika) // добавь свой файл
            mediaPlayer?.apply {
                setOnCompletionListener { release() }
                start()
            }
        }

        // Уведомление
        val channelId = "timer_finish_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                   channelId,
                   "Таймер завершён",
                   NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                description = "Уведомления при завершении таймера"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background) // твоя иконка
            .setContentTitle("Время вышло!")
            .setContentText("Таймер завершён.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2001, notification)

    }



    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        super.onDestroy()
    }
}
