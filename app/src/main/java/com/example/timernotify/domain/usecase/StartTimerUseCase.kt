package com.example.timernotify.domain.usecase

import com.example.timernotify.domain.repository.TimerRepository
import javax.inject.Inject
/**
 * UseCase: старт таймера на заданное количество минут.
 */
class StartTimerUseCase @Inject constructor(private val timerRepository: TimerRepository) {
    suspend operator fun invoke(durationMinutes: Long) {
        timerRepository.startTimer(durationMinutes)
    }
}
