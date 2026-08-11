package com.application.bibleapp.data.repository

import android.content.Context
import com.application.bibleapp.data.local.BibleDatabaseManager
import com.application.bibleapp.data.local.VersionDownloadSummary
import com.application.bibleapp.data.model.BibleTranslation
import com.application.bibleapp.data.model.DEFAULT_VERSION
import com.application.bibleapp.data.model.DownloadedVersionInfo
import com.application.bibleapp.data.model.Footnote
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.model.toUI
import com.application.bibleapp.data.remote.BibleRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    private val remote: BibleRemoteDataSource
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

    private companion object {
        const val KEY_SELECTED_VERSION = "selected_version_id"
    }
}