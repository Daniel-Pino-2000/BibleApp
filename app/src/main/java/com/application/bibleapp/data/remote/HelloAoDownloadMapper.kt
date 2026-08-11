package com.application.bibleapp.data.remote

/**
 * Maps a parsed [HelloAoCompleteTranslationDto] onto the app's internal 1–66
 * canonical book numbering. Pure/no Android dependency, so it's plain-JVM testable.
 */
object HelloAoDownloadMapper {

    private val CANONICAL_BOOK_RANGE = 1..66

    data class MappingResult(
        val verses: List<MappedVerse>,
        val downloadedChapterCount: Int,
        val skippedBookCount: Int
    )

    /**
     * [HelloAoCompleteBookDto.order] was confirmed live to stay in standard
     * canonical order (1=Genesis..66=Revelation) even for partial translations
     * (e.g. an NT-only translation's Matthew still reports order=40) — so it
     * maps directly onto book_id. Books outside 1..66 (e.g. apocrypha) aren't
     * supported by the local schema and are skipped, not treated as an error.
     */
    fun map(complete: HelloAoCompleteTranslationDto): MappingResult {
        val verses = mutableListOf<MappedVerse>()
        var skippedBooks = 0
        var chapterCount = 0

        complete.books.forEach { book ->
            if (book.order !in CANONICAL_BOOK_RANGE) {
                skippedBooks++
                return@forEach
            }

            book.chapters.forEach { chapterEntry ->
                chapterCount++
                val chapterNumber = chapterEntry.chapter.number
                HelloAoContentParser.extractVerseTexts(chapterEntry.chapter.content).forEach { (verseNumber, text) ->
                    verses += MappedVerse(
                        bookId = book.order,
                        chapter = chapterNumber,
                        verse = verseNumber,
                        text = text
                    )
                }
            }
        }

        return MappingResult(
            verses = verses,
            downloadedChapterCount = chapterCount,
            skippedBookCount = skippedBooks
        )
    }
}
