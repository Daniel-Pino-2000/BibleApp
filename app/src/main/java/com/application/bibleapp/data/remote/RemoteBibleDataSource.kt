package com.application.bibleapp.data.remote

import android.util.Log
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemoteBibleDataSource(
    private val baseUrl: String = "https://cdn.jsdelivr.net/gh/wldeh/bible-api"
) {

    private val client = HttpClientProvider.client

    /**
     * Fetch a single verse from the remote API
     */
    suspend fun getVerse(
        version: String,
        bookName: String,
        chapter: Int,
        verseNumber: Int
    ): BibleVerse? {

        return try {

            val url =
                "$baseUrl/bibles/$version/books/${bookName.lowercase()}/chapters/$chapter/verses/$verseNumber.json"

            Log.d("BibleAPI", "Fetching verse from $url")

            val response: ApiVerseDto = client.get(url).body()

            val bookId = BibleBooks.getBookByName(bookName)?.id
                ?: throw IllegalArgumentException("Unknown book: $bookName")

            response.toBibleVerse(bookId, chapter)

        } catch (e: Exception) {

            Log.e(
                "BibleAPI",
                "ERROR fetching verse: ${e::class.simpleName} - ${e.message}"
            )

            null
        }
    }

    /**
     * Fetch an entire chapter
     */
    suspend fun getChapter(
        version: String,
        book: String,
        chapter: Int
    ): List<VerseUI>? {

        return try {

            val url =
                "$baseUrl/bibles/$version/books/${book.lowercase()}/chapters/$chapter.json"

            Log.d("BibleAPI", "Fetching chapter from $url")

            val apiChapter: ApiChapterDto = client.get(url).body()

            val bookId = BibleBooks.getBookByName(book)?.id
                ?: throw IllegalArgumentException("Unknown book: $book")

            apiChapter.data.map { verse ->
                verse.toUIVerse(bookId, chapter)
            }

        } catch (e: Exception) {

            Log.e(
                "BibleAPI",
                "ERROR fetching chapter: ${e::class.simpleName} - ${e.message}"
            )

            null
        }
    }

    /**
     * Fetch list of available Bible versions
     */
    suspend fun getAvailableVersions(): List<BibleVersionDto> {

        return try {

            val url = "$baseUrl/bibles/bibles.json"

            Log.d("BibleAPI", "Fetching versions from $url")

            val versions: List<BibleVersionDto> = client.get(url).body()

            Log.d("BibleAPI", "Loaded ${versions.size} Bible versions")

            versions

        } catch (e: Exception) {

            Log.e(
                "BibleAPI",
                "ERROR fetching versions: ${e::class.simpleName} - ${e.message}"
            )

            emptyList()
        }
    }
}