package com.application.bibleapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingWorkPolicy
import com.application.bibleapp.data.remote.HelloAoBibleDataSource
import com.application.bibleapp.data.remote.OurMannaBibleDataSource
import com.application.bibleapp.data.repository.BibleRepository
import com.application.bibleapp.worker.DailyVerseScheduler

/**
 * Runs the app-startup jobs the daily-verse feature needs:
 * - Create the notification channel (a no-op pre-API 26, required from O onward before
 *   any notification on [DailyVerseScheduler.NOTIFICATION_CHANNEL_ID] can show).
 * - Make sure the daily-fetch background job is scheduled. [ExistingWorkPolicy.KEEP]
 *   means this never disrupts a fetch already in flight — it only fills in the
 *   schedule if it's somehow missing, it doesn't reset it on every app open.
 * - Re-arm the notification reminder job the same way, if the user has it turned on.
 *   It's normally toggle-driven and self-perpetuating (see
 *   [com.application.bibleapp.data.repository.BibleRepository.setNotificationEnabled]
 *   and [com.application.bibleapp.worker.DailyVerseNotificationWorker]), but that chain
 *   lives entirely inside WorkManager's persisted job — an OEM battery manager or a
 *   Doze-restricted standby bucket can silently drop it, and nothing would ever
 *   re-arm it otherwise. KEEP means this is a no-op whenever the job is already
 *   pending, same as the fetch job above.
 */
class BibleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createDailyVerseNotificationChannel()
        DailyVerseScheduler.scheduleDailyFetch(this)
        rearmNotificationIfEnabled()
    }

    private fun rearmNotificationIfEnabled() {
        val repository = BibleRepository(
            context = this,
            remote = HelloAoBibleDataSource(),
            daily = OurMannaBibleDataSource()
        )
        if (repository.isNotificationEnabled()) {
            val time = repository.loadNotificationTime()
            DailyVerseScheduler.scheduleNotification(this, time.hour, time.minute, ExistingWorkPolicy.KEEP)
        }
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
