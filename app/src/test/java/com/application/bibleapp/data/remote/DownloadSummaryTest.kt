package com.application.bibleapp.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadSummaryTest {

    private fun success(bookId: Int = 1, slug: String = "genesis", chapter: Int = 1) =
        ChapterDownloadOutcome(bookId, slug, chapter, ChapterFetchResult.Success(ApiChapterDto(emptyList())))

    @Test
    fun `all successes has no skipped or failed chapters`() {
        val outcomes = (1..5).map { success(chapter = it) }
        val summary = summarizeDownload(outcomes)

        assertFalse(summary.hasFailures)
        assertTrue(summary.hasContent)
        assertEquals(5, summary.downloadedChapters)
        assertEquals(0, summary.skippedChapters)
        assertEquals(0, summary.failedChapters)
        assertTrue(summary.sampleErrors.isEmpty())
    }

    @Test
    fun `a genuine 404 is skipped, not a failure`() {
        val outcomes = listOf(
            success(chapter = 1),
            ChapterDownloadOutcome(9, "1samuel", 1, ChapterFetchResult.NotFound),
            success(chapter = 2)
        )
        val summary = summarizeDownload(outcomes)

        assertFalse("a real 404 must not count as a failure", summary.hasFailures)
        assertEquals(2, summary.downloadedChapters)
        assertEquals(1, summary.skippedChapters)
        assertEquals(0, summary.failedChapters)
    }

    @Test
    fun `a network error after retries is a real failure`() {
        val outcomes = listOf(
            success(chapter = 1),
            ChapterDownloadOutcome(1, "genesis", 2, ChapterFetchResult.Error("UnresolvedAddressException"))
        )
        val summary = summarizeDownload(outcomes)

        assertTrue(summary.hasFailures)
        assertEquals(1, summary.failedChapters)
        assertEquals(listOf("genesis 2: UnresolvedAddressException"), summary.sampleErrors)
    }

    @Test
    fun `sample errors are capped so the message stays readable`() {
        val outcomes = (1..10).map {
            ChapterDownloadOutcome(1, "genesis", it, ChapterFetchResult.Error("boom $it"))
        }
        val summary = summarizeDownload(outcomes, maxSamples = 3)

        assertEquals(10, summary.failedChapters)
        assertEquals(3, summary.sampleErrors.size)
    }

    @Test
    fun `a version that is entirely 404 has no content but no failures either`() {
        val outcomes = listOf(
            ChapterDownloadOutcome(1, "genesis", 1, ChapterFetchResult.NotFound),
            ChapterDownloadOutcome(1, "genesis", 2, ChapterFetchResult.NotFound)
        )
        val summary = summarizeDownload(outcomes)

        assertFalse(summary.hasContent)
        assertFalse(summary.hasFailures)
        assertEquals(2, summary.skippedChapters)
    }
}
