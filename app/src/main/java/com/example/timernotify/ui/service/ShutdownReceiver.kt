package com.example.timernotify.ui.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShutdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SHUTDOWN) {
            val prefs = context.getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("shutdownTime", System.currentTimeMillis())
                .putLong("remainingTime", TimerState.remainingTimeMs.value)
                .apply()
        }
    }
}

//@AndroidEntryPoint
//class ShutdownReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent?) {
//        if (intent?.action == Intent.ACTION_SHUTDOWN) {
//            val prefs = context.getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
//            prefs.edit().putLong("shutdownTime", System.currentTimeMillis()).apply()
//        }
//    }
//}
