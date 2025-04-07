package com.example.timernotify.ui.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
/**
 * Singleton-объект, хранящий текущее состояние таймера в памяти:
 * - Оставшееся время в миллисекундах
 * - Флаг активности таймера
 * - Время окончания (если задано)
 *
 * Используется сервисом и потенциально UI, если тебе нужно отобразить статус таймера.
 */
object TimerState {
    private val _remainingTimeMs = MutableStateFlow(0L)
    val remainingTimeMs = _remainingTimeMs.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _targetEndTimeMs = MutableStateFlow<Long?>(null)
    val targetEndTimeMs = _targetEndTimeMs.asStateFlow()

    fun updateRemainingTime(timeMs: Long) {
        _remainingTimeMs.value = timeMs
    }

    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }

    fun setTargetEndTime(timeMs: Long?) {
        _targetEndTimeMs.value = timeMs
    }

    fun reset() {
        _remainingTimeMs.value = 0L
        _isRunning.value = false
        _targetEndTimeMs.value = null
    }
}
