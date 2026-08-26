package com.application.bibleapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.application.bibleapp.worker.DailyVerseScheduler

/**
 * Runs the two one-time app-startup jobs the daily-verse feature needs:
 * - Create the notification channel (a no-op pre-API 26, required from O onward before
 *   any notification on [DailyVerseScheduler.NOTIFICATION_CHANNEL_ID] can show).
 * - Make sure the daily-fetch background job is scheduled. [ExistingWorkPolicy.KEEP]
 *   (the default) means this never disrupts a fetch already in flight — it only fills
 *   in the schedule if it's somehow missing, it doesn't reset it on every app open.
 *
 * The notification reminder job is *not* (re)scheduled here — it's toggle-driven, set
 * up by [com.application.bibleapp.data.repository.BibleRepository.setNotificationEnabled]
 * when the user turns it on in Settings, and self-perpetuating after that.
 */
class BibleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createDailyVerseNotificationChannel()
        DailyVerseScheduler.scheduleDailyFetch(this)
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
