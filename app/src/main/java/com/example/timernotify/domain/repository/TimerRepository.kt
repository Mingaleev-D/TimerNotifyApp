package com.example.timernotify.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс репозитория, абстрагирующего доступ к данным таймера.
 */
interface TimerRepository {
    val isRunning: StateFlow<Boolean>
    val timeDisplay: StateFlow<String>
    suspend fun startTimer(durationMinutes: Long)
    suspend fun stopTimer()
    suspend fun resetTimer()
}
