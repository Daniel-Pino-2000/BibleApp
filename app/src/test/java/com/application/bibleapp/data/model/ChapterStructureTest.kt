package com.application.bibleapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ChapterStructureTest {

    @Test
    fun `an empty structure falls back to BibleBooks' KJV-based counts`() {
        val structure: ChapterStructure = emptyMap()

        // Genesis: 50 chapters, chapter 1 has 31 verses per BibleBooks.
        assertEquals(50, structure.chapterCount(1))
        assertEquals(31, structure.verseCount(1, 1))
    }

    @Test
    fun `a version's own structure overrides BibleBooks when present`() {
        // A hypothetical translation whose Genesis has only 2 chapters, chapter 1 with 40 verses.
        val structure: ChapterStructure = mapOf(1 to mapOf(1 to 40, 2 to 10))

        assertEquals(2, structure.chapterCount(1))
        assertEquals(40, structure.verseCount(1, 1))
    }

    @Test
    fun `a book missing from the structure still falls back per-book`() {
        // Structure only knows about Genesis (book 1) — Exodus (book 2, 40 chapters,
        // chapter 1 with 22 verses per BibleBooks) falls back independently.
        val structure: ChapterStructure = mapOf(1 to mapOf(1 to 999))

        assertEquals(40, structure.chapterCount(2))
        assertEquals(22, structure.verseCount(2, 1))
    }
}
