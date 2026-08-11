package com.application.bibleapp.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadSummaryTest {

    private fun success(bookId: Int = 1, slug: String = "genesis", chapter: Int = 1) =
        ChapterDownloadOutcome(bookId, slug, chapter, ChapterFetchResult.Success(ApiChapterDto(emptyList())))

    @Test
    fun `all successes is a complete download with no errors`() {
        val outcomes = (1..5).map { success(chapter = it) }
        val summary = summarizeDownload(outcomes)

        assertTrue(summary.isComplete)
        assertEquals(0, summary.failedCount)
        assertTrue(summary.sampleErrors.isEmpty())
    }

    @Test
    fun `any failure marks the download incomplete`() {
        val outcomes = listOf(
            success(chapter = 1),
            ChapterDownloadOutcome(9, "1samuel", 1, ChapterFetchResult.NotFound),
            success(chapter = 2)
        )
        val summary = summarizeDownload(outcomes)

        assertFalse(summary.isComplete)
        assertEquals(1, summary.failedCount)
        assertEquals(listOf("1samuel 1: not found"), summary.sampleErrors)
    }

    @Test
    fun `sample errors are capped so the message stays readable`() {
        val outcomes = (1..10).map {
            ChapterDownloadOutcome(1, "genesis", it, ChapterFetchResult.Error("boom $it"))
        }
        val summary = summarizeDownload(outcomes, maxSamples = 3)

        assertEquals(10, summary.failedCount)
        assertEquals(3, summary.sampleErrors.size)
    }

    @Test
    fun `network errors preserve their message for display`() {
        val outcomes = listOf(
            ChapterDownloadOutcome(1, "genesis", 1, ChapterFetchResult.Error("Unable to resolve host"))
        )
        val summary = summarizeDownload(outcomes)

        assertEquals(listOf("genesis 1: Unable to resolve host"), summary.sampleErrors)
    }
}
