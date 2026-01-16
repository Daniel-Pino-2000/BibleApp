package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemoteBibleDataSource(
    private val baseUrl: String = "https://raw.githubusercontent.com/wldeh/bible-api/master"
) {
    private val client = HttpClientProvider.client

    suspend fun getVerse(
        version: String,
        bookName: String, // Use book name, not ID
        chapter: Int,
        verseNumber: Int
    ): BibleVerse? {
        return try {
            val url = "$baseUrl/bibles/$version/books/${bookName.lowercase()}/chapters/$chapter/verses/$verseNumber.json"
            val response: ApiVerseDto = client.get(url).body()
            response.toBibleVerse(BibleBooks.getBookByName(bookName)?.id ?: 0, chapter)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getChapter(
        version: String,
        book: String,
        chapter: Int
    ): List<VerseUI>? {
        return try {
            val url = "$baseUrl/bibles/$version/books/${book.lowercase()}/chapters/$chapter.json"
            val apiChapter: ApiChapterDto = client.get(url).body()
            val bookId = BibleBooks.getBookByName(book)?.id ?: 0
            apiChapter.data.map { chapterVerse ->
                val apiVerseDto = ApiVerseDto(
                    verse = chapterVerse.verse,
                    text = chapterVerse.text
                )
                apiVerseDto.toUIVerse(bookId, chapter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // Fetch all available Bible versions from the API
    suspend fun getAvailableVersions(): List<BibleVersionDto> {
        return try {
            client.get("$baseUrl/bibles/bibles.json").body<List<BibleVersionDto>>()  // Explicit type here
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}