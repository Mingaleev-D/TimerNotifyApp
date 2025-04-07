package com.example.timernotify.ui.screen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timernotify.domain.usecase.GetTimeDisplayUseCase
import com.example.timernotify.domain.usecase.GetTimerStateUseCase
import com.example.timernotify.domain.usecase.ResetTimerUseCase
import com.example.timernotify.domain.usecase.StartTimerUseCase
import com.example.timernotify.domain.usecase.StopTimerUseCase
import com.example.timernotify.ui.service.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ViewModel, управляющая логикой таймера.
 * Использует use case'ы для старта, остановки и сброса таймера.
 * Также предоставляет UI-данные: форматированное время и флаг активности.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
       private val startTimerUseCase: StartTimerUseCase,
       private val stopTimerUseCase: StopTimerUseCase,
       private val resetTimerUseCase: ResetTimerUseCase,
       getTimeDisplayUseCase: GetTimeDisplayUseCase,
       getTimerStateUseCase: GetTimerStateUseCase
) : ViewModel() {

    val isRunning = getTimerStateUseCase()
    val timeDisplay = getTimeDisplayUseCase()
    var inputMinutes by mutableStateOf("")

    fun startTimer() {
        val minutes = inputMinutes.toLongOrNull() ?: return
        if (minutes <= 0) return
        Log.d("TimerViewModel", "Start Timer with $minutes minutes")
        viewModelScope.launch {
            startTimerUseCase(minutes)
        }
    }

    fun stopTimer() {
        viewModelScope.launch {
            stopTimerUseCase()
        }
    }

    fun resetTimer() {
        viewModelScope.launch {
            resetTimerUseCase()
        }
    }
}
