package com.example.timernotify.domain.usecase

import com.example.timernotify.domain.repository.TimerRepository
import javax.inject.Inject
/**
 * UseCase: сброс состояния таймера.
 */
class ResetTimerUseCase @Inject constructor(private val timerRepository: TimerRepository) {
    suspend operator fun invoke() {
        timerRepository.resetTimer()
    }
}
