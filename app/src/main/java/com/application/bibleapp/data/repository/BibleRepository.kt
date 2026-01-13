package com.application.bibleapp.data.repository

import android.content.Context
import com.application.bibleapp.data.local.BibleDatabaseManager
import com.application.bibleapp.data.model.BibleBooks.getBookById
import com.application.bibleapp.data.model.BibleSourceType
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.SelectedBibleVersion
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.model.toUI
import com.application.bibleapp.data.remote.BibleVersionDto
import com.application.bibleapp.data.remote.RemoteBibleDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class BibleRepository(
    private val context: Context,
    private val remote: RemoteBibleDataSource,
    private val currentVersionProvider: () -> SelectedBibleVersion
) {

    // Cache for recently accessed chapters: key = bookId to chapter
    private val chapterCache = mutableMapOf<Pair<Int, Int>, List<VerseUI>>()

    /**
     * Get a chapter as VerseUI list.
     * Cached if already loaded.
     */
    suspend fun getChapter(bookId: Int, chapter: Int): List<VerseUI> {

        val selected = currentVersionProvider()

        return when (selected.source) {

            BibleSourceType.LOCAL -> {
                withContext(Dispatchers.IO) {
                    BibleDatabaseManager
                        .getVersesByChapter(context, bookId, chapter)
                        .map { it.toUI() }
                }
            }

            BibleSourceType.REMOTE -> {
                val versionId = selected.id ?: return emptyList() // safety check
                remote.getChapter(
                    version = versionId,
                    book = getBookById(bookId)?.name ?: "genesis",
                    chapter = chapter
                ) ?: emptyList()
            }
        }
    }

    suspend fun searchVerses(query: String): List<VerseUI> = withContext(Dispatchers.IO) {
        if (query.isBlank()) emptyList()
        else BibleDatabaseManager.searchVerses(query)
    }

    suspend fun getAllVersions(): List<BibleVersionDto> {
        return remote.getAvailableVersions()
    }

}

