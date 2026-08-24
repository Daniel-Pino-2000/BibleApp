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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/** The last book/chapter/verse the user had open, restored on app launch. */
data class ReadingPosition(val bookId: Int, val chapter: Int, val verse: Int)

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
     * The daily verse reference, refetched at most once per calendar day (cached in
     * [prefs]) so repeated app opens don't re-hit OurManna. Any failure from [daily]
     * propagates to the caller — [BibleViewModel] decides the offline/error fallback.
     */
    suspend fun getDailyVerse(): DailyVerseRef = withContext(Dispatchers.IO) {
        val today = todayKey()
        val cached = if (prefs.getString(KEY_DAILY_VERSE_DATE, null) == today) readCachedDailyVerse() else null
        cached ?: daily.getDailyVerse().also { cacheDailyVerse(today, it) }
    }

    private fun readCachedDailyVerse(): DailyVerseRef? {
        val bookId = prefs.getInt(KEY_DAILY_VERSE_BOOK, -1)
        if (bookId == -1) return null
        return DailyVerseRef(
            bookId = bookId,
            chapter = prefs.getInt(KEY_DAILY_VERSE_CHAPTER, 1),
            startVerse = prefs.getInt(KEY_DAILY_VERSE_START, 1),
            endVerse = prefs.getInt(KEY_DAILY_VERSE_END, -1).takeIf { it != -1 }
        )
    }

    private fun cacheDailyVerse(today: String, ref: DailyVerseRef) {
        prefs.edit()
            .putString(KEY_DAILY_VERSE_DATE, today)
            .putInt(KEY_DAILY_VERSE_BOOK, ref.bookId)
            .putInt(KEY_DAILY_VERSE_CHAPTER, ref.chapter)
            .putInt(KEY_DAILY_VERSE_START, ref.startVerse)
            .putInt(KEY_DAILY_VERSE_END, ref.endVerse ?: -1)
            .apply()
    }

    // Year + day-of-year rather than a full date format — this only needs to change
    // once every 24h, not represent a real calendar date.
    private fun todayKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
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
        const val KEY_DAILY_VERSE_DATE = "daily_verse_date"
        const val KEY_DAILY_VERSE_BOOK = "daily_verse_book_id"
        const val KEY_DAILY_VERSE_CHAPTER = "daily_verse_chapter"
        const val KEY_DAILY_VERSE_START = "daily_verse_start"
        const val KEY_DAILY_VERSE_END = "daily_verse_end"
    }
}