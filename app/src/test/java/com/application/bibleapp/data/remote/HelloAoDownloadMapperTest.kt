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

    private fun book(
        id: String,
        order: Int,
        chapters: List<HelloAoChapterEntryDto>,
        isApocryphal: Boolean = false,
        name: String = id
    ) = HelloAoCompleteBookDto(
        id = id,
        name = name,
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
        val last = result.verses.last()
        assertEquals(1, last.bookId)
        assertEquals(2, last.chapter)
        assertEquals(1, last.verse)
        assertEquals("Chapter two verse one", last.text)
    }

    @Test
    fun `chapter info records the verse count actually present, per chapter`() {
        val dto = translation(
            book(
                "GEN", order = 1,
                chapters = listOf(
                    chapter(1, 1 to "Verse one", 2 to "Verse two", 3 to "Verse three"),
                    chapter(2, 1 to "Chapter two verse one")
                )
            )
        )

        val result = HelloAoDownloadMapper.map(dto)

        assertEquals(
            mapOf(1 to 3, 2 to 1),
            result.chapterInfo.associate { it.chapter to it.verseCount }
        )
        assertEquals(setOf(1), result.chapterInfo.map { it.bookId }.toSet())
    }

    @Test
    fun `chapter info uses the highest verse number, not just how many verses were parsed`() {
        // A translation that renders a combined verse (e.g. 15-16) as one verse numbered
        // 15 should still size the chapter as going up to 16, not undercount it as 2.
        val dto = translation(
            book("GEN", order = 1, chapters = listOf(chapter(1, 1 to "Verse one", 16 to "Verses 15-16 combined")))
        )

        val result = HelloAoDownloadMapper.map(dto)

        assertEquals(16, result.chapterInfo.single().verseCount)
    }

    @Test
    fun `rich content from the parser is carried through to MappedVerse`() {
        val dto = translation(
            book("GEN", order = 1, chapters = listOf(chapter(1, 1 to "In the beginning...")))
        )

        val result = HelloAoDownloadMapper.map(dto)

        val richContent = result.verses.single().richContent
        assertEquals(1, richContent?.runs?.size)
        assertEquals("In the beginning...", richContent?.runs?.first()?.text)
    }

    @Test
    fun `book names are captured in the translation's own language`() {
        val dto = translation(
            book("GEN", order = 1, chapters = listOf(chapter(1, 1 to "En el principio...")), name = "Génesis"),
            book("REV", order = 66, chapters = listOf(chapter(1, 1 to "La revelacion...")), name = "Apocalipsis")
        )

        val result = HelloAoDownloadMapper.map(dto)

        assertEquals(
            mapOf(1 to "Génesis", 66 to "Apocalipsis"),
            result.bookNames.associate { it.bookId to it.name }
        )
    }

    @Test
    fun `book names for apocryphal books outside 1-66 are not captured`() {
        val dto = translation(
            book("GEN", order = 1, chapters = listOf(chapter(1, 1 to "Text")), name = "Genesis"),
            book("APOC", order = 90, chapters = listOf(chapter(1, 1 to "Apocryphal text")), isApocryphal = true, name = "Apocrypha Book")
        )

        val result = HelloAoDownloadMapper.map(dto)

        assertEquals(listOf(1), result.bookNames.map { it.bookId })
    }

    @Test
    fun `footnotes are scoped to the book and chapter they came from`() {
        val chapterWithFootnote = HelloAoChapterEntryDto(
            numberOfVerses = 1,
            chapter = HelloAoChapterContentDto(
                number = 1,
                content = listOf(verseContent(3, "Let there be light")),
                footnotes = listOf(
                    HelloAoFootnoteDto(noteId = 0, text = "Cited elsewhere", caller = "+", reference = HelloAoFootnoteReferenceDto(1, 3))
                )
            )
        )
        val dto = translation(book("GEN", order = 1, chapters = listOf(chapterWithFootnote)))

        val result = HelloAoDownloadMapper.map(dto)

        assertEquals(1, result.footnotes.size)
        val footnote = result.footnotes.single()
        assertEquals(1, footnote.bookId)
        assertEquals(1, footnote.chapter)
        assertEquals(0, footnote.noteId)
        assertEquals(3, footnote.verse)
        assertEquals("Cited elsewhere", footnote.text)
    }
}
