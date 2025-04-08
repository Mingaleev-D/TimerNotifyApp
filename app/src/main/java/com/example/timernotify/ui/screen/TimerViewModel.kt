package com.example.timernotify.ui.screen

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timernotify.data.manager.Constants
import com.example.timernotify.data.service.TimerService
import com.example.timernotify.domain.usecase.GetEndTimeUseCase
import com.example.timernotify.domain.usecase.IsTimerRunningUseCase
import com.example.timernotify.domain.usecase.ResetTimerUseCase
import com.example.timernotify.domain.usecase.StartTimerUseCase
import com.example.timernotify.domain.usecase.StopTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
       private val startTimerUseCase: StartTimerUseCase,
       private val stopTimerUseCase: StopTimerUseCase,
       private val resetTimerUseCase: ResetTimerUseCase,
       private val isTimerRunningUseCase: IsTimerRunningUseCase,
       private val getEndTimeUseCase: GetEndTimeUseCase,
       @ApplicationContext private val context: Context
) : ViewModel() {

    private val _timeInput = MutableStateFlow("")
    val timeInput: StateFlow<String> = _timeInput
    private val _remainingTime = MutableStateFlow("00:00")
    val remainingTime: StateFlow<String> = _remainingTime
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning
    private var timerUpdateJob: Job? = null

    init {
        observeTimerRunningState()
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            getEndTimeUseCase().collectLatest { endTime ->
                if (endTime > System.currentTimeMillis()) {
                    startForegroundService(endTime)
                }
            }
            isTimerRunningUseCase().collectLatest { isRunning ->
                _isRunning.value = isRunning
            }
        }
    }

    private fun observeTimerRunningState() {
        viewModelScope.launch {
            isTimerRunningUseCase().collectLatest { isRunning ->
                _isRunning.value = isRunning
                if (isRunning) {
                    startTimerUpdates()
                } else {
                    timerUpdateJob?.cancel()
                }
            }
        }
    }

    private fun startTimerUpdates() {
        timerUpdateJob = viewModelScope.launch {
            while (_isRunning.value) {
                val endTime = getEndTimeUseCase.invoke().first() // Получаем текущее значение
                if (endTime > System.currentTimeMillis()) {
                    val remaining = endTime - System.currentTimeMillis()
                    updateRemainingTime(remaining)
                } else if (_isRunning.value) {
                    stopTimer()
                    _remainingTime.value = "00:00"
                }
                delay(1000)
            }
        }
    }

    private fun startForegroundService(endTime: Long) {
        val intent = Intent(context, TimerService::class.java)
        intent.putExtra(Constants.KEY_END_TIME, endTime)
        ContextCompat.startForegroundService(context, intent)
    }

    fun updateTimeInput(input: String) {
        _timeInput.value = input.filter { it.isDigit() }
    }

    fun startTimer() {
        val minutes = timeInput.value.toLongOrNull() ?: return
        if (minutes > 0) {
            startTimerUseCase(minutes)
            _isRunning.value = true
            startTimerUpdates()
        }
    }

    fun stopTimer() {
        stopTimerUseCase()
        _isRunning.value = false
    }

    fun resetTimer() {
        resetTimerUseCase()
        _timeInput.value = ""
        _remainingTime.value = "00:00"
    }

    fun updateRemainingTime(timeInMillis: Long) {
        val remainingSeconds = (timeInMillis / 1000).toInt()
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        _remainingTime.value = String.format("%02d:%02d", minutes, seconds)
    }
}
