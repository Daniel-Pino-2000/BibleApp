package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.utils.VerseUtils.toVerseUI
import com.application.bibleapp.utils.VerseUtils.toVerseUIList
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemoteBibleDataSource(
    private val baseUrl: String = "https://cdn.jsdelivr.net/gh/wldeh/bible-api"
) {
    private val client = HttpClientProvider.client

    suspend fun getVerse(
        version: String,
        bookId: Int,
        chapter: Int,
        verseNumber: Int
    ): BibleVerse? {
        return try {
            val url = "$baseUrl/bibles/$version/books/$bookId/chapters/$chapter/verses/$verseNumber.json"
            val response: ApiVerseDto = client.get(url).body()

            response.toBibleVerse(bookId, chapter)
        } catch (e: Exception) {
            null // never crash app on network error
        }
    }



    suspend fun getChapter(
        version: String,
        book: String,
        chapter: Int
    ): List<VerseUI>? {
        return try {
            val url = "$baseUrl/bibles/$version/books/$book/chapters/$chapter.json"
            val apiChapter: ApiChapterDto = client.get(url).body()

            // Generate bookIdMap from your BibleBooks object
            val bookIdMap = BibleBooks.allBooks.associate { it.name to it.id }

            apiChapter.data.map { it.toVerseUI(bookIdMap) }
        } catch (e: Exception) {
            null
        }
    }


    suspend fun getAvailableVersions(): List<BibleVersionDto> {
        return try {
            client.get(
                "$baseUrl/bibles/bibles.json"
            ).body()
        } catch (e: Exception) {
            emptyList()
        }
    }
}