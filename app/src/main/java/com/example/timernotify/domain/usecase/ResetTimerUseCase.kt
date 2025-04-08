package com.example.timernotify.domain.usecase

import com.example.timernotify.domain.repository.TimerRepository
import javax.inject.Inject

class ResetTimerUseCase @Inject constructor(
       private val timerRepository: TimerRepository,
       private val stopTimerUseCase: StopTimerUseCase
) {
    operator fun invoke() {
        stopTimerUseCase()
        timerRepository.setEndTime(0L)
    }
}
