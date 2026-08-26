package com.application.bibleapp.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.application.bibleapp.MainActivity
import com.application.bibleapp.R
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.VerseOfTheDay
import com.application.bibleapp.data.model.resolveDailyVerseUI
import com.application.bibleapp.data.remote.HelloAoBibleDataSource
import com.application.bibleapp.data.remote.OurMannaBibleDataSource
import com.application.bibleapp.data.repository.BibleRepository

/**
 * Runs at the user's chosen local time (see [DailyVerseScheduler]) and posts a
 * notification with the daily verse, read entirely from local storage — the cached
 * reference from [com.application.bibleapp.data.local.BibleDatabaseManager] (kept
 * fresh by [DailyVerseFetchWorker]) and the verse text from whichever translation is
 * currently selected. No network call happens here.
 */
class DailyVerseNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = BibleRepository(
            context = applicationContext,
            remote = HelloAoBibleDataSource(),
            daily = OurMannaBibleDataSource()
        )

        if (repository.isNotificationEnabled()) {
            val time = repository.loadNotificationTime()
            // Read fresh each run (not the value at schedule time) so a time change in
            // Settings takes effect starting with the very next notification.
            DailyVerseScheduler.scheduleNotification(applicationContext, time.hour, time.minute)
            postNotification(repository)
        }
        return Result.success()
    }

    private suspend fun postNotification(repository: BibleRepository) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // user turned the OS permission off after enabling the setting
        }

        val versionId = repository.loadSelectedVersion()
        val ref = runCatching { repository.getDailyVerse() }.getOrElse { VerseOfTheDay.forToday() }
        val bookName = BibleBooks.getBookById(ref.bookId)?.name ?: return
        val chapterVerses = repository.getChapter(ref.bookId, ref.chapter, versionId)
        val verse = resolveDailyVerseUI(bookName, ref, chapterVerses) ?: return

        // FLAG_ACTIVITY_CLEAR_TASK, not just NEW_TASK: MainActivity has no launchMode
        // override, so without CLEAR_TASK a plain NEW_TASK intent would just bring the
        // app's existing task back to whatever screen the user last left it on. Tapping
        // this notification should always land on Home, so the old task (and whatever
        // Compose nav state it held) is discarded and MainActivity starts fresh, which
        // means the NavHost begins again at its startDestination (Home).
        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, DailyVerseScheduler.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_daily_verse)
            .setColor(BRAND_COLOR)
            .setContentTitle(applicationContext.getString(R.string.daily_verse_notification_title))
            .setContentText(verse.reference)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(verse.text)
                    .setBigContentTitle(verse.reference)
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // channel importance governs API 26+; this covers 24-25
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val NOTIFICATION_ID = 1001

        // Matches ui/theme/Color.kt's LightPrimary, so the notification reads as part of
        // the same app instead of using the OS's generic default accent.
        const val BRAND_COLOR = 0xFFA6743A.toInt()
    }
}
