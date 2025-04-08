package com.example.timernotify.domain.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.timernotify.data.manager.Constants
import com.example.timernotify.data.service.TimerService
import com.example.timernotify.domain.repository.TimerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StartTimerUseCase @Inject constructor(
       private val timerRepository: TimerRepository,
       @ApplicationContext private val context: Context
) {
    operator fun invoke(minutes: Long) {
        val endTime = System.currentTimeMillis() + minutes * 60 * 1000
        timerRepository.setEndTime(endTime)
        timerRepository.setIsTimerRunning(true)

        val intent = Intent(context, TimerService::class.java)
        intent.putExtra(Constants.KEY_END_TIME, endTime)
        ContextCompat.startForegroundService(context, intent)
    }
}
