package com.example.timernotify.domain.usecase

import com.example.timernotify.domain.repository.TimerRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
/**
 * UseCase: получение текущего статуса таймера (запущен/не запущен).
 */
class GetTimerStateUseCase @Inject constructor(private val timerRepository: TimerRepository) {
    operator fun invoke(): StateFlow<Boolean> = timerRepository.isRunning
}
