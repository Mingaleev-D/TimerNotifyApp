package com.example.timernotify.domain.usecase

import com.example.timernotify.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class IsTimerRunningUseCase @Inject constructor(
       private val timerRepository: TimerRepository
) {
    operator fun invoke(): Flow<Boolean> = flow {
        emit(timerRepository.isTimerRunning())
    }
}
