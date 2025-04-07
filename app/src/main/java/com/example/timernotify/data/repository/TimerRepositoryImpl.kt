package com.example.timernotify.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.timernotify.domain.repository.TimerRepository
import com.example.timernotify.ui.service.TimerService
import com.example.timernotify.ui.service.TimerState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
/**
 * Реализация репозитория. Работает с `TimerService`, `TimerState` и форматирует отображение времени.
 */
@Singleton
class TimerRepositoryImpl @Inject constructor(
       @ApplicationContext private val context: Context
) : TimerRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override val isRunning: StateFlow<Boolean> = TimerState.isRunning
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false) // Using mainLooper for context

    @SuppressLint("DefaultLocale")
    // Форматированное отображение времени
    override val timeDisplay: StateFlow<String> = TimerState.remainingTimeMs
        .map { ms ->
            if (ms <= 0L) {
                "00:00"
            } else {
                String.format(
                       "%02d:%02d",
                       TimeUnit.MILLISECONDS.toMinutes(ms),
                       TimeUnit.MILLISECONDS.toSeconds(ms) % 60
                )
            }
        }
        .stateIn(
               scope,
               SharingStarted.WhileSubscribed(5000),
               "00:00"
        ) // Using mainLooper for context

    override suspend fun startTimer(durationMinutes: Long) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_DURATION_MINUTES, durationMinutes)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    override suspend fun stopTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        ContextCompat.startForegroundService(context, intent)
    }

    override suspend fun resetTimer() {
        stopTimer()
        TimerState.reset()
    }
}
