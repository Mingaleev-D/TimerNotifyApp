package com.example.timernotify.di

import android.app.NotificationManager
import android.content.Context
import com.example.timernotify.data.repository.TimerRepositoryImpl
import com.example.timernotify.domain.repository.TimerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
/**
 * DI-модуль, предоставляющий зависимости для таймера.
 */
@Module
@InstallIn(SingletonComponent::class)
object TimerModule {

    @Provides
    @Singleton
    fun provideTimerRepository(
           @ApplicationContext context: Context
    ): TimerRepository = TimerRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}
