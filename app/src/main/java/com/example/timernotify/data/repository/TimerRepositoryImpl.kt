package com.example.timernotify.data.repository

import com.example.timernotify.data.manager.PrefManager
import com.example.timernotify.domain.repository.TimerRepository
import javax.inject.Inject

class TimerRepositoryImpl @Inject constructor(
       private val prefManager: PrefManager
) : TimerRepository {

    override fun setEndTime(endTime: Long) {
        prefManager.endTimeMillis = endTime
    }

    override fun getEndTime(): Long {
        return prefManager.endTimeMillis
    }

    override fun setIsTimerRunning(isRunning: Boolean) {
        prefManager.isTimerRunning = isRunning
    }

    override fun isTimerRunning(): Boolean {
        return prefManager.isTimerRunning
    }
}
