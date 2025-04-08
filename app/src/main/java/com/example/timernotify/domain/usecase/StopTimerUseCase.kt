package com.example.timernotify.domain.usecase

import android.content.Context
import android.content.Intent
import com.example.timernotify.data.service.TimerService
import com.example.timernotify.domain.repository.TimerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StopTimerUseCase @Inject constructor(
       private val timerRepository: TimerRepository,
       @ApplicationContext private val context: Context
) {
    operator fun invoke() {
        timerRepository.setIsTimerRunning(false)
        context.stopService(Intent(context, TimerService::class.java))
    }
}
