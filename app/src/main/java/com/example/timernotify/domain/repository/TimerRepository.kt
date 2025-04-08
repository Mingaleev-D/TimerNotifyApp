package com.example.timernotify.domain.repository

/**
 * Интерфейс репозитория, абстрагирующего доступ к данным таймера.
 */
interface TimerRepository {
    fun setEndTime(endTime: Long)
    fun getEndTime(): Long
    fun setIsTimerRunning(isRunning: Boolean)
    fun isTimerRunning(): Boolean
}
