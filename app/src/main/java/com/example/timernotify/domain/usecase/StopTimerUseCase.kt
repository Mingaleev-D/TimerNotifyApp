package com.example.timernotify.domain.usecase

import com.example.timernotify.domain.repository.TimerRepository
import javax.inject.Inject
/**
 * UseCase: остановка текущего таймера.
 */
class StopTimerUseCase @Inject constructor(private val timerRepository: TimerRepository) {
    suspend operator fun invoke() {
        timerRepository.stopTimer()
    }
}
