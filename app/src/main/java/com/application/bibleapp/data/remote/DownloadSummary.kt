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

/**
 * [downloadedChapters] were fetched successfully and are ready to write.
 * [skippedChapters] genuinely don't exist for this version (real 404s — e.g. an
 * NT-only translation missing the Old Testament); that's absent content, not a failure.
 * [failedChapters] are real network/server errors that survived retries — these are
 * the only ones that should fail the whole download.
 */
data class DownloadSummary(
    val downloadedChapters: Int,
    val skippedChapters: Int,
    val failedChapters: Int,
    val sampleErrors: List<String>
) {
    val attemptedChapters: Int get() = downloadedChapters + skippedChapters + failedChapters
    val hasFailures: Boolean get() = failedChapters > 0
    val hasContent: Boolean get() = downloadedChapters > 0
}

/**
 * Reduces raw fetch outcomes to a downloaded/skipped/failed summary with a few
 * sample error messages for display, without touching the database or the network.
 */
fun summarizeDownload(outcomes: List<ChapterDownloadOutcome>, maxSamples: Int = 3): DownloadSummary {
    var downloaded = 0
    var skipped = 0
    val failures = mutableListOf<ChapterDownloadOutcome>()

    outcomes.forEach { outcome ->
        when (outcome.result) {
            is ChapterFetchResult.Success -> downloaded++
            is ChapterFetchResult.NotFound -> skipped++
            is ChapterFetchResult.Error -> failures += outcome
        }
    }

    val samples = failures.take(maxSamples).map { outcome ->
        val message = (outcome.result as ChapterFetchResult.Error).message
        "${outcome.bookSlug} ${outcome.chapterNumber}: $message"
    }

    return DownloadSummary(
        downloadedChapters = downloaded,
        skippedChapters = skipped,
        failedChapters = failures.size,
        sampleErrors = samples
    )
}
