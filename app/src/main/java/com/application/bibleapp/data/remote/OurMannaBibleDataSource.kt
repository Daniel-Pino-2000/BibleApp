package com.application.bibleapp.data.remote

import android.util.Log
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.DailyVerseRef
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import java.io.IOException

/**
 * Client for OurManna's verse-of-the-day endpoint — used only for the Home screen's
 * daily verse. Returns a [DailyVerseRef] (not verse text): the reference is resolved
 * against whatever translation the user has selected, via [BibleBooks], so the daily
 * verse always shows in the user's own language/version rather than OurManna's NIV.
 */
class OurMannaBibleDataSource(
    private val baseUrl: String = "https://beta.ourmanna.com/api/v1/get",
    private val client: HttpClient = HttpClientProvider.client
) : DailyVerseDataSource {

    override suspend fun getDailyVerse(): DailyVerseRef {
        val url = "$baseUrl?format=json&order=daily"
        val response = client.get(url)

        if (response.status != HttpStatusCode.OK) {
            throw IOException("Failed to load daily verse: server returned ${response.status}")
        }

        val dto: OurMannaResponseDto = response.body()
        return parseReference(dto.verse.details.reference)
    }

    // internal (not private) so unit tests can exercise the parsing directly; still
    // outside DailyVerseDataSource's public contract.
    internal fun parseReference(rawReference: String): DailyVerseRef {
        val match = REFERENCE_PATTERN.matchEntire(rawReference)
            ?: throw IllegalArgumentException("Invalid reference: $rawReference")

        val book = match.groupValues[1]
        val chapter = match.groupValues[2].toInt()
        val startVerse = match.groupValues[3].toInt()
        val endVerse = match.groupValues[4].takeIf { it.isNotEmpty() }?.toInt()

        val bookId = BibleBooks.getBookByName(book)?.id
            ?: throw IllegalArgumentException("Unknown Bible book: $book")

        return DailyVerseRef(
            bookId = bookId,
            chapter = chapter,
            startVerse = startVerse,
            endVerse = endVerse
        )
    }

    private companion object {
        // e.g. "John 3:16" or "John 3:20-21" — book names can contain a leading
        // number ("1 John") so the book group is matched non-greedily up to the
        // first " <digits>:<digits>" it finds.
        val REFERENCE_PATTERN = Regex("""^(.+?)\s+(\d+):(\d+)(?:-(\d+))?$""")
    }
}