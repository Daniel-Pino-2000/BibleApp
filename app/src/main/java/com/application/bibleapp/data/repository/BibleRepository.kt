package com.application.bibleapp.data.repository

import android.content.Context
import com.application.bibleapp.data.local.BibleDatabaseManager
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.BibleBooks.getBookById
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.model.toUI
import com.application.bibleapp.data.remote.BibleVersionDto
import com.application.bibleapp.data.remote.DownloadSummary
import com.application.bibleapp.data.remote.RemoteBibleDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BibleRepository(
    private val context: Context,
    private val remote: RemoteBibleDataSource
) {

    suspend fun getChapter(bookId: Int, chapter: Int, versionId: String): List<VerseUI> =
        withContext(Dispatchers.IO) {
            BibleDatabaseManager
                .getVersesByChapter(context, bookId, chapter, versionId)
                .map { it.toUI() }
        }

    suspend fun searchVerses(query: String, versionId: String): List<VerseUI> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) emptyList()
            else BibleDatabaseManager.searchVerses(query, versionId)
        }

    suspend fun getAllVersions(): List<BibleVersionDto> = remote.getAvailableVersions()

    // BibleDatabaseManager.isVersionDownloaded runs a blocking SQLite query; keep it off
    // whatever dispatcher the caller happens to be on (viewModelScope defaults to Main).
    suspend fun isVersionDownloaded(versionId: String): Boolean = withContext(Dispatchers.IO) {
        BibleDatabaseManager.isVersionDownloaded(context, versionId)
    }

    /**
     * Downloads [versionId] and persists it to the local DB.
     * Pass the same [bookNames] + [chapterCounts] lists you use elsewhere in the app.
     * [onProgress] fires with 0f–1f as chapters complete.
     */
    suspend fun downloadVersion(
        versionId: String,
        versionName: String,
        bookNames: List<String>,
        chapterCounts: List<Int>,
        onProgress: (Float) -> Unit = {}
    ): Result<DownloadSummary> = BibleDatabaseManager.downloadAndSaveVersion(
        context = context,
        versionId = versionId,
        versionName = versionName,
        bookNames = bookNames,
        chapterCounts = chapterCounts,
        remote = remote,
        onProgress = onProgress
    )
}