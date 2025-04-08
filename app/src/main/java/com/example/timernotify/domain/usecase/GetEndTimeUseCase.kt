package com.example.timernotify.domain.usecase

import com.example.timernotify.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetEndTimeUseCase @Inject constructor(
       private val timerRepository: TimerRepository
) {

    operator fun invoke(): Flow<Long> = flow {
        emit(timerRepository.getEndTime())
    }
}
