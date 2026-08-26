package com.application.bibleapp.data.repository

import android.content.Context
import com.application.bibleapp.data.local.BibleDatabaseManager
import com.application.bibleapp.data.local.VersionDownloadSummary
import com.application.bibleapp.data.model.BibleTranslation
import com.application.bibleapp.data.model.DEFAULT_VERSION
import com.application.bibleapp.data.model.DailyVerseRef
import com.application.bibleapp.data.model.DownloadedVersionInfo
import com.application.bibleapp.data.model.Footnote
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.model.toUI
import com.application.bibleapp.data.remote.BibleRemoteDataSource
import com.application.bibleapp.data.remote.DailyVerseDataSource
import com.application.bibleapp.ui.theme.ThemeMode
import com.application.bibleapp.ui.theme.VerseTextScale
import com.application.bibleapp.utils.NetworkUtils
import com.application.bibleapp.worker.DailyVerseScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Calendar

/**
 * Year + day-of-year — descriptive metadata only (which day a cached row was fetched
 * for), not used to decide cache hit/miss: freshness is [DailyVerseFetchWorker]'s job
 * now, this is just what gets stamped on the row when it writes one.
 */
internal fun dailyVerseDateKey(now: Calendar = Calendar.getInstance()): String =
    "${now.get(Calendar.YEAR)}-${now.get(Calendar.DAY_OF_YEAR)}"

/** The last book/chapter/verse the user had open, restored on app launch. */
data class ReadingPosition(val bookId: Int, val chapter: Int, val verse: Int)

/** The user's chosen local wall-clock time for the daily verse reminder. */
data class NotificationTime(val hour: Int, val minute: Int)

/**
 * Sits between [BibleViewModel][com.application.bibleapp.viewmodel.BibleViewModel] and
 * the two data sources it coordinates: [remote] (the [BibleRemoteDataSource]
 * interface, currently backed by the helloao API client) for the translation
 * catalog and downloads, and [BibleDatabaseManager] for everything already on
 * disk. The ViewModel never talks to either directly — this is the one place
 * that decides *where* a piece of data comes from, so swapping the remote
 * source (or adding a cache layer) later only touches this class.
 */
class BibleRepository(
    private val context: Context,
    private val remote: BibleRemoteDataSource,
    private val daily: DailyVerseDataSource
) {
    private val prefs by lazy {
        context.applicationContext.getSharedPreferences("bible_prefs", Context.MODE_PRIVATE)
    }

    suspend fun getChapter(bookId: Int, chapter: Int, versionId: String): List<VerseUI> =
        withContext(Dispatchers.IO) {
            BibleDatabaseManager
                .getVersesByChapter(context, bookId, chapter, versionId)
                .map { it.toUI() }
        }

    suspend fun getFootnotes(bookId: Int, chapter: Int, versionId: String): List<Footnote> =
        withContext(Dispatchers.IO) {
            BibleDatabaseManager.getFootnotesForChapter(context, bookId, chapter, versionId)
        }

    suspend fun searchVerses(query: String, versionId: String): List<VerseUI> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) emptyList()
            else BibleDatabaseManager.searchVerses(query, versionId)
        }

    /**
     * Reads the reference [DailyVerseFetchWorker][com.application.bibleapp.worker.DailyVerseFetchWorker]
     * cached in the local DB — a plain read, no network involved. Falls back to a
     * direct (uncached) [refreshDailyVerse] only if nothing has been cached yet, e.g.
     * right after install before the background worker's first run.
     */
    suspend fun getDailyVerse(): DailyVerseRef = withContext(Dispatchers.IO) {
        BibleDatabaseManager.getCachedDailyVerse(context) ?: refreshDailyVerse()
    }

    /**
     * Always fetches fresh from [daily] and overwrites the cached row — this is what
     * the daily background worker calls to replace the previous day's verse. Skips the
     * network call entirely when there's no connectivity rather than waiting on a
     * request that's bound to time out.
     */
    suspend fun refreshDailyVerse(): DailyVerseRef = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isOnline(context)) {
            throw IOException("No internet connection — skipping daily verse fetch")
        }
        val fetched = daily.getDailyVerse()
        BibleDatabaseManager.saveCachedDailyVerse(context, dailyVerseDateKey(), fetched)
        fetched
    }

    fun isNotificationEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, false)

    fun loadNotificationTime(): NotificationTime = NotificationTime(
        hour = prefs.getInt(KEY_NOTIFICATION_HOUR, DEFAULT_NOTIFICATION_HOUR),
        minute = prefs.getInt(KEY_NOTIFICATION_MINUTE, DEFAULT_NOTIFICATION_MINUTE)
    )

    /** Persists the toggle and (de)schedules the reminder worker in the same place — the
     *  ViewModel/UI never has to remember to do both. */
    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
        if (enabled) {
            val time = loadNotificationTime()
            DailyVerseScheduler.scheduleNotification(context, time.hour, time.minute)
        } else {
            DailyVerseScheduler.cancelNotification(context)
        }
    }

    /** No-ops on the schedule if the reminder is currently off — it'll pick up the new time
     *  whenever it's next turned on. */
    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_NOTIFICATION_HOUR, hour)
            .putInt(KEY_NOTIFICATION_MINUTE, minute)
            .apply()
        if (isNotificationEnabled()) {
            DailyVerseScheduler.scheduleNotification(context, hour, minute)
        }
    }

    suspend fun getAllVersions(): List<BibleTranslation> = remote.getAvailableTranslations()

    // BibleDatabaseManager.isVersionDownloaded runs a blocking SQLite query; keep it off
    // whatever dispatcher the caller happens to be on (viewModelScope defaults to Main).
    suspend fun isVersionDownloaded(versionId: String): Boolean = withContext(Dispatchers.IO) {
        BibleDatabaseManager.isVersionDownloaded(context, versionId)
    }

    /** All locally downloaded versions, keyed for the picker to badge without a query per row. */
    suspend fun getDownloadedVersions(): List<DownloadedVersionInfo> = withContext(Dispatchers.IO) {
        BibleDatabaseManager.getDownloadedVersions(context)
    }

    /** Downloads [translationId] (a single bulk request under the hood) and persists it to the local DB. */
    suspend fun downloadVersion(
        translationId: String,
        onProgress: (Float) -> Unit = {}
    ): Result<VersionDownloadSummary> = BibleDatabaseManager.downloadAndSaveVersion(
        context = context,
        translationId = translationId,
        remote = remote,
        onProgress = onProgress
    )

    /** Deletes then re-downloads [translationId] — the only way to pick up a newer schema_version. */
    suspend fun redownloadVersion(
        translationId: String,
        onProgress: (Float) -> Unit = {}
    ): Result<VersionDownloadSummary> {
        withContext(Dispatchers.IO) { BibleDatabaseManager.deleteDownloadedVersion(context, translationId) }
        return downloadVersion(translationId, onProgress)
    }

    /** Persisted across process restarts so the app reopens on the last version the user picked. */
    fun saveSelectedVersion(versionId: String) {
        prefs.edit().putString(KEY_SELECTED_VERSION, versionId).apply()
    }

    fun loadSelectedVersion(): String = prefs.getString(KEY_SELECTED_VERSION, DEFAULT_VERSION.id) ?: DEFAULT_VERSION.id

    fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun loadThemeMode(): ThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
    }

    fun saveVerseTextScale(scale: VerseTextScale) {
        prefs.edit().putFloat(KEY_VERSE_TEXT_SCALE, scale.multiplier).apply()
    }

    fun loadVerseTextScale(): VerseTextScale =
        VerseTextScale.fromMultiplier(prefs.getFloat(KEY_VERSE_TEXT_SCALE, VerseTextScale.DEFAULT.multiplier))

    /** Persisted across process restarts so the app reopens on the last book/chapter/verse read. */
    fun saveReadingPosition(bookId: Int, chapter: Int, verse: Int) {
        prefs.edit()
            .putInt(KEY_READING_BOOK, bookId)
            .putInt(KEY_READING_CHAPTER, chapter)
            .putInt(KEY_READING_VERSE, verse)
            .apply()
    }

    fun loadReadingPosition(): ReadingPosition = ReadingPosition(
        bookId = prefs.getInt(KEY_READING_BOOK, 1),
        chapter = prefs.getInt(KEY_READING_CHAPTER, 1),
        verse = prefs.getInt(KEY_READING_VERSE, 1)
    )

    private companion object {
        const val KEY_SELECTED_VERSION = "selected_version_id"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_VERSE_TEXT_SCALE = "verse_text_scale"
        const val KEY_READING_BOOK = "reading_book_id"
        const val KEY_READING_CHAPTER = "reading_chapter"
        const val KEY_READING_VERSE = "reading_verse"
        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        const val KEY_NOTIFICATION_HOUR = "notification_hour"
        const val KEY_NOTIFICATION_MINUTE = "notification_minute"
        const val DEFAULT_NOTIFICATION_HOUR = 8
        const val DEFAULT_NOTIFICATION_MINUTE = 0
    }
}