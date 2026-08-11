package com.application.bibleapp.data.remote

import android.util.Log
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

class RemoteBibleDataSource(
    private val baseUrl: String = "https://cdn.jsdelivr.net/gh/wldeh/bible-api"
) {

    private val client = HttpClientProvider.client

    /**
     * Book/version slugs on the wldeh CDN are lowercase alphanumerics (and parentheses
     * for a couple of apocryphal titles) — no spaces or path separators.
     */
    private val versionIdPattern = Regex("^[a-zA-Z0-9-]+$")
    private val bookSlugPattern = Regex("^[a-z0-9()]+$")

    private fun isValidVersionId(version: String) = version.isNotBlank() && versionIdPattern.matches(version)
    private fun isValidBookSlug(book: String) = book.isNotBlank() && bookSlugPattern.matches(book)

    suspend fun getVerse(
        version: String,
        bookName: String,
        chapter: Int,
        verseNumber: Int
    ): BibleVerse? {
        val slug = bookName.lowercase().replace(" ", "")
        if (!isValidVersionId(version) || !isValidBookSlug(slug) || chapter < 1 || verseNumber < 1) {
            Log.e("BibleAPI", "Invalid getVerse params: version=$version, book=$bookName, chapter=$chapter, verse=$verseNumber")
            return null
        }
        return try {
            val url = "$baseUrl/bibles/$version/books/$slug/chapters/$chapter/verses/$verseNumber.json"
            Log.d("BibleAPI", "Fetching verse from $url")
            val response: ApiVerseDto = client.get(url).body()
            val bookId = BibleBooks.getBookByName(bookName)?.id
                ?: throw IllegalArgumentException("Unknown book: $bookName")
            response.toBibleVerse(bookId, chapter)
        } catch (e: Exception) {
            Log.e("BibleAPI", "ERROR fetching verse: ${e::class.simpleName} - ${e.message}")
            null
        }
    }

    /**
     * Fetches a chapter — used by BibleDatabaseManager during version download.
     * [book] must already be the API slug (see [com.application.bibleapp.data.model.apiSlug]),
     * not a display name.
     */
    suspend fun getChapterDto(
        version: String,
        book: String,
        chapter: Int
    ): ChapterFetchResult {
        if (!isValidVersionId(version) || !isValidBookSlug(book) || chapter < 1) {
            val message = "Invalid request parameters: version=$version, book=$book, chapter=$chapter"
            Log.e("BibleAPI", message)
            return ChapterFetchResult.Error(message)
        }
        return try {
            val url = "$baseUrl/bibles/$version/books/$book/chapters/$chapter.json"
            Log.d("BibleAPI", "Fetching chapter DTO from $url")
            val response = client.get(url)
            when (response.status) {
                HttpStatusCode.OK -> ChapterFetchResult.Success(response.body())
                HttpStatusCode.NotFound -> ChapterFetchResult.NotFound
                else -> ChapterFetchResult.Error("Server returned ${response.status}")
            }
        } catch (e: Exception) {
            Log.e("BibleAPI", "ERROR fetching chapter DTO ($version/$book/$chapter): ${e::class.simpleName} - ${e.message}")
            ChapterFetchResult.Error(e.message ?: e::class.simpleName ?: "Unknown network error")
        }
    }

    /**
     * Throws on network/parsing failure so callers can distinguish "no versions"
     * (a legitimate empty list) from "couldn't fetch the list at all".
     */
    suspend fun getAvailableVersions(): List<BibleVersionDto> {
        val url = "$baseUrl/bibles/bibles.json"
        Log.d("BibleAPI", "Fetching versions from $url")
        val versions: List<BibleVersionDto> = client.get(url).body()
        Log.d("BibleAPI", "Loaded ${versions.size} Bible versions")
        return versions
    }
}