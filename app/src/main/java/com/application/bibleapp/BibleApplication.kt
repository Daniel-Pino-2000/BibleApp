package com.application.bibleapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.application.bibleapp.worker.DailyVerseScheduler

/**
 * Runs the app-startup jobs the daily-verse feature needs:
 * - Create the notification channel (a no-op pre-API 26, required from O onward before
 *   any notification on [DailyVerseScheduler.NOTIFICATION_CHANNEL_ID] can show).
 * - [ensureDailyVerseJobsScheduled] re-arms the fetch job and, if enabled, the
 *   reminder notification job — see its doc for why this needs to run here (every
 *   normal process start) *and* from [com.application.bibleapp.receiver.BootCompletedReceiver]
 *   (so it also happens after a reboot the user hasn't opened the app since).
 */
class BibleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createDailyVerseNotificationChannel()
        ensureDailyVerseJobsScheduled(this)
    }

    private fun createDailyVerseNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            DailyVerseScheduler.NOTIFICATION_CHANNEL_ID,
            "Daily Verse",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Your daily Bible verse reminder"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
