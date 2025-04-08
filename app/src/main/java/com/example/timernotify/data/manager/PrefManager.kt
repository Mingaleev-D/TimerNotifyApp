package com.example.timernotify.data.manager

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
           Constants.PREFS_NAME,
           Context.MODE_PRIVATE
    )
    var endTimeMillis: Long
        get() = prefs.getLong(Constants.KEY_END_TIME, 0L)
        set(value) = prefs.edit().putLong(Constants.KEY_END_TIME, value).apply()
    var isTimerRunning: Boolean
        get() = prefs.getBoolean(Constants.KEY_IS_RUNNING, false)
        set(value) = prefs.edit().putBoolean(Constants.KEY_IS_RUNNING, value).apply()
    var lastUpdateMillis: Long
        get() = prefs.getLong(Constants.KEY_LAST_UPDATE, 0L)
        set(value) = prefs.edit().putLong(Constants.KEY_LAST_UPDATE, value).apply()

}

object Constants {

    const val PREFS_NAME = "TimerPrefs"
    const val KEY_END_TIME = "end_time"
    const val KEY_IS_RUNNING = "is_running"
    const val KEY_LAST_UPDATE = "last_update_time"
}
