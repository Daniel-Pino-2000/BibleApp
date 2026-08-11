package com.application.bibleapp.data.remote

/**
 * Result of fetching one chapter as part of a bulk version download.
 * Kept separate from the DB layer (no Android dependency) so it's plain-JVM testable.
 */
data class ChapterDownloadOutcome(
    val bookId: Int,
    val bookSlug: String,
    val chapterNumber: Int,
    val result: ChapterFetchResult
)

data class DownloadSummary(
    val failedCount: Int,
    val sampleErrors: List<String>
) {
    val isComplete: Boolean get() = failedCount == 0
}

/**
 * Reduces raw fetch outcomes to a pass/fail summary with a few sample error
 * messages for display, without touching the database or the network.
 */
fun summarizeDownload(outcomes: List<ChapterDownloadOutcome>, maxSamples: Int = 3): DownloadSummary {
    val failures = outcomes.filter { it.result !is ChapterFetchResult.Success }
    val samples = failures.take(maxSamples).map { outcome ->
        val reason = when (val result = outcome.result) {
            is ChapterFetchResult.NotFound -> "not found"
            is ChapterFetchResult.Error -> result.message
            is ChapterFetchResult.Success -> "" // unreachable: filtered out above
        }
        "${outcome.bookSlug} ${outcome.chapterNumber}: $reason"
    }
    return DownloadSummary(failedCount = failures.size, sampleErrors = samples)
}
