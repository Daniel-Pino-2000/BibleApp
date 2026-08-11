package com.application.bibleapp.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Test

class HelloAoDownloadMapperTest {

    private fun verseContent(number: Int, text: String): JsonElement =
        Json.parseToJsonElement("""{"type":"verse","number":$number,"content":["$text"]}""")

    private fun translation(vararg books: HelloAoCompleteBookDto) = HelloAoCompleteTranslationDto(
        translation = HelloAoTranslationDto(
            id = "TEST",
            name = "Test Translation",
            englishName = "Test Translation",
            language = "eng"
        ),
        books = books.toList()
    )

    private fun book(id: String, order: Int, chapters: List<HelloAoChapterEntryDto>, isApocryphal: Boolean = false) =
        HelloAoCompleteBookDto(
            id = id,
            name = id,
            order = order,
            numberOfChapters = chapters.size,
            isApocryphal = isApocryphal,
            chapters = chapters
        )

    private fun chapter(number: Int, vararg verses: Pair<Int, String>) = HelloAoChapterEntryDto(
        numberOfVerses = verses.size,
        chapter = HelloAoChapterContentDto(
            number = number,
            content = verses.map { (num, text) -> verseContent(num, text) }
        )
    )

    @Test
    fun `book order maps directly onto bookId`() {
        val dto = translation(
            book("GEN", order = 1, chapters = listOf(chapter(1, 1 to "In the beginning..."))),
            book("REV", order = 66, chapters = listOf(chapter(1, 1 to "The Revelation...")))
        )

        val result = HelloAoDownloadMapper.map(dto)

        assertEquals(setOf(1, 66), result.verses.map { it.bookId }.toSet())
        assertEquals(0, result.skippedBookCount)
    }

    @Test
    fun `a partial NT-only translation keeps its original book order`() {
        // Confirmed live against GHT: Matthew still reports order=40, not renumbered to 1.
        val dto = translation(
            book("MAT", order = 40, chapters = listOf(chapter(1, 1 to "The book of the genealogy...")))
        )

        val result = HelloAoDownloadMapper.map(dto)

        assertEquals(1, result.verses.size)
        assertEquals(40, result.verses.first().bookId)
    }

    @Test
    fun `books outside the 1-66 canon are skipped, not treated as an error`() {
        val dto = translation(
            book("GEN", order = 1, chapters = listOf(chapter(1, 1 to "Text"))),
            book("APOC", order = 90, chapters = listOf(chapter(1, 1 to "Apocryphal text")), isApocryphal = true)
        )

        val result = HelloAoDownloadMapper.map(dto)

        assertEquals(1, result.verses.size)
        assertEquals(1, result.skippedBookCount)
        assertEquals(1, result.verses.first().bookId)
    }

    @Test
    fun `chapter and verse numbers are preserved`() {
        val dto = translation(
            book(
                "GEN", order = 1,
                chapters = listOf(
                    chapter(1, 1 to "Verse one", 2 to "Verse two"),
                    chapter(2, 1 to "Chapter two verse one")
                )
            )
        )

        val result = HelloAoDownloadMapper.map(dto)

        assertEquals(2, result.downloadedChapterCount)
        assertEquals(3, result.verses.size)
        assertEquals(
            MappedVerse(bookId = 1, chapter = 2, verse = 1, text = "Chapter two verse one"),
            result.verses.last()
        )
    }
}
