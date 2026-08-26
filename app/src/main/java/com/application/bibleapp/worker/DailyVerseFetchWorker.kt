package com.application.bibleapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkerParameters
import com.application.bibleapp.data.remote.HelloAoBibleDataSource
import com.application.bibleapp.data.remote.OurMannaBibleDataSource
import com.application.bibleapp.data.remote.isRetriableNetworkError
import com.application.bibleapp.data.repository.BibleRepository

/**
 * Runs once a day (see [DailyVerseScheduler]) to refresh the cached daily-verse
 * reference in the local DB, so the Home screen reads from SQLite on every open
 * instead of hitting OurManna directly. Always re-arms itself for the next occurrence
 * when it's done — a bad day (network down, OurManna returning something unparseable)
 * shouldn't stop future days from trying again; [BibleRepository]'s read path falls
 * back gracefully in the meantime.
 */
class DailyVerseFetchWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = BibleRepository(
            context = applicationContext,
            remote = HelloAoBibleDataSource(),
            daily = OurMannaBibleDataSource()
        )

        val outcome = runCatching { repository.refreshDailyVerse() }

        return outcome.fold(
            onSuccess = {
                DailyVerseScheduler.scheduleDailyFetch(applicationContext, ExistingWorkPolicy.REPLACE)
                Result.success()
            },
            onFailure = { e ->
                Log.w(TAG, "Daily verse refresh failed (attempt ${runAttemptCount + 1}): ${e.message}")
                if (isRetriableNetworkError(e) && runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    // Let WorkManager's own exponential backoff retry this same run later
                    // today — no need to touch the schedule for tomorrow yet.
                    Result.retry()
                } else {
                    // Giving up on today, but still re-arm for tomorrow rather than going
                    // silent forever on a single bad or permanently-unparseable response.
                    DailyVerseScheduler.scheduleDailyFetch(applicationContext, ExistingWorkPolicy.REPLACE)
                    Result.failure()
                }
            }
        )
    }

    private companion object {
        const val TAG = "DailyVerseFetchWorker"
        const val MAX_RETRY_ATTEMPTS = 3
    }
}
