package com.application.bibleapp.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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

        val notification = NotificationCompat.Builder(applicationContext, DailyVerseScheduler.NOTIFICATION_CHANNEL_ID)
            // TODO: swap for a proper monochrome status-bar icon — the launcher mipmap
            // works but isn't designed for the notification tray (System will force-tint
            // it, which can look muddy depending on its actual artwork).
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(verse.reference)
            .setContentText(verse.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(verse.text))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val NOTIFICATION_ID = 1001
    }
}
