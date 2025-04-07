package com.example.timernotify.domain.usecase

import com.example.timernotify.domain.repository.TimerRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * UseCase: получение форматированной строки оставшегося времени.
 */
class GetTimeDisplayUseCase @Inject constructor(private val timerRepository: TimerRepository) {
    operator fun invoke(): StateFlow<String> = timerRepository.timeDisplay
}
